package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.PublisherDetailResponse;
import com.chungpr0.bookhub.modules.catalog.service.PublisherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/publishers")
@RequiredArgsConstructor
@Tag(name = "3.4 Nhà xuất bản Công khai (Publishers)", description = "APIs tra cứu danh sách nhà xuất bản và chi tiết NXB cùng các ấn phẩm")
public class PublisherController {

    private final PublisherService publisherService;

    @GetMapping
    @Operation(
            summary = "PUB-01: Danh sách Nhà xuất bản công khai",
            description = "Trả về danh sách các nhà xuất bản uy tín có phân trang kèm số lượng tác phẩm đang mở bán."
    )
    public ResponseEntity<ApiResponse<PageResponse<PublisherCardResponse>>> getPublishers(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang (tối đa 60)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<PublisherCardResponse> response = publisherService.getPublishers(page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách nhà xuất bản thành công", response));
    }

    @GetMapping("/{slug}")
    @Operation(
            summary = "PUB-02: Chi tiết Nhà xuất bản & Top tác phẩm",
            description = "Lấy thông tin giới thiệu, địa chỉ, website và danh sách các tác phẩm nổi bật của một nhà xuất bản."
    )
    public ResponseEntity<ApiResponse<PublisherDetailResponse>> getPublisherBySlug(
            @Parameter(description = "Slug nhà xuất bản", example = "nxb-hoi-nha-van", required = true)
            @PathVariable String slug
    ) {
        PublisherDetailResponse detail = publisherService.getPublisherBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin nhà xuất bản thành công", detail));
    }
}

