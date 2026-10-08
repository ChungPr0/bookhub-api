package com.chungpr0.bookhub.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    @Builder.Default
    private List<T> items = new ArrayList<>();

    private PageMeta page;

    public static <T> PageResponse<T> of(Page<T> springPage) {
        return PageResponse.<T>builder()
                .items(springPage.getContent())
                .page(PageMeta.builder()
                        .number(springPage.getNumber())
                        .size(springPage.getSize())
                        .totalElements(springPage.getTotalElements())
                        .totalPages(springPage.getTotalPages())
                        .first(springPage.isFirst())
                        .last(springPage.isLast())
                        .build())
                .build();
    }

    public static <T> PageResponse<T> of(List<T> items, PageMeta page) {
        return PageResponse.<T>builder()
                .items(items)
                .page(page)
                .build();
    }
}

