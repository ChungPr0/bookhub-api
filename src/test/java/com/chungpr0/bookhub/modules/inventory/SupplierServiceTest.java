package com.chungpr0.bookhub.modules.inventory;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.SupplierFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.UpdateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierResponse;
import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import com.chungpr0.bookhub.modules.inventory.repository.StockReceiptRepository;
import com.chungpr0.bookhub.modules.inventory.repository.SupplierRepository;
import com.chungpr0.bookhub.modules.inventory.service.impl.SupplierServiceImpl;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private StockReceiptRepository stockReceiptRepository;

    @InjectMocks
    private SupplierServiceImpl supplierService;

    private Supplier testSupplier;

    @BeforeEach
    void setUp() {
        testSupplier = Supplier.builder()
                .id(1L)
                .name("Công ty Nhã Nam")
                .contactName("Trần Văn Nam")
                .phone("02435146875")
                .email("kinhdoanh@nhanam.vn")
                .address("59 Đỗ Quang, Hà Nội")
                .taxCode("0101824123")
                .build();
    }

    @Test
    @DisplayName("getSuppliers - Thành công phân trang và tìm kiếm nhà cung cấp")
    void testGetSuppliers_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        when(supplierRepository.findAll(ArgumentMatchers.<Specification<Supplier>>any(), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testSupplier)));
        when(supplierRepository.countReceiptsBySupplierId(1L)).thenReturn(5L);
        when(supplierRepository.sumTotalCostBySupplierId(1L)).thenReturn(10000000L);

        SupplierFilterRequest filter = SupplierFilterRequest.builder().keyword("Nhã Nam").build();
        PageResponse<SupplierResponse> response = supplierService.getSuppliers(filter, pageable);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getName()).isEqualTo("Công ty Nhã Nam");
        assertThat(response.getItems().get(0).getReceiptCount()).isEqualTo(5L);
        assertThat(response.getItems().get(0).getTotalImportCost()).isEqualTo(10000000L);
    }

    @Test
    @DisplayName("getSupplierDetail - Thành công lấy chi tiết nhà cung cấp")
    void testGetSupplierDetail_Success() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.countReceiptsBySupplierId(1L)).thenReturn(5L);
        when(supplierRepository.sumTotalCostBySupplierId(1L)).thenReturn(10000000L);

        SupplierDetailResponse detail = supplierService.getSupplierDetail(1L);

        assertThat(detail).isNotNull();
        assertThat(detail.getId()).isEqualTo(1L);
        assertThat(detail.getName()).isEqualTo("Công ty Nhã Nam");
        assertThat(detail.getReceiptCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("getSupplierDetail - Ném lỗi SUPPLIER_NOT_FOUND khi ID không tồn tại")
    void testGetSupplierDetail_NotFound() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> supplierService.getSupplierDetail(99L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.SUPPLIER_NOT_FOUND));
    }

    @Test
    @DisplayName("createSupplier - Thành công tạo mới nhà cung cấp")
    void testCreateSupplier_Success() {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .name("Alpha Books")
                .contactName("Nguyễn Hoàng Anh")
                .phone("02437226234")
                .email("contact@alphabooks.vn")
                .build();

        when(supplierRepository.existsByName("Alpha Books")).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenAnswer(invocation -> {
            Supplier s = invocation.getArgument(0);
            s.setId(2L);
            return s;
        });

        SupplierResponse response = supplierService.createSupplier(request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getName()).isEqualTo("Alpha Books");
    }

    @Test
    @DisplayName("createSupplier - Ném lỗi SUPPLIER_NAME_DUPLICATE khi tên đã tồn tại")
    void testCreateSupplier_DuplicateName() {
        CreateSupplierRequest request = CreateSupplierRequest.builder()
                .name("Công ty Nhã Nam")
                .build();

        when(supplierRepository.existsByName("Công ty Nhã Nam")).thenReturn(true);

        assertThatThrownBy(() -> supplierService.createSupplier(request))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.SUPPLIER_NAME_DUPLICATE));
    }

    @Test
    @DisplayName("updateSupplier - Thành công cập nhật nhà cung cấp")
    void testUpdateSupplier_Success() {
        UpdateSupplierRequest request = UpdateSupplierRequest.builder()
                .name("Nhã Nam (Updated)")
                .contactName("Trần Văn Nam")
                .phone("02435146875")
                .build();

        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(supplierRepository.existsByNameAndIdNot("Nhã Nam (Updated)", 1L)).thenReturn(false);
        when(supplierRepository.save(any(Supplier.class))).thenReturn(testSupplier);
        when(supplierRepository.countReceiptsBySupplierId(1L)).thenReturn(5L);
        when(supplierRepository.sumTotalCostBySupplierId(1L)).thenReturn(10000000L);

        SupplierResponse response = supplierService.updateSupplier(1L, request);

        assertThat(response).isNotNull();
        assertThat(testSupplier.getName()).isEqualTo("Nhã Nam (Updated)");
    }

    @Test
    @DisplayName("deleteSupplier - Ném lỗi SUPPLIER_HAS_RECEIPTS khi nhà cung cấp đã có phiếu nhập")
    void testDeleteSupplier_HasReceipts() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(stockReceiptRepository.countBySupplierId(1L)).thenReturn(3L);

        assertThatThrownBy(() -> supplierService.deleteSupplier(1L))
                .isInstanceOf(AppException.class)
                .satisfies(ex -> assertThat(((AppException) ex).getErrorCode()).isEqualTo(ErrorCode.SUPPLIER_HAS_RECEIPTS));
    }

    @Test
    @DisplayName("deleteSupplier - Thành công xóa nhà cung cấp khi chưa có phiếu nhập")
    void testDeleteSupplier_Success() {
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(testSupplier));
        when(stockReceiptRepository.countBySupplierId(1L)).thenReturn(0L);

        supplierService.deleteSupplier(1L);

        verify(supplierRepository).delete(testSupplier);
    }
}

