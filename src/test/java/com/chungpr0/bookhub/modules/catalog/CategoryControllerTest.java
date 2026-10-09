package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CategoryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private com.chungpr0.bookhub.modules.catalog.repository.BookRepository bookRepository;

    @Autowired
    private com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository reviewRepository;

    @Autowired
    private com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository wishlistRepository;

    @Autowired(required = false)
    private OrderDetailRepository orderDetailRepository;

    @Autowired(required = false)
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private CartItemRepository cartItemRepository;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Category root = Category.builder()
                .name("Văn học")
                .slug("van-hoc")
                .sortOrder(1)
                .build();
        categoryRepository.save(root);

        Category child = Category.builder()
                .name("Tiểu thuyết")
                .slug("tieu-thuyet")
                .parent(root)
                .sortOrder(1)
                .build();
        categoryRepository.save(child);
    }

    @Test
    @DisplayName("GET /api/v1/categories/tree - Thành công (200 OK)")
    void testGetCategoryTree_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/tree")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data[0].slug").value("van-hoc"))
                .andExpect(jsonPath("$.data[0].children[0].slug").value("tieu-thuyet"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{slug} - Thành công (200 OK)")
    void testGetCategoryBySlug_Success() throws Exception {
        mockMvc.perform(get("/api/v1/categories/tieu-thuyet")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Tiểu thuyết"))
                .andExpect(jsonPath("$.data.breadcrumb").isArray())
                .andExpect(jsonPath("$.data.breadcrumb[0].slug").value("van-hoc"))
                .andExpect(jsonPath("$.data.breadcrumb[1].slug").value("tieu-thuyet"));
    }

    @Test
    @DisplayName("GET /api/v1/categories/{slug} - Không tìm thấy danh mục (404 NOT_FOUND)")
    void testGetCategoryBySlug_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/categories/unknown-category")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"));
    }

    @AfterEach
    void tearDown() {
        cleanDatabase();
    }

    private void cleanDatabase() {
        if (orderDetailRepository != null) {
            orderDetailRepository.deleteAll();
        }
        if (orderRepository != null) {
            orderRepository.deleteAll();
        }
        if (cartItemRepository != null) {
            cartItemRepository.deleteAll();
        }
        reviewRepository.deleteAll();
        wishlistRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
    }
}

