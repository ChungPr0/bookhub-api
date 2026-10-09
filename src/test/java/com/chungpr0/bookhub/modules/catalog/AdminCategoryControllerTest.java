package com.chungpr0.bookhub.modules.catalog;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.catalog.dto.request.CreateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.dto.request.UpdateCategoryRequest;
import com.chungpr0.bookhub.modules.catalog.entity.Category;
import com.chungpr0.bookhub.modules.catalog.repository.BookRepository;
import com.chungpr0.bookhub.modules.catalog.repository.CategoryRepository;
import com.chungpr0.bookhub.modules.catalog.repository.ReviewRepository;
import com.chungpr0.bookhub.modules.catalog.repository.WishlistRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.chungpr0.bookhub.modules.cart.repository.CartItemRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderDetailRepository;
import com.chungpr0.bookhub.modules.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AdminCategoryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

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

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private String customerToken;
    private String adminToken;
    private Category rootCategory;
    private Category childCategory;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        cleanDatabase();

        Account customerAccount = accountRepository.findByUsername("0911111111").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0911111111")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role(Role.CUSTOMER)
                        .status(AccountStatus.ACTIVE)
                        .tokenVersion(0)
                        .build()));
        customerToken = jwtTokenProvider.generateAccessToken(customerAccount);

        Account adminAccount = accountRepository.findByUsername("0922222222").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0922222222")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role(Role.ADMIN)
                        .status(AccountStatus.ACTIVE)
                        .tokenVersion(0)
                        .build()));
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);

        rootCategory = categoryRepository.save(Category.builder()
                .name("Sách Trong Nước")
                .slug("sach-trong-nuoc")
                .sortOrder(1)
                .build());

        childCategory = categoryRepository.save(Category.builder()
                .name("Văn Học")
                .slug("van-hoc")
                .parent(rootCategory)
                .sortOrder(1)
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/admin/categories/tree - 401 UNAUTHORIZED khi chưa đăng nhập")
    void testGetTree_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories/tree"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/categories/tree - 403 FORBIDDEN khi người dùng là CUSTOMER")
    void testGetTree_ForbiddenForCustomer() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories/tree")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("GET /api/v1/admin/categories/tree - 200 OK trả về cấu trúc cây đầy đủ cho ADMIN")
    void testGetTree_SuccessForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/admin/categories/tree")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data", hasSize(1)))
                .andExpect(jsonPath("$.data[0].name").value("Sách Trong Nước"))
                .andExpect(jsonPath("$.data[0].children", hasSize(1)))
                .andExpect(jsonPath("$.data[0].children[0].name").value("Văn Học"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 400 BAD_REQUEST khi tên danh mục để trống")
    void testCreateCategory_ValidationBlankName() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("")
                .build();

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 201 CREATED thành công thêm danh mục mới")
    void testCreateCategory_Success() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Kinh Tế")
                .sortOrder(2)
                .build();

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Kinh Tế"))
                .andExpect(jsonPath("$.data.slug").value("kinh-te"));
    }

    @Test
    @DisplayName("POST /api/v1/admin/categories - 409 CONFLICT khi tên danh mục bị trùng trong cùng nhóm")
    void testCreateCategory_DuplicateName() throws Exception {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Văn Học")
                .parentId(rootCategory.getId())
                .build();

        mockMvc.perform(post("/api/v1/admin/categories")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CATEGORY_NAME_DUPLICATE"));
    }

    @Test
    @DisplayName("PUT /api/v1/admin/categories/{id} - 422 UNPROCESSABLE_CONTENT khi tham chiếu vòng lặp cha con")
    void testUpdateCategory_CircularReference() throws Exception {
        UpdateCategoryRequest request = UpdateCategoryRequest.builder()
                .name("Sách Trong Nước")
                .parentId(rootCategory.getId()) // Gán cha là chính nó
                .build();

        mockMvc.perform(put("/api/v1/admin/categories/" + rootCategory.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CATEGORY_CIRCULAR_REFERENCE"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{id} - 409 CONFLICT khi danh mục còn chứa danh mục con")
    void testDeleteCategory_HasChildren() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/" + rootCategory.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("CATEGORY_HAS_CHILDREN"));
    }

    @Test
    @DisplayName("DELETE /api/v1/admin/categories/{id} - 200 OK thành công xóa danh mục lá không có sách")
    void testDeleteCategory_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/admin/categories/" + childCategory.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
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
