package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.catalog.entity.Author;
import com.chungpr0.bookhub.modules.catalog.repository.AuthorRepository;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
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
class AuthorControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private WishlistRepository wishlistRepository;

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

        Author author = Author.builder()
                .name("Paulo Coelho")
                .slug("paulo-coelho")
                .biography("Tác giả cuốn tiểu thuyết nổi tiếng Nhà Giả Kim")
                .avatarUrl("https://cdn.bookhub.vn/authors/paulo-coelho.webp")
                .build();
        authorRepository.save(author);
    }

    @Test
    @DisplayName("GET /api/v1/authors - Thành công trả về danh sách phân trang (200 OK)")
    void testGetAuthors_Success() throws Exception {
        mockMvc.perform(get("/api/v1/authors?page=0&size=10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.items[0].slug").value("paulo-coelho"))
                .andExpect(jsonPath("$.data.page.totalElements").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/authors/{slug} - Thành công trả về chi tiết tác giả (200 OK)")
    void testGetAuthorBySlug_Success() throws Exception {
        mockMvc.perform(get("/api/v1/authors/paulo-coelho")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Paulo Coelho"))
                .andExpect(jsonPath("$.data.slug").value("paulo-coelho"));
    }

    @Test
    @DisplayName("GET /api/v1/authors/{slug} - Không tìm thấy tác giả (404 NOT_FOUND)")
    void testGetAuthorBySlug_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/authors/unknown-author")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("AUTHOR_NOT_FOUND"));
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
        authorRepository.deleteAll();
    }
}

