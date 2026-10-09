package com.chungpr0.bookhub.modules.catalog.media;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class MediaControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private String customerToken;
    private String adminToken;

    private static final byte[] VALID_JPEG = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] VALID_PNG = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] INVALID_BYTES = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        Account customerAccount = accountRepository.findByUsername("0919999999").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0919999999")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role(Role.CUSTOMER)
                        .status(AccountStatus.ACTIVE)
                        .tokenVersion(0)
                        .build()));
        customerToken = jwtTokenProvider.generateAccessToken(customerAccount);

        Account adminAccount = accountRepository.findByUsername("0929999999").orElseGet(() ->
                accountRepository.save(Account.builder()
                        .username("0929999999")
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .role(Role.ADMIN)
                        .status(AccountStatus.ACTIVE)
                        .tokenVersion(0)
                        .build()));
        adminToken = jwtTokenProvider.generateAccessToken(adminAccount);
    }

    @Test
    @DisplayName("POST /api/v1/media/upload/single - 401 UNAUTHORIZED khi chưa đăng nhập")
    void testUploadSingle_Unauthenticated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", VALID_JPEG);

        mockMvc.perform(multipart("/api/v1/media/upload/single")
                        .file(file)
                        .param("folder", "BOOKS"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNAUTHENTICATED"));
    }

    @Test
    @DisplayName("POST /api/v1/media/upload/single - 201 CREATED thành công tải ảnh sách bởi ADMIN")
    void testUploadSingle_AdminBooksSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "clean-code.jpg", "image/jpeg", VALID_JPEG);

        mockMvc.perform(multipart("/api/v1/media/upload/single")
                        .file(file)
                        .param("folder", "BOOKS")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").isString())
                .andExpect(jsonPath("$.data.mimeType").value("image/webp"));
    }

    @Test
    @DisplayName("POST /api/v1/media/upload/single - 201 CREATED thành công tải ảnh đại diện bởi CUSTOMER")
    void testUploadSingle_CustomerAvatarSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", VALID_PNG);

        mockMvc.perform(multipart("/api/v1/media/upload/single")
                        .file(file)
                        .param("folder", "AVATARS")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").isString());
    }

    @Test
    @DisplayName("POST /api/v1/media/upload/single - 403 FORBIDDEN khi CUSTOMER cố tình tải vào thư mục BOOKS")
    void testUploadSingle_CustomerForbiddenFolder() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hack.jpg", "image/jpeg", VALID_JPEG);

        mockMvc.perform(multipart("/api/v1/media/upload/single")
                        .file(file)
                        .param("folder", "BOOKS")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("POST /api/v1/media/upload/single - 415 UNSUPPORTED_MEDIA_TYPE khi magic bytes không hợp lệ")
    void testUploadSingle_InvalidMagicBytes() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "malicious.jpg", "image/jpeg", INVALID_BYTES);

        mockMvc.perform(multipart("/api/v1/media/upload/single")
                        .file(file)
                        .param("folder", "BOOKS")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().is(415))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_FILE_TYPE"));
    }

    @Test
    @DisplayName("POST /api/v1/media/upload/batch - 403 FORBIDDEN khi CUSTOMER cố tình gọi API upload hàng loạt")
    void testUploadBatch_ForbiddenForCustomer() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "img1.png", "image/png", VALID_PNG);

        mockMvc.perform(multipart("/api/v1/media/upload/batch")
                        .file(file1)
                        .param("folder", "BOOKS")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }

    @Test
    @DisplayName("POST /api/v1/media/upload/batch - 201 CREATED thành công tải lên nhiều ảnh cho ADMIN")
    void testUploadBatch_SuccessForAdmin() throws Exception {
        MockMultipartFile file1 = new MockMultipartFile("files", "img1.png", "image/png", VALID_PNG);
        MockMultipartFile file2 = new MockMultipartFile("files", "img2.jpg", "image/jpeg", VALID_JPEG);

        mockMvc.perform(multipart("/api/v1/media/upload/batch")
                        .file(file1)
                        .file(file2)
                        .param("folder", "BOOKS")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUploaded").value(2))
                .andExpect(jsonPath("$.data.items", hasSize(2)));
    }
}

