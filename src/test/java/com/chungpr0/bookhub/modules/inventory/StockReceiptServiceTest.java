package com.chungpr0.bookhub.modules.inventory;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
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
import com.chungpr0.bookhub.modules.inventory.repository.BatchRepository;
import com.chungpr0.bookhub.modules.inventory.repository.InventoryTransactionRepository;
import com.chungpr0.bookhub.modules.inventory.repository.StockReceiptRepository;
import com.chungpr0.bookhub.modules.inventory.repository.SupplierRepository;
import com.chungpr0.bookhub.modules.inventory.service.impl.StockReceiptServiceImpl;
import com.chungpr0.bookhub.modules.user.repository.StaffRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReceiptServiceTest {

    @Mock
    private StockReceiptRepository stockReceiptRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private StockReceiptServiceImpl stockReceiptService;

    private Supplier testSupplier;
    private Book testBook;
    private StockReceipt testReceipt;
    private Batch testBatch;

    @BeforeEach
    void setUp() {
        testSupplier = Supplier.builder()
                .id(1L)
                .name("Công ty Nhã Nam")
                .build();

        testBook = Book.builder()
                .id(101L)
                .title("Nhà Giả Kim")
                .isbn("9786045629870")
                .slug("nha-gia-kim")
                .stockQuantity(10)
                .salePrice(80000L)
                .status(BookStatus.ACTIVE)
                .build();

        testReceipt = StockReceipt.builder()
                .id(88L)
                .receiptCode("PN-20261009-TEST")
                .supplier(testSupplier)
                .importDate(LocalDate.now())
                .totalQuantity(200)
                .totalCost(9480000L)
                .createdBy(1L)
                .items(new ArrayList<>())
                .build();

        testBatch = Batch.builder()
                .id(501L)
                .batchCode("BATCH-PN-20261009-TEST-101")
                .receipt(testReceipt)
                .book(testBook)
                .importPrice(47400L)
                .quantityImported(200)
                .quantityRemaining(200)
                .importDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("createStockReceipt - Thành công tạo mới phiếu nhập kho và tăng tồn kho")
    void testCreateStockReceipt_Success() {
        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(1L)
                .importDate(LocalDate.now())
                .note("Nhập sách đợt 1")
                .items(List.of(
                        StockReceiptItemRequest.builder()
                                .bookId(101L)
                                .quantity(200)
                                .importPrice(47400L)
                                .build()
                ))
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));
        when(stockReceiptRepository.save(any(StockReceipt.class))).thenAnswer(inv -> {
            StockReceipt r = inv.getArgument(0);
            r.setId(88L);
            return r;
        });
        when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> {
            Batch b = inv.getArgument(0);
            b.setId(501L);
            return b;
        });

        CreateStockReceiptResponse response = stockReceiptService.createStockReceipt(1L, null, request);

        assertThat(response).isNotNull();
        assertThat(response.getReceipt()).isNotNull();
        assertThat(response.getReceipt().getTotalQuantity()).isEqualTo(200);
        assertThat(response.getReceipt().getTotalCost()).isEqualTo(9480000L);
        assertThat(testBook.getStockQuantity()).isEqualTo(210);
        assertThat(response.getWarnings()).isEmpty();

        verify(bookRepository).save(testBook);
        verify(inventoryTransactionRepository).save(any(InventoryTransaction.class));
    }

    @Test
    @DisplayName("createStockReceipt - Trả về phiếu cũ khi trùng Idempotency-Key")
    void testCreateStockReceipt_IdempotencyKeyCached() {
        when(stockReceiptRepository.findByIdempotencyKey("idemp-key-123")).thenReturn(Optional.of(testReceipt));

        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(1L)
                .importDate(LocalDate.now())
                .items(List.of(StockReceiptItemRequest.builder().bookId(101L).quantity(5).importPrice(50000L).build()))
                .build();

        CreateStockReceiptResponse response = stockReceiptService.createStockReceipt(1L, "idemp-key-123", request);

        assertThat(response).isNotNull();
        assertThat(response.getReceipt().getReceiptCode()).isEqualTo("PN-20261009-TEST");
    }

    @Test
    @DisplayName("createStockReceipt - Ném lỗi SUPPLIER_NOT_FOUND khi nhà cung cấp không tồn tại")
    void testCreateStockReceipt_SupplierNotFound() {
        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(99L)
                .importDate(LocalDate.now())
                .items(List.of(StockReceiptItemRequest.builder().bookId(101L).quantity(10).importPrice(50000L).build()))
                .build();

        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockReceiptService.createStockReceipt(1L, null, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.SUPPLIER_NOT_FOUND));
    }

    @Test
    @DisplayName("createStockReceipt - Ném lỗi VALIDATION_FAILED khi ngày nhập ở tương lai")
    void testCreateStockReceipt_FutureDate() {
        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(1L)
                .importDate(LocalDate.now().plusDays(2))
                .items(List.of(StockReceiptItemRequest.builder().bookId(101L).quantity(10).importPrice(50000L).build()))
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        assertThatThrownBy(() -> stockReceiptService.createStockReceipt(1L, null, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("createStockReceipt - Ném lỗi VALIDATION_FAILED khi trùng lặp bookId trong danh sách")
    void testCreateStockReceipt_DuplicateBookId() {
        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(1L)
                .importDate(LocalDate.now())
                .items(List.of(
                        StockReceiptItemRequest.builder().bookId(101L).quantity(10).importPrice(50000L).build(),
                        StockReceiptItemRequest.builder().bookId(101L).quantity(20).importPrice(50000L).build()
                ))
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));

        assertThatThrownBy(() -> stockReceiptService.createStockReceipt(1L, null, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("createStockReceipt - Trả cảnh báo warnings khi sách INACTIVE hoặc giá nhập >= giá bán")
    void testCreateStockReceipt_BusinessWarnings() {
        testBook.setStatus(BookStatus.INACTIVE);
        testBook.setSalePrice(40000L); // importPrice 47400 >= salePrice 40000

        CreateStockReceiptRequest request = CreateStockReceiptRequest.builder()
                .supplierId(1L)
                .importDate(LocalDate.now())
                .items(List.of(
                        StockReceiptItemRequest.builder().bookId(101L).quantity(10).importPrice(47400L).build()
                ))
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));
        when(stockReceiptRepository.save(any(StockReceipt.class))).thenAnswer(inv -> inv.getArgument(0));
        when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateStockReceiptResponse response = stockReceiptService.createStockReceipt(1L, null, request);

        assertThat(response).isNotNull();
        assertThat(response.getWarnings()).hasSize(2);
    }

    @Test
    @DisplayName("getStockReceipts - Thành công phân trang danh sách phiếu nhập kho")
    void testGetStockReceipts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(stockReceiptRepository.findAll(ArgumentMatchers.<Specification<StockReceipt>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testReceipt)));

        StockReceiptFilterRequest filter = StockReceiptFilterRequest.builder().keyword("PN-").build();
        PageResponse<StockReceiptSummaryResponse> response = stockReceiptService.getStockReceipts(filter, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getReceiptCode()).isEqualTo("PN-20261009-TEST");
    }

    @Test
    @DisplayName("getStockReceiptDetail - Thành công lấy chi tiết phiếu nhập")
    void testGetStockReceiptDetail_Success() {
        testReceipt.getItems().add(testBatch);
        when(stockReceiptRepository.findById(88L)).thenReturn(Optional.of(testReceipt));

        StockReceiptDetailResponse response = stockReceiptService.getStockReceiptDetail(88L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(88L);
        assertThat(response.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("getStockReceiptDetail - Ném lỗi STOCK_RECEIPT_NOT_FOUND khi ID không tồn tại")
    void testGetStockReceiptDetail_NotFound() {
        when(stockReceiptRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockReceiptService.getStockReceiptDetail(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.STOCK_RECEIPT_NOT_FOUND));
    }

    @Test
    @DisplayName("getBatches - Thành công lấy danh sách lô hàng theo FIFO")
    void testGetBatches_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(batchRepository.findAll(ArgumentMatchers.<Specification<Batch>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testBatch)));

        BatchFilterRequest filter = BatchFilterRequest.builder().bookId(101L).hasRemaining(true).build();
        PageResponse<BatchResponse> response = stockReceiptService.getBatches(filter, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getBatchCode()).isEqualTo("BATCH-PN-20261009-TEST-101");
    }
}

