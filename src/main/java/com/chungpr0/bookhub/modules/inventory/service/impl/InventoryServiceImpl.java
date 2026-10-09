package com.chungpr0.bookhub.modules.inventory.service.impl;

import com.chungpr0.bookhub.common.dto.PageMeta;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.inventory.dto.request.AdjustInventoryRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.InventoryTransactionFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.LowStockFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.AffectedBatchResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryAdjustmentResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryCardSummaryResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryTransactionPageResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryTransactionResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.LowStockBookResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.LowStockCountSummaryResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.LowStockPageResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SimpleSupplierResponse;
import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import com.chungpr0.bookhub.modules.inventory.entity.InventoryTransaction;
import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentReason;
import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentType;
import com.chungpr0.bookhub.modules.inventory.enums.InventoryTransactionType;
import com.chungpr0.bookhub.modules.inventory.mapper.InventoryMapper;
import com.chungpr0.bookhub.modules.inventory.repository.BatchRepository;
import com.chungpr0.bookhub.modules.inventory.repository.InventoryTransactionRepository;
import com.chungpr0.bookhub.modules.inventory.repository.specification.InventoryTransactionSpecification;
import com.chungpr0.bookhub.modules.inventory.service.InventoryService;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private static final ZoneId VIETNAM_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final BookRepository bookRepository;
    private final BatchRepository batchRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public InventoryAdjustmentResponse adjustInventory(Long accountId, String idempotencyKey, AdjustInventoryRequest request) {
        if (StringUtils.hasText(idempotencyKey)) {
            Optional<InventoryTransaction> existingTxOpt = inventoryTransactionRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existingTxOpt.isPresent()) {
                InventoryTransaction tx = existingTxOpt.get();
                return InventoryAdjustmentResponse.builder()
                        .transactionId(tx.getId())
                        .book(InventoryMapper.toSimpleBookResponse(tx.getBook()))
                        .type(tx.getType() == InventoryTransactionType.ADJUST_IN ? AdjustmentType.ADJUST_IN : AdjustmentType.ADJUST_OUT)
                        .quantity(Math.abs(tx.getQuantity()))
                        .stockBefore(tx.getStockAfter() - tx.getQuantity())
                        .stockAfter(tx.getStockAfter())
                        .reason(tx.getReason())
                        .affectedBatches(new ArrayList<>())
                        .createdAt(tx.getCreatedAt())
                        .build();
            }
        }

        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));

        if (request.getReason() == AdjustmentReason.OTHER && !StringUtils.hasText(request.getNote())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Lý do OTHER yêu cầu ghi chú giải trình");
        }

        int stockBefore = book.getStockQuantity();
        int stockAfter;
        List<AffectedBatchResponse> affectedBatches = new ArrayList<>();
        Batch createdBatch = null;

        if (request.getType() == AdjustmentType.ADJUST_OUT) {
            if (request.getQuantity() > book.getStockQuantity()) {
                throw new AppException(ErrorCode.ADJUSTMENT_EXCEEDS_STOCK,
                        "Số lượng điều chỉnh giảm (" + request.getQuantity() + ") vượt quá tồn kho hiện tại (" + book.getStockQuantity() + ")");
            }

            int remainingToDeduct = request.getQuantity();
            List<Batch> batches = batchRepository.findByBookIdAndQuantityRemainingGreaterThanOrderByImportDateAscIdAsc(book.getId(), 0);

            for (Batch batch : batches) {
                int deduct = Math.min(batch.getQuantityRemaining(), remainingToDeduct);
                batch.setQuantityRemaining(batch.getQuantityRemaining() - deduct);
                batchRepository.save(batch);

                affectedBatches.add(AffectedBatchResponse.builder()
                        .batchCode(batch.getBatchCode())
                        .quantityDeducted(deduct)
                        .remainingInBatch(batch.getQuantityRemaining())
                        .build());

                remainingToDeduct -= deduct;
                if (remainingToDeduct <= 0) {
                    break;
                }
            }

            stockAfter = stockBefore - request.getQuantity();
            book.setStockQuantity(stockAfter);
            bookRepository.save(book);

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .book(book)
                    .type(InventoryTransactionType.ADJUST_OUT)
                    .quantity(-request.getQuantity())
                    .stockAfter(stockAfter)
                    .referenceType("ADJUSTMENT")
                    .reason(request.getReason().name())
                    .note(StringUtils.hasText(request.getNote()) ? request.getNote().trim() : null)
                    .createdBy(accountId)
                    .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey.trim() : null)
                    .build();

            InventoryTransaction savedTx = inventoryTransactionRepository.save(transaction);
            savedTx.setReferenceId(savedTx.getId());
            inventoryTransactionRepository.save(savedTx);

            return InventoryAdjustmentResponse.builder()
                    .transactionId(savedTx.getId())
                    .book(InventoryMapper.toSimpleBookResponse(book))
                    .type(AdjustmentType.ADJUST_OUT)
                    .quantity(request.getQuantity())
                    .stockBefore(stockBefore)
                    .stockAfter(stockAfter)
                    .reason(request.getReason().name())
                    .affectedBatches(affectedBatches)
                    .createdAt(savedTx.getCreatedAt())
                    .build();

        } else {
            if (request.getImportPrice() == null || request.getImportPrice() <= 0) {
                throw new AppException(ErrorCode.VALIDATION_FAILED, "Điều chỉnh tăng yêu cầu đơn giá nhập (importPrice > 0)");
            }

            String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            String batchCode = "BATCH-ADJ-" + datePart + "-" + book.getId() + "-" + (System.currentTimeMillis() % 10000);

            Batch batch = Batch.builder()
                    .receipt(null)
                    .book(book)
                    .batchCode(batchCode)
                    .importPrice(request.getImportPrice())
                    .quantityImported(request.getQuantity())
                    .quantityRemaining(request.getQuantity())
                    .importDate(LocalDate.now())
                    .build();

            createdBatch = batchRepository.save(batch);

            stockAfter = stockBefore + request.getQuantity();
            book.setStockQuantity(stockAfter);
            bookRepository.save(book);

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .book(book)
                    .batch(createdBatch)
                    .type(InventoryTransactionType.ADJUST_IN)
                    .quantity(request.getQuantity())
                    .stockAfter(stockAfter)
                    .referenceType("ADJUSTMENT")
                    .reason(request.getReason().name())
                    .note(StringUtils.hasText(request.getNote()) ? request.getNote().trim() : null)
                    .createdBy(accountId)
                    .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey.trim() : null)
                    .build();

            InventoryTransaction savedTx = inventoryTransactionRepository.save(transaction);
            savedTx.setReferenceId(savedTx.getId());
            inventoryTransactionRepository.save(savedTx);

            return InventoryAdjustmentResponse.builder()
                    .transactionId(savedTx.getId())
                    .book(InventoryMapper.toSimpleBookResponse(book))
                    .type(AdjustmentType.ADJUST_IN)
                    .quantity(request.getQuantity())
                    .stockBefore(stockBefore)
                    .stockAfter(stockAfter)
                    .reason(request.getReason().name())
                    .affectedBatches(affectedBatches)
                    .createdAt(savedTx.getCreatedAt())
                    .build();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryTransactionPageResponse getTransactions(InventoryTransactionFilterRequest filter, Pageable pageable) {
        Long bookId = (filter != null) ? filter.getBookId() : null;
        InventoryTransactionType type = (filter != null) ? filter.getType() : null;
        LocalDate createdFrom = (filter != null) ? filter.getCreatedFrom() : null;
        LocalDate createdTo = (filter != null) ? filter.getCreatedTo() : null;

        Specification<InventoryTransaction> spec = InventoryTransactionSpecification.filter(bookId, type, createdFrom, createdTo);
        Page<InventoryTransaction> page = inventoryTransactionRepository.findAll(spec, pageable);

        List<InventoryTransactionResponse> items = new ArrayList<>();
        for (InventoryTransaction tx : page.getContent()) {
            String creatorName = resolveCreatorName(tx.getCreatedBy());
            items.add(InventoryMapper.toInventoryTransactionResponse(tx, creatorName));
        }

        PageMeta pageMeta = PageMeta.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();

        InventoryCardSummaryResponse summary = null;
        if (bookId != null) {
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isPresent()) {
                Book book = bookOpt.get();
                List<InventoryTransaction> allTxs = inventoryTransactionRepository.findByBookId(book.getId());

                int totalImported = 0;
                int totalSold = 0;
                int totalAdjusted = 0;

                OffsetDateTime fromDateTime = (createdFrom != null) ? createdFrom.atStartOfDay(VIETNAM_ZONE).toOffsetDateTime() : null;
                OffsetDateTime toDateTime = (createdTo != null) ? createdTo.plusDays(1).atStartOfDay(VIETNAM_ZONE).minusNanos(1).toOffsetDateTime() : null;

                for (InventoryTransaction tx : allTxs) {
                    if (fromDateTime != null && tx.getCreatedAt().isBefore(fromDateTime)) {
                        continue;
                    }
                    if (toDateTime != null && tx.getCreatedAt().isAfter(toDateTime)) {
                        continue;
                    }

                    if (tx.getType() == InventoryTransactionType.IMPORT) {
                        totalImported += tx.getQuantity();
                    } else if (tx.getType() == InventoryTransactionType.SALE) {
                        totalSold += Math.abs(tx.getQuantity());
                    } else if (tx.getType() == InventoryTransactionType.ADJUST_IN || tx.getType() == InventoryTransactionType.ADJUST_OUT) {
                        totalAdjusted += tx.getQuantity();
                    }
                }

                int closingStock = book.getStockQuantity();
                int openingStock = closingStock - totalImported + totalSold - totalAdjusted;

                summary = InventoryCardSummaryResponse.builder()
                        .bookId(book.getId())
                        .bookTitle(book.getTitle())
                        .openingStock(openingStock)
                        .totalImported(totalImported)
                        .totalSold(totalSold)
                        .totalAdjusted(totalAdjusted)
                        .closingStock(closingStock)
                        .build();
            }
        }

        return InventoryTransactionPageResponse.builder()
                .summary(summary)
                .items(items)
                .page(pageMeta)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LowStockPageResponse getLowStockBooks(LowStockFilterRequest filter, Pageable pageable) {
        long lowStockCount = bookRepository.count((root, query, cb) ->
                cb.and(cb.gt(root.get("stockQuantity"), 0), cb.le(root.get("stockQuantity"), root.get("lowStockThreshold")))
        );

        long outOfStockCount = bookRepository.count((root, query, cb) ->
                cb.le(root.get("stockQuantity"), 0)
        );

        Specification<Book> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            String statusStr = (filter != null) ? filter.getStockStatus() : null;
            if ("LOW_STOCK".equalsIgnoreCase(statusStr)) {
                predicates.add(cb.and(
                        cb.gt(root.get("stockQuantity"), 0),
                        cb.le(root.get("stockQuantity"), root.get("lowStockThreshold"))
                ));
            } else if ("OUT_OF_STOCK".equalsIgnoreCase(statusStr)) {
                predicates.add(cb.le(root.get("stockQuantity"), 0));
            } else {
                predicates.add(cb.or(
                        cb.le(root.get("stockQuantity"), 0),
                        cb.le(root.get("stockQuantity"), root.get("lowStockThreshold"))
                ));
            }

            if (filter != null && filter.getCategoryId() != null) {
                predicates.add(cb.equal(root.get("category").get("id"), filter.getCategoryId()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Book> booksPage = bookRepository.findAll(spec, pageable);

        List<LowStockBookResponse> items = new ArrayList<>();
        for (Book book : booksPage.getContent()) {
            List<Batch> latestBatches = batchRepository.findLatestByBookId(book.getId(), PageRequest.of(0, 1));
            LocalDate lastImportDate = latestBatches.isEmpty() ? null : latestBatches.get(0).getImportDate();

            Supplier lastSupplierEntity = null;
            if (!latestBatches.isEmpty() && latestBatches.get(0).getReceipt() != null) {
                lastSupplierEntity = latestBatches.get(0).getReceipt().getSupplier();
            }
            SimpleSupplierResponse lastSupplier = InventoryMapper.toSimpleSupplierResponse(lastSupplierEntity);

            int stockQty = book.getStockQuantity();
            int sold30 = book.getSoldCount();
            int daysLeft = (stockQty <= 0) ? 0 : (sold30 > 0 ? (int) Math.round((double) stockQty / (sold30 / 30.0)) : 999);

            items.add(LowStockBookResponse.builder()
                    .book(InventoryMapper.toSimpleBookResponse(book))
                    .stockQuantity(stockQty)
                    .lowStockThreshold(book.getLowStockThreshold())
                    .stockStatus(book.getStockStatus().name())
                    .soldLast30Days(sold30)
                    .estimatedDaysOfStock(daysLeft)
                    .lastImportDate(lastImportDate)
                    .lastSupplier(lastSupplier)
                    .build());
        }

        PageMeta pageMeta = PageMeta.builder()
                .number(booksPage.getNumber())
                .size(booksPage.getSize())
                .totalElements(booksPage.getTotalElements())
                .totalPages(booksPage.getTotalPages())
                .first(booksPage.isFirst())
                .last(booksPage.isLast())
                .build();

        LowStockCountSummaryResponse countSummary = LowStockCountSummaryResponse.builder()
                .lowStockCount(lowStockCount)
                .outOfStockCount(outOfStockCount)
                .build();

        return LowStockPageResponse.builder()
                .summary(countSummary)
                .items(items)
                .page(pageMeta)
                .build();
    }

    private String resolveCreatorName(Long accountId) {
        if (accountId == null) {
            return "Hệ thống";
        }
        Optional<Staff> staffOpt = staffRepository.findByAccountId(accountId);
        if (staffOpt.isPresent() && StringUtils.hasText(staffOpt.get().getFullName())) {
            return staffOpt.get().getFullName();
        }
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            return accountOpt.get().getUsername();
        }
        return "Nhân viên #" + accountId;
    }
}

