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
public class AdminOrderStatusHistoryResponse {

    @Schema(description = "ID bản ghi", example = "1")
    private Long id;

    @Schema(description = "Trạng thái trước đó", example = "PENDING")
    private OrderStatus fromStatus;

    @Schema(description = "Trạng thái sau khi đổi", example = "CONFIRMED")
    private OrderStatus toStatus;

    @Schema(description = "Ghi chú quá trình", example = "Hệ thống tự động duyệt sau khi VNPAY báo thành công")
    private String note;

    @Schema(description = "Loại đối tượng thao tác", example = "STAFF")
    private ActorType actorType;

    @Schema(description = "Thông tin nhân viên đổi trạng thái (null nếu là SYSTEM hoặc CUSTOMER)")
    private StaffOrAccountInfoResponse changedBy;

    @Schema(description = "Thời gian thay đổi")
    private OffsetDateTime createdAt;
}

