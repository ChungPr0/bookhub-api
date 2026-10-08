package com.chungpr0.bookhub.modules.order;

import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Book;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.order.dto.request.CalculateShippingRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ShippingAddressRequest;
import com.chungpr0.bookhub.modules.order.dto.request.ShippingCalculateItemRequest;
import com.chungpr0.bookhub.modules.order.dto.request.UpdateShippingConfigRequest;
import com.chungpr0.bookhub.modules.order.dto.response.CalculatedShippingOptionResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingCalculateResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingConfigResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingServiceResponse;
import com.chungpr0.bookhub.modules.order.dto.response.ShippingTrackingResponse;
import com.chungpr0.bookhub.modules.order.entity.Order;
import com.chungpr0.bookhub.modules.order.entity.ShippingConfig;
import com.chungpr0.bookhub.modules.order.enums.DeliveryRegion;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import com.chungpr0.bookhub.modules.order.enums.ShippingServiceCode;
import com.chungpr0.bookhub.modules.order.mapper.OrderMapper;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import com.chungpr0.bookhub.modules.order.repository.ShippingConfigRepository;
import com.chungpr0.bookhub.modules.order.service.impl.ShippingServiceImpl;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShippingServiceTest {

    @Mock
    private ShippingConfigRepository shippingConfigRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private AccountRepository accountRepository;

    @Spy
    private OrderMapper orderMapper = new OrderMapper();

    @InjectMocks
    private ShippingServiceImpl shippingService;

    private ShippingConfig defaultConfig;
    private Book standardBook;

    @BeforeEach
    void setUp() {
        defaultConfig = ShippingConfig.builder()
                .id(1L)
                .standardBaseFee(30_000L)
                .expressBaseFee(45_000L)
                .sameDayBaseFee(60_000L)
                .freeShippingThreshold(300_000L)
                .maxFreeShippingSubsidy(30_000L)
                .standardMaxWeightGram(2000)
                .overweightUnitGram(500)
                .overweightSurcharge(5_000L)
                .build();

        standardBook = Book.builder()
                .id(1001L)
                .title("Lập trình Java hiện đại")
                .salePrice(100_000L)
                .weightGram(400)
                .build();
    }

    @Test
    @DisplayName("Get Shipping Services - Trả về đủ 3 gói dịch vụ giao hàng tiêu chuẩn, nhanh, hỏa tốc")
    void testGetShippingServices() {
        when(shippingConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));

        List<ShippingServiceResponse> services = shippingService.getShippingServices();

        assertThat(services).hasSize(3);
        List<ShippingServiceCode> codes = new ArrayList<>();
        for (ShippingServiceResponse item : services) {
            codes.add(item.getCode());
        }
        assertThat(codes).containsExactlyInAnyOrder(ShippingServiceCode.STANDARD, ShippingServiceCode.EXPRESS, ShippingServiceCode.SAME_DAY);
    }

    @Test
    @DisplayName("Calculate Shipping - Nội thành dưới ngưỡng freeship có thêm gói hỏa tốc")
    void testCalculateShipping_InnerCity_BelowThreshold() {
        when(shippingConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(bookRepository.findById(1001L)).thenReturn(Optional.of(standardBook));

        ShippingAddressRequest addressRequest = ShippingAddressRequest.builder()
                .fullName("Tiến Chung")
                .phone("0988888888")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng")
                .detailAddress("123 Xuân Thủy")
                .build();

        CalculateShippingRequest request = CalculateShippingRequest.builder()
                .shippingAddress(addressRequest)
                .items(List.of(
                        ShippingCalculateItemRequest.builder().bookId(1001L).quantity(2).build()
                ))
                .build();

        ShippingCalculateResponse response = shippingService.calculateShipping(null, request);

        assertThat(response.getPackageInfo().getRegion()).isEqualTo(DeliveryRegion.INNER_CITY);
        assertThat(response.getPackageInfo().isFreeShippingEligible()).isFalse();
        assertThat(response.getPackageInfo().getTotalWeightGram()).isEqualTo(800);
        assertThat(response.getPackageInfo().getAmountToFreeShipping()).isEqualTo(100_000L); // 300k - 200k = 100k

        assertThat(response.getServices()).hasSize(3); // Bao gồm cả SAME_DAY vì là nội thành Hà Nội

        CalculatedShippingOptionResponse standardOpt = response.getServices().stream()
                .filter(s -> s.getCode() == ShippingServiceCode.STANDARD)
                .findFirst().orElseThrow();
        assertThat(standardOpt.getBaseFee()).isEqualTo(30_000L);
        assertThat(standardOpt.getOverweightFee()).isEqualTo(0L);
        assertThat(standardOpt.getDiscountFee()).isEqualTo(0L);
        assertThat(standardOpt.getFinalFee()).isEqualTo(30_000L);
    }

    @Test
    @DisplayName("Calculate Shipping - Ngoại thành không hỗ trợ hỏa tốc")
    void testCalculateShipping_OuterCity_NoSameDay() {
        when(shippingConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(bookRepository.findById(1001L)).thenReturn(Optional.of(standardBook));

        ShippingAddressRequest addressRequest = ShippingAddressRequest.builder()
                .province("Đà Nẵng")
                .build();

        CalculateShippingRequest request = CalculateShippingRequest.builder()
                .shippingAddress(addressRequest)
                .items(List.of(
                        ShippingCalculateItemRequest.builder().bookId(1001L).quantity(1).build()
                ))
                .build();

        ShippingCalculateResponse response = shippingService.calculateShipping(null, request);

        assertThat(response.getPackageInfo().getRegion()).isEqualTo(DeliveryRegion.OUTER_CITY);
        assertThat(response.getServices()).hasSize(2); // Chỉ có STANDARD và EXPRESS
        assertThat(response.getServices()).noneMatch(s -> s.getCode() == ShippingServiceCode.SAME_DAY);
    }

    @Test
    @DisplayName("Calculate Shipping - Đơn đạt ngưỡng 300k được miễn phí vận chuyển tiêu chuẩn")
    void testCalculateShipping_FreeShippingEligible() {
        when(shippingConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(bookRepository.findById(1001L)).thenReturn(Optional.of(standardBook));

        ShippingAddressRequest addressRequest = ShippingAddressRequest.builder()
                .province("Hà Nội")
                .build();

        CalculateShippingRequest request = CalculateShippingRequest.builder()
                .shippingAddress(addressRequest)
                .items(List.of(
                        ShippingCalculateItemRequest.builder().bookId(1001L).quantity(4).build() // 4 x 100k = 400k > 300k
                ))
                .build();

        ShippingCalculateResponse response = shippingService.calculateShipping(null, request);

        assertThat(response.getPackageInfo().isFreeShippingEligible()).isTrue();
        assertThat(response.getPackageInfo().getAmountToFreeShipping()).isEqualTo(0L);

        CalculatedShippingOptionResponse standardOpt = response.getServices().stream()
                .filter(s -> s.getCode() == ShippingServiceCode.STANDARD)
                .findFirst().orElseThrow();
        assertThat(standardOpt.getDiscountFee()).isEqualTo(30_000L);
        assertThat(standardOpt.getFinalFee()).isEqualTo(0L); // Miễn phí tiêu chuẩn

        CalculatedShippingOptionResponse expressOpt = response.getServices().stream()
                .filter(s -> s.getCode() == ShippingServiceCode.EXPRESS)
                .findFirst().orElseThrow();
        assertThat(expressOpt.getDiscountFee()).isEqualTo(30_000L); // Trợ giá tối đa 30k
        assertThat(expressOpt.getFinalFee()).isEqualTo(15_000L); // 45k - 30k = 15k
    }

    @Test
    @DisplayName("Calculate Shipping - Phụ phí quá cân cho kiện hàng nặng trên 2000g")
    void testCalculateShipping_OverweightSurcharge() {
        Book heavyBook = Book.builder()
                .id(1002L)
                .title("Đại từ điển tiếng Việt")
                .salePrice(250_000L)
                .weightGram(1600) // 2 cuốn = 3200g (vượt 1200g -> 3 đơn vị 500g -> +15,000 VND)
                .build();

        when(shippingConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(bookRepository.findById(1002L)).thenReturn(Optional.of(heavyBook));

        ShippingAddressRequest addressRequest = ShippingAddressRequest.builder()
                .province("Hồ Chí Minh")
                .build();

        CalculateShippingRequest request = CalculateShippingRequest.builder()
                .shippingAddress(addressRequest)
                .items(List.of(
                        ShippingCalculateItemRequest.builder().bookId(1002L).quantity(2).build()
                ))
                .build();

        ShippingCalculateResponse response = shippingService.calculateShipping(null, request);

        assertThat(response.getPackageInfo().getTotalWeightGram()).isEqualTo(3200);
        CalculatedShippingOptionResponse standardOpt = response.getServices().stream()
                .filter(s -> s.getCode() == ShippingServiceCode.STANDARD)
                .findFirst().orElseThrow();

        assertThat(standardOpt.getOverweightFee()).isEqualTo(15_000L);
        // Subtotal = 500k -> freeship giảm 30k base -> finalFee = 30k base + 15k quá cân - 30k giảm = 15k
        assertThat(standardOpt.getFinalFee()).isEqualTo(15_000L);
    }

    @Test
    @DisplayName("Get Tracking - Trả về đầy đủ các mốc vận chuyển của đơn hàng")
    void testGetTracking_Success() {
        OffsetDateTime now = OffsetDateTime.now();
        Order order = Order.builder()
                .orderCode("ORD-20261008-TRK")
                .status(OrderStatus.COMPLETED)
                .shippingAddress("123 Cầu Giấy, Hà Nội")
                .build();
        order.setCreatedAt(now.minusDays(3));
        order.setConfirmedAt(now.minusDays(2));
        order.setShippedAt(now.minusDays(1));
        order.setCompletedAt(now);

        when(orderRepository.findByOrderCode("ORD-20261008-TRK")).thenReturn(Optional.of(order));

        ShippingTrackingResponse response = shippingService.getTracking("ORD-20261008-TRK");

        assertThat(response.getOrderCode()).isEqualTo("ORD-20261008-TRK");
        assertThat(response.getCurrentStatus()).isEqualTo("COMPLETED");
        assertThat(response.getEvents()).hasSize(5); // ORDER_CREATED, PACKAGE_PACKED, HANDED_OVER, IN_TRANSIT, DELIVERED
    }

    @Test
    @DisplayName("Update Shipping Config - Thành công cập nhật biểu phí vận chuyển")
    void testUpdateShippingConfig_Success() {
        when(shippingConfigRepository.findFirstByOrderByIdAsc()).thenReturn(Optional.of(defaultConfig));
        when(shippingConfigRepository.save(any(ShippingConfig.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateShippingConfigRequest request = UpdateShippingConfigRequest.builder()
                .standardBaseFee(35_000L)
                .expressBaseFee(50_000L)
                .sameDayBaseFee(70_000L)
                .freeShippingThreshold(400_000L)
                .maxFreeShippingSubsidy(35_000L)
                .standardMaxWeightGram(2500)
                .overweightUnitGram(500)
                .overweightSurcharge(6_000L)
                .build();

        ShippingConfigResponse response = shippingService.updateShippingConfig(1L, request);

        assertThat(response.getStandardBaseFee()).isEqualTo(35_000L);
        assertThat(response.getExpressBaseFee()).isEqualTo(50_000L);
        assertThat(response.getFreeShippingThreshold()).isEqualTo(400_000L);
    }
}
