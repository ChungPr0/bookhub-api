package com.chungpr0.bookhub.modules.order.service;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.order.dto.request.CreateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ToggleVoucherStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.VoucherFilterRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AvailableVouchersResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherUsageResponse;
import org.springframework.data.domain.Pageable;

public interface VoucherService {

    AvailableVouchersResponse getAvailableVouchers(Long accountId, Long subtotal);

    PageResponse<VoucherResponse> searchVouchers(VoucherFilterRequest filter, Pageable pageable);

    VoucherResponse getVoucherById(Long id);

    VoucherResponse createVoucher(CreateVoucherRequest request);

    VoucherResponse updateVoucher(Long id, UpdateVoucherRequest request);

    VoucherResponse toggleStatus(Long id, ToggleVoucherStatusRequest request);

    void deleteVoucher(Long id);

    PageResponse<VoucherUsageResponse> getVoucherUsages(Long voucherId, Pageable pageable);
}

