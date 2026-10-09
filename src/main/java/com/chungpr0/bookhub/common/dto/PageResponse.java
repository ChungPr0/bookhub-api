package com.chungpr0.bookhub.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Đối tượng phân trang dữ liệu chuẩn hóa toàn hệ thống")
public class PageResponse<T> {

    @Builder.Default
    @Schema(description = "Danh sách các phần tử của trang hiện tại")
    private List<T> items = new ArrayList<>();

    @Schema(description = "Thông tin siêu dữ liệu phân trang")
    private PageMeta page;

    /**
     * Tạo PageResponse từ Spring Data Page trực tiếp (cùng kiểu T).
     */
    public static <T> PageResponse<T> of(Page<T> springPage) {
        if (springPage == null) {
            return empty(0, 20);
        }
        return PageResponse.<T>builder()
                .items(springPage.getContent() != null ? springPage.getContent() : Collections.emptyList())
                .page(PageMeta.of(springPage))
                .build();
    }

    /**
     * Tạo PageResponse từ danh sách đã xử lý và PageMeta độc lập.
     */
    public static <T> PageResponse<T> of(List<T> items, PageMeta page) {
        return PageResponse.<T>builder()
                .items(items != null ? items : Collections.emptyList())
                .page(page != null ? page : PageMeta.empty(0, 20))
                .build();
    }

    /**
     * Tạo PageResponse từ danh sách DTO đã transform và Spring Data Page gốc (khác kiểu).
     */
    public static <T, R> PageResponse<T> of(List<T> items, Page<R> springPage) {
        return PageResponse.<T>builder()
                .items(items != null ? items : Collections.emptyList())
                .page(PageMeta.of(springPage))
                .build();
    }

    /**
     * Tạo PageResponse từ Spring Data Page gốc và tự động map qua Function mapper.
     */
    public static <T, R> PageResponse<R> of(Page<T> springPage, Function<T, R> mapper) {
        if (springPage == null) {
            return empty(0, 20);
        }
        List<R> mappedItems = springPage.getContent() != null
                ? springPage.getContent().stream().map(mapper).toList()
                : Collections.emptyList();
        return PageResponse.<R>builder()
                .items(mappedItems)
                .page(PageMeta.of(springPage))
                .build();
    }

    /**
     * Tạo PageResponse từ danh sách items và các chỉ số phân trang thủ công (in-memory hoặc raw count).
     */
    public static <T> PageResponse<T> of(List<T> items, int pageNumber, int pageSize, long totalElements) {
        return PageResponse.<T>builder()
                .items(items != null ? items : Collections.emptyList())
                .page(PageMeta.of(pageNumber, pageSize, totalElements))
                .build();
    }

    /**
     * Tạo PageResponse rỗng cho trường hợp không có dữ liệu.
     */
    public static <T> PageResponse<T> empty(int pageNumber, int pageSize) {
        return PageResponse.<T>builder()
                .items(Collections.emptyList())
                .page(PageMeta.empty(pageNumber, pageSize))
                .build();
    }
}
