package com.chungpr0.bookhub.modules.inventory.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.inventory.dto.request.BatchFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateStockReceiptRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.StockReceiptFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.StockReceiptItemRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.BatchResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.CreateStockReceiptResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.StockReceiptSummaryResponse;
import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import com.chungpr0.bookhub.modules.inventory.entity.InventoryTransaction;
import com.chungpr0.bookhub.modules.inventory.entity.StockReceipt;
import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import com.chungpr0.bookhub.modules.inventory.enums.InventoryTransactionType;
import com.chungpr0.bookhub.modules.inventory.mapper.InventoryMapper;
import com.chungpr0.bookhub.modules.inventory.repository.BatchRepository;
import com.chungpr0.bookhub.modules.inventory.repository.InventoryTransactionRepository;
import com.chungpr0.bookhub.modules.inventory.repository.StockReceiptRepository;
import com.chungpr0.bookhub.modules.inventory.repository.SupplierRepository;
import com.chungpr0.bookhub.modules.inventory.repository.specification.BatchSpecification;
import com.chungpr0.bookhub.modules.inventory.repository.specification.StockReceiptSpecification;
import com.chungpr0.bookhub.modules.inventory.service.StockReceiptService;
import com.chungpr0.bookhub.modules.user.entity.Staff;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StockReceiptServiceImpl implements StockReceiptService {

    private static final String RANDOM_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final StockReceiptRepository stockReceiptRepository;
    private final SupplierRepository supplierRepository;
    private final BatchRepository batchRepository;
    private final BookRepository bookRepository;
    private final InventoryTransactionRepository inventoryTransactionRepository;
    private final StaffRepository staffRepository;
    private final AccountRepository accountRepository;

    @Override
    @Transactional
    public CreateStockReceiptResponse createStockReceipt(Long accountId, String idempotencyKey, CreateStockReceiptRequest request) {
        if (StringUtils.hasText(idempotencyKey)) {
            Optional<StockReceipt> existingReceiptOpt = stockReceiptRepository.findByIdempotencyKey(idempotencyKey.trim());
            if (existingReceiptOpt.isPresent()) {
                StockReceipt existing = existingReceiptOpt.get();
                String creatorName = resolveCreatorName(existing.getCreatedBy());
                return CreateStockReceiptResponse.builder()
                        .receipt(InventoryMapper.toStockReceiptDetailResponse(existing, creatorName))
                        .warnings(new ArrayList<>())
                        .build();
            }
        }

        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        LocalDate importDate = request.getImportDate();
        LocalDate today = LocalDate.now();
        if (importDate.isAfter(today)) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Ngày nhập không được ở tương lai");
        }
        if (importDate.isBefore(today.minusDays(30))) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Ngày nhập không được quá 30 ngày trước");
        }

        List<StockReceiptItemRequest> items = request.getItems();
        if (items == null || items.isEmpty()) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Danh sách sách nhập không được để trống");
        }

        Set<Long> bookIdSet = new HashSet<>();
        for (StockReceiptItemRequest item : items) {
            if (!bookIdSet.add(item.getBookId())) {
                throw new AppException(ErrorCode.VALIDATION_FAILED, "Danh sách sách nhập có chứa ID sách trùng lặp: " + item.getBookId());
            }
        }

        List<String> warnings = new ArrayList<>();
        int totalQuantity = 0;
        long totalCost = 0L;

        List<Book> booksToUpdate = new ArrayList<>();
        for (StockReceiptItemRequest item : items) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND, "Không tìm thấy cuốn sách với ID: " + item.getBookId()));

            if (book.getStatus() == BookStatus.INACTIVE) {
                warnings.add("Sách [ID: " + book.getId() + ", " + book.getTitle() + "] đang ở trạng thái ngừng bán (INACTIVE)");
            }

            if (item.getImportPrice() >= book.getSalePrice()) {
                warnings.add("Sách [ID: " + book.getId() + "] có giá nhập (" + item.getImportPrice() + " đ) lớn hơn hoặc bằng giá bán hiện tại (" + book.getSalePrice() + " đ)");
            }

            totalQuantity += item.getQuantity();
            totalCost += (item.getImportPrice() * item.getQuantity());
            booksToUpdate.add(book);
        }

        String receiptCode = generateReceiptCode(importDate);

        StockReceipt receipt = StockReceipt.builder()
                .receiptCode(receiptCode)
                .supplier(supplier)
                .note(StringUtils.hasText(request.getNote()) ? request.getNote().trim() : null)
                .importDate(importDate)
                .totalQuantity(totalQuantity)
                .totalCost(totalCost)
                .createdBy(accountId)
                .idempotencyKey(StringUtils.hasText(idempotencyKey) ? idempotencyKey.trim() : null)
                .build();

        StockReceipt savedReceipt = stockReceiptRepository.save(receipt);

        List<Batch> savedBatches = new ArrayList<>();
        for (int i = 0; i < items.size(); i++) {
            StockReceiptItemRequest item = items.get(i);
            Book book = booksToUpdate.get(i);

            String batchCode = "BATCH-" + receiptCode + "-" + book.getId();

            Batch batch = Batch.builder()
                    .receipt(savedReceipt)
                    .book(book)
                    .batchCode(batchCode)
                    .importPrice(item.getImportPrice())
                    .quantityImported(item.getQuantity())
                    .quantityRemaining(item.getQuantity())
                    .importDate(importDate)
                    .build();

            Batch savedBatch = batchRepository.save(batch);
            savedBatches.add(savedBatch);

            book.setStockQuantity(book.getStockQuantity() + item.getQuantity());
            bookRepository.save(book);

            InventoryTransaction transaction = InventoryTransaction.builder()
                    .book(book)
                    .batch(savedBatch)
                    .type(InventoryTransactionType.IMPORT)
                    .quantity(item.getQuantity())
                    .stockAfter(book.getStockQuantity())
                    .referenceType("RECEIPT")
                    .referenceId(savedReceipt.getId())
                    .note("Nhập theo phiếu " + receiptCode)
                    .createdBy(accountId)
                    .build();

            inventoryTransactionRepository.save(transaction);
        }

        savedReceipt.setItems(savedBatches);
        String creatorName = resolveCreatorName(accountId);

        return CreateStockReceiptResponse.builder()
                .receipt(InventoryMapper.toStockReceiptDetailResponse(savedReceipt, creatorName))
                .warnings(warnings)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockReceiptSummaryResponse> getStockReceipts(StockReceiptFilterRequest filter, Pageable pageable) {
        String keyword = (filter != null) ? filter.getKeyword() : null;
        Long supplierId = (filter != null) ? filter.getSupplierId() : null;
        Long createdBy = (filter != null) ? filter.getCreatedBy() : null;
        LocalDate importDateFrom = (filter != null) ? filter.getImportDateFrom() : null;
        LocalDate importDateTo = (filter != null) ? filter.getImportDateTo() : null;

        Specification<StockReceipt> spec = StockReceiptSpecification.filter(
                keyword, supplierId, createdBy, importDateFrom, importDateTo
        );

        Page<StockReceipt> page = stockReceiptRepository.findAll(spec, pageable);

        List<StockReceiptSummaryResponse> items = new ArrayList<>();
        for (StockReceipt receipt : page.getContent()) {
            String creatorName = resolveCreatorName(receipt.getCreatedBy());
            items.add(InventoryMapper.toStockReceiptSummaryResponse(receipt, creatorName));
        }

        return PageResponse.of(items, page);
    }

    @Override
    @Transactional(readOnly = true)
    public StockReceiptDetailResponse getStockReceiptDetail(Long id) {
        StockReceipt receipt = stockReceiptRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.STOCK_RECEIPT_NOT_FOUND));

        String creatorName = resolveCreatorName(receipt.getCreatedBy());
        return InventoryMapper.toStockReceiptDetailResponse(receipt, creatorName);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BatchResponse> getBatches(BatchFilterRequest filter, Pageable pageable) {
        Long bookId = (filter != null) ? filter.getBookId() : null;
        Long receiptId = (filter != null) ? filter.getReceiptId() : null;
        Long supplierId = (filter != null) ? filter.getSupplierId() : null;
        Boolean hasRemaining = (filter != null) ? filter.getHasRemaining() : null;

        Specification<Batch> spec = BatchSpecification.filter(bookId, receiptId, supplierId, hasRemaining);
        Page<Batch> page = batchRepository.findAll(spec, pageable);

        return PageResponse.of(page, InventoryMapper::toBatchResponse);
    }

    private String generateReceiptCode(LocalDate date) {
        String datePart = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder sb = new StringBuilder(4);
        for (int i = 0; i < 4; i++) {
            sb.append(RANDOM_CHARS.charAt(RANDOM.nextInt(RANDOM_CHARS.length())));
        }
        return "PN-" + datePart + "-" + sb;
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

