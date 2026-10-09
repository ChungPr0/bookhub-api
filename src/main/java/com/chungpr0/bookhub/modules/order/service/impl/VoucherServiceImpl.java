package com.chungpr0.bookhub.modules.order.service.impl;

import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.common.util.DateTimeUtils;
import com.chungpr0.bookhub.modules.order.dto.request.CreateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ToggleVoucherStatusRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateVoucherRequest;
import com.chungpr0.bookhub.modules.order.dto.request.VoucherFilterRequest;
import com.chungpr0.bookhub.modules.order.dto.response.AvailableVouchersResponse;
import com.chungpr0.bookhub.modules.order.dto.response.UnusableVoucherResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherResponse;
import com.chungpr0.bookhub.modules.order.dto.response.VoucherUsageResponse;
import com.chungpr0.bookhub.modules.order.entity.Voucher;
import com.chungpr0.bookhub.modules.order.entity.VoucherUsage;
import com.chungpr0.bookhub.modules.order.enums.VoucherDiscountType;
import com.chungpr0.bookhub.modules.order.enums.VoucherStatus;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.VoucherRepository;
import com.chungpr0.bookhub.modules.order.repository.VoucherUsageRepository;
import com.chungpr0.bookhub.modules.order.repository.specification.VoucherSpecification;
import com.chungpr0.bookhub.modules.order.service.VoucherService;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final CustomerRepository customerRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional(readOnly = true)
    public AvailableVouchersResponse getAvailableVouchers(Long accountId, Long subtotal) {
        Customer customer = customerRepository.findByAccountId(accountId)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        OffsetDateTime now = DateTimeUtils.nowVietnam();
        long currentSubtotal = (subtotal != null && subtotal > 0L) ? subtotal : 0L;

        List<Voucher> allVouchers = voucherRepository.findAll();
        List<VoucherResponse> usable = new ArrayList<>();
        List<UnusableVoucherResponse> unusable = new ArrayList<>();

        for (Voucher v : allVouchers) {
            if (v.getStatus() != VoucherStatus.ACTIVE) {
                continue;
            }

            VoucherResponse vResp = orderMapper.toVoucherResponse(v, now, 0L);

            if (now.isBefore(v.getStartDate())) {
                unusable.add(UnusableVoucherResponse.builder()
                        .voucher(vResp)
                        .reasonCode("VOUCHER_NOT_YET_VALID")
                        .reasonMessage("Mã giảm giá chưa đến thời gian áp dụng")
                        .build());
                continue;
            }

            if (now.isAfter(v.getExpirationDate())) {
                unusable.add(UnusableVoucherResponse.builder()
                        .voucher(vResp)
                        .reasonCode("VOUCHER_EXPIRED")
                        .reasonMessage("Mã giảm giá đã hết hạn sử dụng")
                        .build());
                continue;
            }

            if (v.getUsageLimit() != null && v.getUsedCount() >= v.getUsageLimit()) {
                unusable.add(UnusableVoucherResponse.builder()
                        .voucher(vResp)
                        .reasonCode("VOUCHER_USAGE_LIMIT_REACHED")
                        .reasonMessage("Mã giảm giá đã hết lượt sử dụng trên toàn hệ thống")
                        .build());
                continue;
            }

            long customerUsageCount = voucherUsageRepository
                    .countByVoucherIdAndCustomerIdAndReleasedAtIsNull(v.getId(), customer.getId());
            if (customerUsageCount >= v.getUsageLimitPerCustomer()) {
                unusable.add(UnusableVoucherResponse.builder()
                        .voucher(vResp)
                        .reasonCode("VOUCHER_USAGE_LIMIT_REACHED")
                        .reasonMessage("Bạn đã sử dụng hết lượt cho phép của mã giảm giá này")
                        .build());
                continue;
            }

            if (currentSubtotal < v.getMinOrderAmount()) {
                long needed = v.getMinOrderAmount() - currentSubtotal;
                unusable.add(UnusableVoucherResponse.builder()
                        .voucher(vResp)
                        .reasonCode("VOUCHER_MIN_ORDER_NOT_MET")
                        .reasonMessage("Đơn hàng chưa đạt giá trị tối thiểu " + v.getMinOrderAmount() + "đ (còn thiếu " + needed + "đ)")
                        .build());
                continue;
            }

            // If eligible, calculate estimated discount
            long discount = calculateDiscount(v, currentSubtotal);
            VoucherResponse eligibleResp = orderMapper.toVoucherResponse(v, now, discount);
            usable.add(eligibleResp);
        }

        return AvailableVouchersResponse.builder()
                .usable(usable)
                .unusable(unusable)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VoucherResponse> searchVouchers(VoucherFilterRequest filter, Pageable pageable) {
        OffsetDateTime now = DateTimeUtils.nowVietnam();
        Page<Voucher> page = voucherRepository.findAll(
                VoucherSpecification.filter(
                        filter != null ? filter.getKeyword() : null,
                        filter != null ? filter.getStatus() : null,
                        filter != null ? filter.getDiscountType() : null
                ),
                pageable
        );

        List<VoucherResponse> items = page.getContent().stream()
                .map(v -> orderMapper.toVoucherResponse(v, now, 0L))
                .filter(resp -> {
                    if (filter != null && filter.getState() != null) {
                        return resp.getState() == filter.getState();
                    }
                    return true;
                })
                .toList();

        return PageResponse.of(items, page);
    }

    @Override
    @Transactional(readOnly = true)
    public VoucherResponse getVoucherById(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));
        return orderMapper.toVoucherResponse(voucher, DateTimeUtils.nowVietnam(), 0L);
    }

    @Override
    @Transactional
    public VoucherResponse createVoucher(CreateVoucherRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (voucherRepository.existsByCode(code)) {
            throw new AppException(ErrorCode.CONCURRENT_MODIFICATION, "Mã voucher đã tồn tại trong hệ thống");
        }

        if (request.getExpirationDate().isBefore(request.getStartDate())) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Thời gian hết hạn phải sau thời gian bắt đầu");
        }

        if (request.getDiscountType() == VoucherDiscountType.PERCENTAGE && request.getDiscountValue() > 100) {
            throw new AppException(ErrorCode.VALIDATION_FAILED, "Giá trị giảm theo phần trăm không được vượt quá 100%");
        }

        Voucher voucher = Voucher.builder()
                .code(code)
                .name(request.getName().trim())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : 0L)
                .usageLimit(request.getUsageLimit())
                .usageLimitPerCustomer(request.getUsageLimitPerCustomer() != null ? request.getUsageLimitPerCustomer() : 1)
                .usedCount(0)
                .startDate(request.getStartDate())
                .expirationDate(request.getExpirationDate())
                .status(VoucherStatus.ACTIVE)
                .version(0)
                .build();

        voucher = voucherRepository.save(voucher);
        return orderMapper.toVoucherResponse(voucher, DateTimeUtils.nowVietnam(), 0L);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(Long id, UpdateVoucherRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        // If usedCount > 0, financial fields are immutable
        if (voucher.getUsedCount() > 0) {
            if (request.getDiscountType() != null && request.getDiscountType() != voucher.getDiscountType()) {
                throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
            }
            if (request.getDiscountValue() != null && !request.getDiscountValue().equals(voucher.getDiscountValue())) {
                throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
            }
            if (request.getMaxDiscountAmount() != null && !request.getMaxDiscountAmount().equals(voucher.getMaxDiscountAmount())) {
                throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
            }
            if (request.getMinOrderAmount() != null && !request.getMinOrderAmount().equals(voucher.getMinOrderAmount())) {
                throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
            }
        } else {
            // Can update financial attributes if usedCount == 0
            if (request.getDiscountType() != null) {
                voucher.setDiscountType(request.getDiscountType());
            }
            if (request.getDiscountValue() != null) {
                if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE && request.getDiscountValue() > 100) {
                    throw new AppException(ErrorCode.VALIDATION_FAILED, "Giá trị giảm theo % không được vượt quá 100%");
                }
                voucher.setDiscountValue(request.getDiscountValue());
            }
            if (request.getMaxDiscountAmount() != null) {
                voucher.setMaxDiscountAmount(request.getMaxDiscountAmount());
            }
            if (request.getMinOrderAmount() != null) {
                voucher.setMinOrderAmount(request.getMinOrderAmount());
            }
        }

        if (request.getUsageLimit() != null) {
            if (request.getUsageLimit() < voucher.getUsedCount()) {
                throw new AppException(ErrorCode.VALIDATION_FAILED, "Giới hạn lượt dùng mới không được nhỏ hơn số lượt đã dùng hiện tại");
            }
            voucher.setUsageLimit(request.getUsageLimit());
        }

        if (request.getUsageLimitPerCustomer() != null) {
            voucher.setUsageLimitPerCustomer(request.getUsageLimitPerCustomer());
        }

        if (request.getStartDate() != null) {
            voucher.setStartDate(request.getStartDate());
        }
        voucher.setExpirationDate(request.getExpirationDate());
        voucher.setName(request.getName().trim());
        voucher.setDescription(request.getDescription());

        voucher = voucherRepository.save(voucher);
        return orderMapper.toVoucherResponse(voucher, DateTimeUtils.nowVietnam(), 0L);
    }

    @Override
    @Transactional
    public VoucherResponse toggleStatus(Long id, ToggleVoucherStatusRequest request) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        voucher.setStatus(request.getStatus());
        voucher = voucherRepository.save(voucher);
        return orderMapper.toVoucherResponse(voucher, DateTimeUtils.nowVietnam(), 0L);
    }

    @Override
    @Transactional
    public void deleteVoucher(Long id) {
        Voucher voucher = voucherRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.VOUCHER_NOT_FOUND));

        if (voucher.getUsedCount() > 0) {
            throw new AppException(ErrorCode.VOUCHER_ALREADY_USED);
        }

        voucherRepository.delete(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<VoucherUsageResponse> getVoucherUsages(Long voucherId, Pageable pageable) {
        if (!voucherRepository.existsById(voucherId)) {
            throw new AppException(ErrorCode.VOUCHER_NOT_FOUND);
        }

        Page<VoucherUsage> page = voucherUsageRepository.findByVoucherId(voucherId, pageable);
        return PageResponse.of(page, orderMapper::toVoucherUsageResponse);
    }

    private long calculateDiscount(Voucher voucher, long subtotal) {
        if (voucher.getDiscountType() == VoucherDiscountType.PERCENTAGE) {
            long discount = (subtotal * voucher.getDiscountValue()) / 100L;
            if (voucher.getMaxDiscountAmount() != null) {
                discount = Math.min(discount, voucher.getMaxDiscountAmount());
            }
            return discount;
        } else {
            return Math.min(voucher.getDiscountValue(), subtotal);
        }
    }
}
