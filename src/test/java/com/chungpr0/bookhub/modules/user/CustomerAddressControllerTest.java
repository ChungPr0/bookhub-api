package com.chungpr0.bookhub.modules.user;

import com.chungpr0.bookhub.common.enums.AccountStatus;
import com.chungpr0.bookhub.common.enums.CustomerTier;
import com.chungpr0.bookhub.common.enums.Gender;
import com.chungpr0.bookhub.common.enums.Role;
import com.chungpr0.bookhub.modules.auth.entity.Account;
import com.chungpr0.bookhub.modules.auth.repository.AccountRepository;
import com.chungpr0.bookhub.modules.user.dto.request.AddressRequest;
import com.chungpr0.bookhub.modules.user.entity.Address;
import com.chungpr0.bookhub.modules.user.entity.Customer;
import com.chungpr0.bookhub.modules.user.repository.AddressRepository;
import com.chungpr0.bookhub.modules.user.repository.CustomerRepository;
import com.chungpr0.bookhub.security.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class CustomerAddressControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private com.chungpr0.bookhub.modules.cart.repository.CartRepository cartRepository;

    @Autowired
    private com.chungpr0.bookhub.modules.user.repository.PointTransactionRepository pointTransactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    private Account customerAAccount;
    private Customer customerA;
    private String tokenA;

    private Account customerBAccount;
    private Customer customerB;
    private String tokenB;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        addressRepository.deleteAll();
        pointTransactionRepository.deleteAll();
        cartRepository.deleteAll();
        customerRepository.deleteAll();
        accountRepository.deleteAll();

        // Customer A
        customerAAccount = Account.builder()
                .username("0988888888")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(customerAAccount);

        customerA = Customer.builder()
                .account(customerAAccount)
                .fullName("Khách hàng A")
                .phone("0988888888")
                .gender(Gender.MALE)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customerRepository.save(customerA);

        tokenA = jwtTokenProvider.generateAccessToken(
                customerAAccount.getId(),
                customerAAccount.getRole(),
                customerAAccount.getTokenVersion()
        );

        // Customer B
        customerBAccount = Account.builder()
                .username("0977777777")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.CUSTOMER)
                .status(AccountStatus.ACTIVE)
                .tokenVersion(0)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        accountRepository.save(customerBAccount);

        customerB = Customer.builder()
                .account(customerBAccount)
                .fullName("Khách hàng B")
                .phone("0977777777")
                .gender(Gender.FEMALE)
                .customerTier(CustomerTier.BRONZE)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        customerRepository.save(customerB);

        tokenB = jwtTokenProvider.generateAccessToken(
                customerBAccount.getId(),
                customerBAccount.getRole(),
                customerBAccount.getTokenVersion()
        );
    }

    @Test
    @DisplayName("ADR-01: GET /api/v1/me/addresses - Trả về danh sách địa chỉ sắp xếp mặc định trước")
    void testGetAddressesSuccess() throws Exception {
        Address addr1 = Address.builder()
                .customer(customerA)
                .receiverName("A1")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 1")
                .isDefault(false)
                .createdAt(OffsetDateTime.now().minusHours(2))
                .updatedAt(OffsetDateTime.now().minusHours(2))
                .build();
        addressRepository.save(addr1);

        Address addr2 = Address.builder()
                .customer(customerA)
                .receiverName("A2")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Đống Đa")
                .ward("Láng Hạ")
                .detailAddress("Số 2")
                .isDefault(true)
                .createdAt(OffsetDateTime.now().minusHours(1))
                .updatedAt(OffsetDateTime.now().minusHours(1))
                .build();
        addressRepository.save(addr2);

        mockMvc.perform(get("/api/v1/me/addresses")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(addr2.getId()))
                .andExpect(jsonPath("$.data[0].isDefault").value(true))
                .andExpect(jsonPath("$.data[1].id").value(addr1.getId()))
                .andExpect(jsonPath("$.data[1].isDefault").value(false));
    }

    @Test
    @DisplayName("ADR-02: GET /api/v1/me/addresses/{id} - Chống IDOR (404 khi truy cập địa chỉ của người khác)")
    void testGetAddressIdorDefense() throws Exception {
        Address addrA = Address.builder()
                .customer(customerA)
                .receiverName("A Nhà Riêng")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 10")
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        addressRepository.save(addrA);

        // Customer B calls with addrA's id -> 404 NOT_FOUND
        mockMvc.perform(get("/api/v1/me/addresses/" + addrA.getId())
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    @DisplayName("ADR-03: POST /api/v1/me/addresses - Tạo địa chỉ đầu tiên tự động thành mặc định (201 Created)")
    void testCreateFirstAddressAutoDefault() throws Exception {
        AddressRequest request = AddressRequest.builder()
                .receiverName("Nguyễn Tiến Chung")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Quận Cầu Giấy")
                .ward("Phường Dịch Vọng")
                .detailAddress("Số 10 Trần Thái Tông")
                .isDefault(false) // Thậm chí gửi false cũng phải tự thành true vì là địa chỉ đầu tiên
                .build();

        mockMvc.perform(post("/api/v1/me/addresses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.code").value("CREATED"))
                .andExpect(jsonPath("$.data.isDefault").value(true))
                .andExpect(jsonPath("$.data.fullAddress").value("Số 10 Trần Thái Tông, Phường Dịch Vọng, Quận Cầu Giấy, Hà Nội"));
    }

    @Test
    @DisplayName("ADR-03: POST /api/v1/me/addresses - 422 Unprocessable Entity khi vượt quá 10 địa chỉ")
    void testCreateAddressLimitExceeded() throws Exception {
        // Seed 10 addresses for Customer A
        for (int i = 1; i <= 10; i++) {
            Address addr = Address.builder()
                    .customer(customerA)
                    .receiverName("Địa chỉ " + i)
                    .receiverPhone("0988888888")
                    .province("Hà Nội")
                    .district("Cầu Giấy")
                    .ward("Dịch Vọng")
                    .detailAddress("Số " + i)
                    .isDefault(i == 1)
                    .createdAt(OffsetDateTime.now())
                    .updatedAt(OffsetDateTime.now())
                    .build();
            addressRepository.save(addr);
        }

        AddressRequest request = AddressRequest.builder()
                .receiverName("Địa chỉ thứ 11")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 11")
                .build();

        mockMvc.perform(post("/api/v1/me/addresses")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.code").value("ADDRESS_LIMIT_EXCEEDED"));
    }

    @Test
    @DisplayName("ADR-04: PUT /api/v1/me/addresses/{id} - Chống IDOR khi cập nhật địa chỉ")
    void testUpdateAddressIdorDefense() throws Exception {
        Address addrA = Address.builder()
                .customer(customerA)
                .receiverName("A")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 10")
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        addressRepository.save(addrA);

        AddressRequest request = AddressRequest.builder()
                .receiverName("Hacker Sửa")
                .receiverPhone("0977777777")
                .province("TP Hồ Chí Minh")
                .district("Quận 1")
                .ward("Bến Nghé")
                .detailAddress("Đường Nguyễn Huệ")
                .build();

        mockMvc.perform(put("/api/v1/me/addresses/" + addrA.getId())
                        .header("Authorization", "Bearer " + tokenB)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ADDRESS_NOT_FOUND"));
    }

    @Test
    @DisplayName("ADR-05: DELETE /api/v1/me/addresses/{id} - Xóa địa chỉ mặc định, tự động gán địa chỉ còn lại làm mặc định mới")
    void testDeleteDefaultAddressReassignsNewDefault() throws Exception {
        Address addr1 = Address.builder()
                .customer(customerA)
                .receiverName("A1 Mặc định")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 1")
                .isDefault(true)
                .createdAt(OffsetDateTime.now().minusHours(2))
                .updatedAt(OffsetDateTime.now().minusHours(2))
                .build();
        addressRepository.save(addr1);

        Address addr2 = Address.builder()
                .customer(customerA)
                .receiverName("A2 Phụ")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Đống Đa")
                .ward("Láng Hạ")
                .detailAddress("Số 2")
                .isDefault(false)
                .createdAt(OffsetDateTime.now().minusHours(1))
                .updatedAt(OffsetDateTime.now().minusHours(1))
                .build();
        addressRepository.save(addr2);

        mockMvc.perform(delete("/api/v1/me/addresses/" + addr1.getId())
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.deletedAddressId").value(addr1.getId()))
                .andExpect(jsonPath("$.data.newDefaultAddressId").value(addr2.getId()));

        Address updatedAddr2 = addressRepository.findById(addr2.getId()).orElseThrow();
        assertThat(updatedAddr2.isDefault()).isTrue();
    }

    @Test
    @DisplayName("ADR-06: PATCH /api/v1/me/addresses/{id}/default - Đặt làm địa chỉ mặc định nhanh chóng")
    void testSetDefaultAddressQuick() throws Exception {
        Address addr1 = Address.builder()
                .customer(customerA)
                .receiverName("A1")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Cầu Giấy")
                .ward("Dịch Vọng")
                .detailAddress("Số 1")
                .isDefault(true)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        addressRepository.save(addr1);

        Address addr2 = Address.builder()
                .customer(customerA)
                .receiverName("A2")
                .receiverPhone("0988888888")
                .province("Hà Nội")
                .district("Đống Đa")
                .ward("Láng Hạ")
                .detailAddress("Số 2")
                .isDefault(false)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
        addressRepository.save(addr2);

        mockMvc.perform(patch("/api/v1/me/addresses/" + addr2.getId() + "/default")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(addr2.getId()))
                .andExpect(jsonPath("$.data.isDefault").value(true));

        Address updatedAddr1 = addressRepository.findById(addr1.getId()).orElseThrow();
        Address updatedAddr2 = addressRepository.findById(addr2.getId()).orElseThrow();
        assertThat(updatedAddr1.isDefault()).isFalse();
        assertThat(updatedAddr2.isDefault()).isTrue();
    }
}
