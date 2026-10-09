package com.chungpr0.bookhub.modules.inventory;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.inventory.dto.request.AdjustInventoryRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.InventoryTransactionFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.LowStockFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryAdjustmentResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.InventoryTransactionPageResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.LowStockPageResponse;
import com.chungpr0.bookhub.modules.inventory.entity.Batch;
import com.chungpr0.bookhub.modules.inventory.entity.InventoryTransaction;
import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentReason;
import com.chungpr0.bookhub.modules.inventory.enums.AdjustmentType;
import com.chungpr0.bookhub.modules.inventory.enums.InventoryTransactionType;
import com.chungpr0.bookhub.modules.inventory.repository.BatchRepository;
import com.chungpr0.bookhub.modules.inventory.repository.InventoryTransactionRepository;
import com.chungpr0.bookhub.modules.inventory.service.impl.InventoryServiceImpl;
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
class InventoryServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BatchRepository batchRepository;

    @Mock
    private InventoryTransactionRepository inventoryTransactionRepository;

    @Mock
    private StaffRepository staffRepository;

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    private Book testBook;
    private Batch testBatch;

    @BeforeEach
    void setUp() {
        testBook = Book.builder()
                .id(101L)
                .title("Nhà Giả Kim")
                .stockQuantity(84)
                .lowStockThreshold(10)
                .soldCount(60)
                .status(BookStatus.ACTIVE)
                .build();

        testBatch = Batch.builder()
                .id(501L)
                .batchCode("BATCH-PN-20261009-TEST-101")
                .book(testBook)
                .importPrice(47400L)
                .quantityImported(200)
                .quantityRemaining(84)
                .importDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("adjustInventory - Thành công ADJUST_OUT giảm tồn kho và trừ theo FIFO")
    void testAdjustInventory_AdjustOut_Success() {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(101L)
                .type(AdjustmentType.ADJUST_OUT)
                .quantity(5)
                .reason(AdjustmentReason.DAMAGED)
                .note("Sách bị dính nước mưa")
                .build();

        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));
        when(batchRepository.findByBookIdAndQuantityRemainingGreaterThanOrderByImportDateAscIdAsc(101L, 0))
                .thenReturn(new ArrayList<>(List.of(testBatch)));
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class))).thenAnswer(inv -> {
            InventoryTransaction tx = inv.getArgument(0);
            tx.setId(701L);
            return tx;
        });

        InventoryAdjustmentResponse response = inventoryService.adjustInventory(1L, null, request);

        assertThat(response).isNotNull();
        assertThat(response.getStockBefore()).isEqualTo(84);
        assertThat(response.getStockAfter()).isEqualTo(79);
        assertThat(response.getAffectedBatches()).hasSize(1);
        assertThat(response.getAffectedBatches().get(0).getQuantityDeducted()).isEqualTo(5);
        assertThat(testBatch.getQuantityRemaining()).isEqualTo(79);
        assertThat(testBook.getStockQuantity()).isEqualTo(79);

        verify(bookRepository).save(testBook);
        verify(batchRepository).save(testBatch);
    }

    @Test
    @DisplayName("adjustInventory - Ném lỗi ADJUSTMENT_EXCEEDS_STOCK khi giảm vượt tồn kho hiện có")
    void testAdjustInventory_ExceedsStock() {
        testBook.setStockQuantity(2);
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(101L)
                .type(AdjustmentType.ADJUST_OUT)
                .quantity(5)
                .reason(AdjustmentReason.DAMAGED)
                .build();

        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> inventoryService.adjustInventory(1L, null, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.ADJUSTMENT_EXCEEDS_STOCK));
    }

    @Test
    @DisplayName("adjustInventory - Ném lỗi VALIDATION_FAILED khi reason OTHER nhưng không có ghi chú")
    void testAdjustInventory_OtherReasonWithoutNote() {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(101L)
                .type(AdjustmentType.ADJUST_OUT)
                .quantity(1)
                .reason(AdjustmentReason.OTHER)
                .note("")
                .build();

        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> inventoryService.adjustInventory(1L, null, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("adjustInventory - Thành công ADJUST_IN tăng tồn kho và tạo lô điều chỉnh")
    void testAdjustInventory_AdjustIn_Success() {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(101L)
                .type(AdjustmentType.ADJUST_IN)
                .quantity(10)
                .reason(AdjustmentReason.FOUND)
                .importPrice(50000L)
                .build();

        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));
        when(batchRepository.save(any(Batch.class))).thenAnswer(inv -> {
            Batch b = inv.getArgument(0);
            b.setId(601L);
            return b;
        });
        when(inventoryTransactionRepository.save(any(InventoryTransaction.class))).thenAnswer(inv -> {
            InventoryTransaction tx = inv.getArgument(0);
            tx.setId(702L);
            return tx;
        });

        InventoryAdjustmentResponse response = inventoryService.adjustInventory(1L, null, request);

        assertThat(response).isNotNull();
        assertThat(response.getStockBefore()).isEqualTo(84);
        assertThat(response.getStockAfter()).isEqualTo(94);
        assertThat(testBook.getStockQuantity()).isEqualTo(94);

        verify(batchRepository).save(any(Batch.class));
        verify(bookRepository).save(testBook);
    }

    @Test
    @DisplayName("adjustInventory - Ném lỗi VALIDATION_FAILED khi ADJUST_IN không có đơn giá nhập")
    void testAdjustInventory_AdjustInWithoutPrice() {
        AdjustInventoryRequest request = AdjustInventoryRequest.builder()
                .bookId(101L)
                .type(AdjustmentType.ADJUST_IN)
                .quantity(10)
                .reason(AdjustmentReason.FOUND)
                .importPrice(null)
                .build();

        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));

        assertThatThrownBy(() -> inventoryService.adjustInventory(1L, null, request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.VALIDATION_FAILED));
    }

    @Test
    @DisplayName("getTransactions - Thành công tra cứu thẻ kho kèm tổng kết số liệu")
    void testGetTransactions_SuccessWithSummary() {
        Pageable pageable = PageRequest.of(0, 10);
        InventoryTransaction tx = InventoryTransaction.builder()
                .id(992L)
                .book(testBook)
                .type(InventoryTransactionType.ADJUST_OUT)
                .quantity(-5)
                .stockAfter(79)
                .reason("DAMAGED")
                .createdBy(1L)
                .build();

        when(inventoryTransactionRepository.findAll(ArgumentMatchers.<Specification<InventoryTransaction>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(tx)));
        when(bookRepository.findById(101L)).thenReturn(Optional.of(testBook));
        when(inventoryTransactionRepository.findByBookId(101L)).thenReturn(List.of(tx));

        InventoryTransactionFilterRequest filter = InventoryTransactionFilterRequest.builder().bookId(101L).build();
        InventoryTransactionPageResponse response = inventoryService.getTransactions(filter, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getSummary()).isNotNull();
        assertThat(response.getSummary().getBookTitle()).isEqualTo("Nhà Giả Kim");
        assertThat(response.getSummary().getClosingStock()).isEqualTo(84);
    }

    @Test
    @DisplayName("getLowStockBooks - Thành công phân trang và đếm tổng cảnh báo tồn kho")
    void testGetLowStockBooks_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(bookRepository.count(ArgumentMatchers.<Specification<Book>>any())).thenReturn(5L);
        when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testBook)));
        when(batchRepository.findLatestByBookId(101L, PageRequest.of(0, 1))).thenReturn(List.of(testBatch));

        LowStockFilterRequest filter = LowStockFilterRequest.builder().stockStatus("LOW_STOCK").build();
        LowStockPageResponse response = inventoryService.getLowStockBooks(filter, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getStockQuantity()).isEqualTo(84);
        assertThat(response.getSummary().getLowStockCount()).isEqualTo(5L);
    }
}

