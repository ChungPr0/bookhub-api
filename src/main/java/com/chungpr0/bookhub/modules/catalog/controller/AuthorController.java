package com.chungpr0.bookhub.modules.catalog.controller;

import com.chungpr0.bookhub.common.dto.ApiResponse;
import com.chungpr0.bookhub.common.dto.PageResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorCardResponse;
import com.chungpr0.bookhub.modules.catalog.dto.response.AuthorDetailResponse;
import com.chungpr0.bookhub.modules.catalog.service.AuthorService;
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
@RequestMapping("/api/v1/authors")
@RequiredArgsConstructor
@Tag(name = "3.3 Tác giả Công khai (Authors)", description = "APIs tra cứu danh sách tác giả và chi tiết tác giả cùng các tác phẩm nổi bật")
public class AuthorController {

    private final AuthorService authorService;

    @GetMapping
    @Operation(
            summary = "AUT-01: Danh sách Tác giả công khai",
            description = "Trả về danh sách tác giả có phân trang, kèm theo số lượng tác phẩm đang mở bán của từng tác giả."
    )
    public ResponseEntity<ApiResponse<PageResponse<AuthorCardResponse>>> getAuthors(
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Kích thước trang (tối đa 60)", example = "20")
            @RequestParam(defaultValue = "20") int size
    ) {
        PageResponse<AuthorCardResponse> response = authorService.getAuthors(page, size);
        return ResponseEntity.ok(ApiResponse.success("Lấy danh sách tác giả thành công", response));
    }

    @GetMapping("/{slug}")
    @Operation(
            summary = "AUT-02: Chi tiết Tác giả & Top tác phẩm",
            description = "Lấy thông tin chi tiết một tác giả theo slug, bao gồm tiểu sử, ảnh đại diện và tối đa 8 tác phẩm tiêu biểu."
    )
    public ResponseEntity<ApiResponse<AuthorDetailResponse>> getAuthorBySlug(
            @Parameter(description = "Slug tác giả", example = "paulo-coelho", required = true)
            @PathVariable String slug
    ) {
        AuthorDetailResponse detail = authorService.getAuthorBySlug(slug);
        return ResponseEntity.ok(ApiResponse.success("Lấy thông tin tác giả thành công", detail));
    }
}

