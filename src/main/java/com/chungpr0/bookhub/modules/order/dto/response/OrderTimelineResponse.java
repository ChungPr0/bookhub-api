package com.chungpr0.bookhub.modules.order.dto.response;

import com.chungpr0.bookhub.modules.order.enums.ActorType;
import com.chungpr0.bookhub.modules.order.enums.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderTimelineResponse {

    @Schema(description = "ID bản ghi lịch sử", example = "1")
    private Long id;

    @Schema(description = "Trạng thái trước đó", example = "PENDING")
    private OrderStatus fromStatus;

    @Schema(description = "Trạng thái chuyển sang", example = "CONFIRMED")
    private OrderStatus toStatus;

    @Schema(description = "Ghi chú tiến trình", example = "Khách hàng tạo đơn hàng thành công")
    private String note;

    @Schema(description = "Thời điểm diễn ra sự kiện")
    private OffsetDateTime timestamp;

    @Schema(description = "Đối tượng thực hiện thay đổi (CUSTOMER, STAFF, SYSTEM, ADMIN)", example = "CUSTOMER")
    private ActorType actorType;
}

