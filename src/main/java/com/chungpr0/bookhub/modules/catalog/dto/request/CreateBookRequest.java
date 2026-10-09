package com.chungpr0.bookhub.modules.catalog.dto.request;

import com.chungpr0.bookhub.common.enums.BookStatus;
import com.chungpr0.bookhub.common.enums.CoverType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Yêu cầu tạo mới sản phẩm sách (ABK-03)")
public class CreateBookRequest {

    @Pattern(regexp = "^(97(8|9))?\\d{9}(\\d|X)$|^$", message = "Mã ISBN không hợp lệ (phải là 10 hoặc 13 chữ số)")
    @Schema(description = "Mã ISBN (10 hoặc 13 chữ số, có thể để trống)", example = "9786045629870")
    private String isbn;

    @NotBlank(message = "Tiêu đề cuốn sách không được để trống")
    @Size(min = 2, max = 255, message = "Tiêu đề cuốn sách phải từ 2 đến 255 ký tự")
    @Schema(description = "Tiêu đề cuốn sách", example = "Nhà Giả Kim (Tái bản 2026)")
    private String title;

    @NotEmpty(message = "Sách phải có ít nhất 1 tác giả")
    @Size(min = 1, max = 10, message = "Sách tối đa 10 tác giả")
    @Schema(description = "Danh sách ID tác giả theo thứ tự hiển thị", example = "[7]")
    @Builder.Default
    private List<Long> authorIds = new ArrayList<>();

    @NotNull(message = "Vui lòng chọn danh mục cho cuốn sách")
    @Schema(description = "ID danh mục sách", example = "5")
    private Long categoryId;

    @NotNull(message = "Vui lòng chọn nhà xuất bản")
    @Schema(description = "ID nhà xuất bản", example = "4")
    private Long publisherId;

    @Size(max = 150, message = "Tên dịch giả tối đa 150 ký tự")
    @Schema(description = "Tên dịch giả", example = "Lê Chu Cầu")
    private String translator;

    @Min(value = 1800, message = "Năm xuất bản phải từ năm 1800 trở về sau")
    @Schema(description = "Năm xuất bản", example = "2026")
    private Integer publicationYear;

    @NotBlank(message = "Ngôn ngữ không được để trống")
    @Size(max = 10, message = "Mã ngôn ngữ tối đa 10 ký tự")
    @Builder.Default
    @Schema(description = "Mã ngôn ngữ (VI, EN...)", example = "VI")
    private String language = "VI";

    @Min(value = 1, message = "Số trang phải từ 1 trở lên")
    @Max(value = 10000, message = "Số trang tối đa 10,000")
    @Schema(description = "Số trang của cuốn sách", example = "228")
    private Integer pageCount;

    @NotNull(message = "Vui lòng chọn loại bìa")
    @Builder.Default
    @Schema(description = "Hình thức bìa sách (PAPERBACK, HARDCOVER)", example = "PAPERBACK")
    private CoverType coverType = CoverType.PAPERBACK;

    @Size(max = 50, message = "Kích thước sách tối đa 50 ký tự")
    @Schema(description = "Kích thước sách (dài x rộng)", example = "13 x 20.5 cm")
    private String dimensions;

    @Min(value = 1, message = "Khối lượng phải từ 1 gram trở lên")
    @Max(value = 10000, message = "Khối lượng tối đa 10,000 gram")
    @Schema(description = "Khối lượng tính theo gram", example = "260")
    private Integer weightGram;

    @NotNull(message = "Vui lòng nhập giá gốc")
    @Min(value = 1000, message = "Giá gốc tối thiểu 1.000 VND")
    @Max(value = 50000000, message = "Giá gốc tối đa 50.000.000 VND")
    @Schema(description = "Giá bìa/giá gốc (VND)", example = "85000")
    private Long originalPrice;

    @NotNull(message = "Vui lòng nhập giá bán")
    @Min(value = 1000, message = "Giá bán tối thiểu 1.000 VND")
    @Max(value = 50000000, message = "Giá bán tối đa 50.000.000 VND")
    @Schema(description = "Giá bán thực tế (VND, phải nhỏ hơn hoặc bằng giá gốc)", example = "68000")
    private Long salePrice;

    @Size(max = 50000, message = "Mô tả cuốn sách tối đa 50,000 ký tự")
    @Schema(description = "Mô tả chi tiết nội dung cuốn sách", example = "<p>Hành trình đi tìm kho báu của chàng chăn cừu Santiago...</p>")
    private String description;

    @Min(value = 0, message = "Ngưỡng cảnh báo tồn kho phải từ 0 trở lên")
    @Builder.Default
    @Schema(description = "Ngưỡng báo động sắp hết hàng", example = "10")
    private Integer lowStockThreshold = 10;

    @Builder.Default
    @Schema(description = "Trạng thái hiển thị sản phẩm (ACTIVE, INACTIVE)", example = "ACTIVE")
    private BookStatus status = BookStatus.ACTIVE;

    @NotEmpty(message = "Sách phải có ít nhất 1 ảnh (ảnh đầu tiên là ảnh bìa)")
    @Size(min = 1, max = 10, message = "Sách tối đa 10 hình ảnh")
    @Schema(description = "Danh sách URL hình ảnh của cuốn sách", example = "[\"https://cdn.bookhub.vn/uploads/202610/nha-gia-kim-cover.webp\"]")
    @Builder.Default
    private List<String> images = new ArrayList<>();
}

