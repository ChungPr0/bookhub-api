package com.chungpr0.bookhub.modules.order.service.impl;

import com.chungpr0.bookhub.common.exception.AppException;
import com.chungpr0.bookhub.common.exception.ErrorCode;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.order.dto.request.CalculateShippingRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ShippingCalculateItemRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateShippingConfigRequest;
import com.chungpr0.bookhub.modules.order.dto.response.CalculatedShippingOptionResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingCalculateResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingConfigResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingMilestoneResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingPackageInfo;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingServiceResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingTrackingResponse;
import com.chungpr0.bookhub.modules.order.dto.response.StaffOrAccountInfoResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.ShippingConfig;
import com.chungpr0.bookhub.modules.order.enums.DeliveryRegion;
import com.chungpr0.bookhub.modules.order.enums.ShippingServiceCode;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.ShippingConfigRepository;
import com.chungpr0.bookhub.modules.order.service.ShippingService;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final ShippingConfigRepository shippingConfigRepository;
    private final OrderRepository orderRepository;
    private final BookRepository bookRepository;
    private final CustomerRepository customerRepository;
    private final AddressRepository addressRepository;
    private final AccountRepository accountRepository;
    private final OrderMapper orderMapper;

    @Override
    public List<ShippingServiceResponse> getShippingServices() {
        ShippingConfig config = getOrCreateConfig();
        return List.of(
                ShippingServiceResponse.builder()
                        .code(ShippingServiceCode.STANDARD)
                        .name("Giao hàng tiêu chuẩn")
                        .description("Chuyển phát đường bộ an toàn, tiết kiệm chi phí tối đa")
                        .baseFee(config.getStandardBaseFee())
                        .estimatedDaysMin(2)
                        .estimatedDaysMax(4)
                        .supportedRegions("ALL")
                        .build(),
                ShippingServiceResponse.builder()
                        .code(ShippingServiceCode.EXPRESS)
                        .name("Giao hàng nhanh")
                        .description("Ưu tiên chuyển phát đường hàng không, rút ngắn thời gian")
                        .baseFee(config.getExpressBaseFee())
                        .estimatedDaysMin(1)
                        .estimatedDaysMax(2)
                        .supportedRegions("ALL")
                        .build(),
                ShippingServiceResponse.builder()
                        .code(ShippingServiceCode.SAME_DAY)
                        .name("Hỏa tốc 4 giờ")
                        .description("Giao tức thì trong ngày bằng xe máy cho độc giả nội thành")
                        .baseFee(config.getSameDayBaseFee())
                        .estimatedDaysMin(0)
                        .estimatedDaysMax(1)
                        .supportedRegions("INNER_CITY_ONLY")
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingCalculateResponse calculateShipping(Long accountId, CalculateShippingRequest request) {
        ShippingConfig config = getOrCreateConfig();

        String province = "";
        if (request.getAddressId() != null) {
            Address address = addressRepository.findById(request.getAddressId())
                    .orElseThrow(() -> new AppException(ErrorCode.ADDRESS_NOT_FOUND));
            if (accountId != null) {
                Customer customer = customerRepository.findByAccountId(accountId)
                        .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
                if (!address.getCustomer().getId().equals(customer.getId())) {
                    throw new AppException(ErrorCode.ADDRESS_NOT_FOUND);
                }
            }
            province = address.getProvince();
        } else if (request.getShippingAddress() != null) {
            province = request.getShippingAddress().getProvince();
        }

        DeliveryRegion region = isInnerCity(province) ? DeliveryRegion.INNER_CITY : DeliveryRegion.OUTER_CITY;

        int totalItems = 0;
        int totalWeightGram = 0;
        long subtotal = 0L;

        for (ShippingCalculateItemRequest item : request.getItems()) {
            Book book = bookRepository.findById(item.getBookId())
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_FOUND));
            int qty = item.getQuantity();
            totalItems += qty;
            int weight = (book.getWeightGram() != null && book.getWeightGram() > 0) ? book.getWeightGram() : 300;
            totalWeightGram += (weight * qty);
            subtotal += (book.getSalePrice() * qty);
        }

        boolean freeShippingEligible = subtotal >= config.getFreeShippingThreshold();
        long amountToFreeShipping = Math.max(0L, config.getFreeShippingThreshold() - subtotal);

        // Overweight calculation
        long overweightFee = 0L;
        if (totalWeightGram > config.getStandardMaxWeightGram()) {
            int extraWeight = totalWeightGram - config.getStandardMaxWeightGram();
            int units = (extraWeight + config.getOverweightUnitGram() - 1) / config.getOverweightUnitGram();
            overweightFee = units * config.getOverweightSurcharge();
        }

        ShippingPackageInfo packageInfo = ShippingPackageInfo.builder()
                .totalItems(totalItems)
                .totalWeightGram(totalWeightGram)
                .region(region)
                .isFreeShippingEligible(freeShippingEligible)
                .amountToFreeShipping(amountToFreeShipping)
                .build();

        List<CalculatedShippingOptionResponse> services = new ArrayList<>();

        // 1. STANDARD
        long standardDiscount = freeShippingEligible ? config.getStandardBaseFee() : 0L;
        long standardFinal = Math.max(0L, config.getStandardBaseFee() + overweightFee - standardDiscount);
        services.add(CalculatedShippingOptionResponse.builder()
                .code(ShippingServiceCode.STANDARD)
                .name("Giao hàng tiêu chuẩn")
                .baseFee(config.getStandardBaseFee())
                .overweightFee(overweightFee)
                .discountFee(standardDiscount)
                .finalFee(standardFinal)
                .estimatedDaysMin(2)
                .estimatedDaysMax(4)
                .build());

        // 2. EXPRESS
        long expressDiscount = freeShippingEligible ? config.getMaxFreeShippingSubsidy() : 0L;
        long expressFinal = Math.max(0L, config.getExpressBaseFee() + overweightFee - expressDiscount);
        services.add(CalculatedShippingOptionResponse.builder()
                .code(ShippingServiceCode.EXPRESS)
                .name("Giao hàng nhanh")
                .baseFee(config.getExpressBaseFee())
                .overweightFee(overweightFee)
                .discountFee(expressDiscount)
                .finalFee(expressFinal)
                .estimatedDaysMin(1)
                .estimatedDaysMax(2)
                .build());

        // 3. SAME_DAY (only if INNER_CITY)
        if (region == DeliveryRegion.INNER_CITY) {
            long sameDayDiscount = freeShippingEligible ? config.getMaxFreeShippingSubsidy() : 0L;
            long sameDayFinal = Math.max(0L, config.getSameDayBaseFee() + overweightFee - sameDayDiscount);
            services.add(CalculatedShippingOptionResponse.builder()
                    .code(ShippingServiceCode.SAME_DAY)
                    .name("Hỏa tốc 4 giờ")
                    .baseFee(config.getSameDayBaseFee())
                    .overweightFee(overweightFee)
                    .discountFee(sameDayDiscount)
                    .finalFee(sameDayFinal)
                    .estimatedDaysMin(0)
                    .estimatedDaysMax(1)
                    .build());
        }

        return ShippingCalculateResponse.builder()
                .packageInfo(packageInfo)
                .services(services)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingTrackingResponse getTracking(String orderCode) {
        Order order = orderRepository.findByOrderCode(orderCode)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<ShippingMilestoneResponse> events = new ArrayList<>();
        OffsetDateTime created = order.getCreatedAt();
        events.add(ShippingMilestoneResponse.builder()
                .status("ORDER_CREATED")
                .title("Đơn hàng đã được khởi tạo")
                .description("Hệ thống BookHub ghi nhận đơn hàng thành công")
                .location("Hệ thống BookHub")
                .timestamp(created)
                .build());

        if (order.getConfirmedAt() != null) {
            events.add(ShippingMilestoneResponse.builder()
                    .status("PACKAGE_PACKED")
                    .title("Đã đóng gói kiện hàng")
                    .description("Nhân viên kho đã kiểm tra sách và dán tem niêm phong kiện hàng")
                    .location("Tổng kho BookHub - Hà Nội")
                    .timestamp(order.getConfirmedAt())
                    .build());
        }

        if (order.getShippedAt() != null) {
            events.add(ShippingMilestoneResponse.builder()
                    .status("HANDED_OVER")
                    .title("Đã bàn giao cho đơn vị vận chuyển")
                    .description("Bưu tá bưu cục đã tiếp nhận kiện hàng vào hệ thống")
                    .location("Bưu cục phát vận chuyển")
                    .timestamp(order.getShippedAt())
                    .build());

            events.add(ShippingMilestoneResponse.builder()
                    .status("IN_TRANSIT")
                    .title("Đang vận chuyển")
                    .description("Kiện hàng đang được trung chuyển tới địa chỉ nhận của bạn")
                    .location("Kho trung chuyển")
                    .timestamp(order.getShippedAt().plusHours(2))
                    .build());
        }

        if (order.getCompletedAt() != null) {
            events.add(ShippingMilestoneResponse.builder()
                    .status("DELIVERED")
                    .title("Giao hàng thành công")
                    .description("Khách hàng đã nhận kiện hàng thành công")
                    .location(order.getShippingAddress())
                    .timestamp(order.getCompletedAt())
                    .build());
        }

        return ShippingTrackingResponse.builder()
                .orderCode(order.getOrderCode())
                .currentStatus(order.getStatus().name())
                .carrier("Giao Hàng Nhanh (GHN)")
                .trackingNumber("GHN-" + order.getOrderCode().replace("ORD-", ""))
                .events(events)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ShippingConfigResponse getShippingConfig() {
        ShippingConfig config = getOrCreateConfig();
        StaffOrAccountInfoResponse updatedBy = null;
        if (config.getUpdatedByAccountId() != null) {
            Optional<Account> accOpt = accountRepository.findById(config.getUpdatedByAccountId());
            if (accOpt.isPresent()) {
                Account acc = accOpt.get();
                updatedBy = StaffOrAccountInfoResponse.builder()
                        .id(acc.getId())
                        .fullName(acc.getUsername())
                        .role(acc.getRole().name())
                        .build();
            }
        }
        return orderMapper.toShippingConfigResponse(config, updatedBy);
    }

    @Override
    @Transactional
    public ShippingConfigResponse updateShippingConfig(Long accountId, UpdateShippingConfigRequest request) {
        ShippingConfig config = getOrCreateConfig();

        config.setStandardBaseFee(request.getStandardBaseFee());
        config.setExpressBaseFee(request.getExpressBaseFee());
        config.setSameDayBaseFee(request.getSameDayBaseFee());
        config.setFreeShippingThreshold(request.getFreeShippingThreshold());
        config.setMaxFreeShippingSubsidy(request.getMaxFreeShippingSubsidy());
        config.setStandardMaxWeightGram(request.getStandardMaxWeightGram());
        config.setOverweightUnitGram(request.getOverweightUnitGram());
        config.setOverweightSurcharge(request.getOverweightSurcharge());
        config.setUpdatedByAccountId(accountId);

        config = shippingConfigRepository.save(config);

        StaffOrAccountInfoResponse updatedBy = null;
        if (accountId != null) {
            Optional<Account> accOpt = accountRepository.findById(accountId);
            if (accOpt.isPresent()) {
                Account acc = accOpt.get();
                updatedBy = StaffOrAccountInfoResponse.builder()
                        .id(acc.getId())
                        .fullName(acc.getUsername())
                        .role(acc.getRole().name())
                        .build();
            }
        }

        return orderMapper.toShippingConfigResponse(config, updatedBy);
    }

    private ShippingConfig getOrCreateConfig() {
        return shippingConfigRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> shippingConfigRepository.save(ShippingConfig.builder()
                        .standardBaseFee(30000L)
                        .expressBaseFee(45000L)
                        .sameDayBaseFee(60000L)
                        .freeShippingThreshold(300000L)
                        .maxFreeShippingSubsidy(30000L)
                        .standardMaxWeightGram(2000)
                        .overweightUnitGram(500)
                        .overweightSurcharge(5000L)
                        .build()));
    }

    private boolean isInnerCity(String province) {
        if (!StringUtils.hasText(province)) {
            return false;
        }
        String p = province.toLowerCase();
        return p.contains("hà nội") || p.contains("hồ chí minh") || p.contains("tp.hcm") || p.contains("tphcm");
    }
}
