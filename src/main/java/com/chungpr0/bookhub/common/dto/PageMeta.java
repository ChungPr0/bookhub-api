package com.chungpr0.bookhub.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Thông tin siêu dữ liệu phân trang chuẩn hệ thống")
public class PageMeta {

    @Schema(description = "Chỉ số trang hiện tại (bắt đầu từ 0)", example = "0")
    private int number;

    @Schema(description = "Số lượng phần tử tối đa trên mỗi trang", example = "20")
    private int size;

    @Schema(description = "Tổng số phần tử thỏa mãn điều kiện lọc", example = "100")
    private long totalElements;

    @Schema(description = "Tổng số trang", example = "5")
    private int totalPages;

    @Schema(description = "Có phải là trang đầu tiên hay không", example = "true")
    private boolean first;

    @Schema(description = "Có phải là trang cuối cùng hay không", example = "false")
    private boolean last;

    public static PageMeta of(Page<?> page) {
        if (page == null) {
            return empty(0, 20);
        }
        return PageMeta.builder()
                .number(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }

    public static PageMeta of(int pageNumber, int pageSize, long totalElements) {
        int validatedSize = Math.max(1, pageSize);
        int totalPages = (int) Math.ceil((double) totalElements / validatedSize);
        return PageMeta.builder()
                .number(pageNumber)
                .size(validatedSize)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(pageNumber <= 0)
                .last(pageNumber >= Math.max(0, totalPages - 1))
                .build();
    }

    public static PageMeta empty(int pageNumber, int pageSize) {
        return PageMeta.builder()
                .number(pageNumber)
                .size(Math.max(1, pageSize))
                .totalElements(0L)
                .totalPages(0)
                .first(true)
                .last(true)
                .build();
    }
}
