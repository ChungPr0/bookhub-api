package com.chungpr0.bookhub.modules.inventory.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.inventory.dto.request.CreateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.SupplierFilterRequest;
import com.chungpr0.bookhub.modules.inventory.dto.request.UpdateSupplierRequest;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierDetailResponse;
import com.chungpr0.bookhub.modules.inventory.dto.response.SupplierResponse;
import org.springframework.data.domain.Pageable;

public interface SupplierService {

    PageResponse<SupplierResponse> getSuppliers(SupplierFilterRequest filter, Pageable pageable);

    SupplierDetailResponse getSupplierDetail(Long id);

    SupplierResponse createSupplier(CreateSupplierRequest request);

    SupplierResponse updateSupplier(Long id, UpdateSupplierRequest request);

    void deleteSupplier(Long id);
}

