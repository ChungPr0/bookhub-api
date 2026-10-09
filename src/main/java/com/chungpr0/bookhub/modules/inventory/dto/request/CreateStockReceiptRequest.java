package com.chungpr0.bookhub.modules.inventory.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu lập phiếu nhập kho theo lô")
public class CreateStockReceiptRequest {

    @NotNull(message = "ID nhà cung cấp không được để trống")
    @Schema(description = "ID nhà cung cấp giao hàng", example = "1")
    private Long supplierId;

    @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
    @Schema(description = "Ghi chú đợt nhập hàng", example = "Nhập sách đợt 1 tháng 10/2026 từ Nhã Nam")
    private String note;

    @NotNull(message = "Ngày nhập kho không được để trống")
    @Schema(description = "Ngày thực tế nhập hàng về kho (YYYY-MM-DD)", example = "2026-10-09")
    private LocalDate importDate;

    @NotEmpty(message = "Danh sách sách nhập không được để trống")
    @Size(min = 1, max = 200, message = "Mỗi phiếu nhập từ 1 đến 200 đầu sách")
    @Valid
    @Schema(description = "Danh sách các đầu sách nhập kho")
    @Builder.Default
    private List<StockReceiptItemRequest> items = new ArrayList<>();
}

