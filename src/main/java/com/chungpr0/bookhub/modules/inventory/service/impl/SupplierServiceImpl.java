package com.chungpr0.bookhub.modules.inventory.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.SupplierFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.UpdateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierResponse;
import com.chungpr0.bookhub.modules.inventory.entity.Supplier;
import com.chungpr0.bookhub.modules.inventory.mapper.InventoryMapper;
import com.chungpr0.bookhub.modules.inventory.repository.StockReceiptRepository;
import com.chungpr0.bookhub.modules.inventory.repository.SupplierRepository;
import com.chungpr0.bookhub.modules.inventory.repository.specification.SupplierSpecification;
import com.chungpr0.bookhub.modules.inventory.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;
    private final StockReceiptRepository stockReceiptRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<SupplierResponse> getSuppliers(SupplierFilterRequest filter, Pageable pageable) {
        String keyword = (filter != null) ? filter.getKeyword() : null;
        Specification<Supplier> spec = SupplierSpecification.filter(keyword);
        Page<Supplier> page = supplierRepository.findAll(spec, pageable);

        List<SupplierResponse> items = new ArrayList<>();
        for (Supplier supplier : page.getContent()) {
            Long receiptCount = supplierRepository.countReceiptsBySupplierId(supplier.getId());
            Long totalImportCost = supplierRepository.sumTotalCostBySupplierId(supplier.getId());
            items.add(InventoryMapper.toSupplierResponse(supplier, receiptCount, totalImportCost));
        }

        return PageResponse.of(items, page);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierDetailResponse getSupplierDetail(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        Long receiptCount = supplierRepository.countReceiptsBySupplierId(id);
        Long totalImportCost = supplierRepository.sumTotalCostBySupplierId(id);
        return InventoryMapper.toSupplierDetailResponse(supplier, receiptCount, totalImportCost);
    }

    @Override
    @Transactional
    public SupplierResponse createSupplier(CreateSupplierRequest request) {
        String trimmedName = request.getName().trim();
        if (supplierRepository.existsByName(trimmedName)) {
            throw new AppException(ErrorCode.SUPPLIER_NAME_DUPLICATE);
        }

        Supplier supplier = Supplier.builder()
                .name(trimmedName)
                .contactName(StringUtils.hasText(request.getContactName()) ? request.getContactName().trim() : null)
                .phone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null)
                .email(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null)
                .address(StringUtils.hasText(request.getAddress()) ? request.getAddress().trim() : null)
                .taxCode(StringUtils.hasText(request.getTaxCode()) ? request.getTaxCode().trim() : null)
                .build();

        Supplier saved = supplierRepository.save(supplier);
        return InventoryMapper.toSupplierResponse(saved, 0L, 0L);
    }

    @Override
    @Transactional
    public SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        String trimmedName = request.getName().trim();
        if (supplierRepository.existsByNameAndIdNot(trimmedName, id)) {
            throw new AppException(ErrorCode.SUPPLIER_NAME_DUPLICATE);
        }

        supplier.setName(trimmedName);
        supplier.setContactName(StringUtils.hasText(request.getContactName()) ? request.getContactName().trim() : null);
        supplier.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        supplier.setEmail(StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null);
        supplier.setAddress(StringUtils.hasText(request.getAddress()) ? request.getAddress().trim() : null);
        supplier.setTaxCode(StringUtils.hasText(request.getTaxCode()) ? request.getTaxCode().trim() : null);

        Supplier saved = supplierRepository.save(supplier);
        Long receiptCount = supplierRepository.countReceiptsBySupplierId(id);
        Long totalImportCost = supplierRepository.sumTotalCostBySupplierId(id);
        return InventoryMapper.toSupplierResponse(saved, receiptCount, totalImportCost);
    }

    @Override
    @Transactional
    public void deleteSupplier(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SUPPLIER_NOT_FOUND));

        long receiptCount = stockReceiptRepository.countBySupplierId(id);
        if (receiptCount > 0) {
            throw new AppException(ErrorCode.SUPPLIER_HAS_RECEIPTS,
                    "Không thể xóa: Nhà cung cấp này đã có " + receiptCount + " phiếu nhập kho trong hệ thống");
        }

        supplierRepository.delete(supplier);
    }
}

