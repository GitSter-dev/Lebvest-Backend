package com.application.lebvest.integration;

import com.application.lebvest.TestcontainersConfiguration;
import com.application.lebvest.models.entities.AdminUser;
import com.application.lebvest.repositories.AdminUserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class AdminAuthSecurityIntTest {

    private static final String ADMIN_EMAIL = "admin@lebvest.com";
    private static final String ADMIN_PASSWORD = "AdminAuthSecurityTestPwd1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AdminUserRepository adminUserRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private static final ObjectMapper TEST_JSON = testObjectMapper();

    @BeforeEach
    void setUp() {
        adminUserRepository.deleteAll();
        adminUserRepository.saveAndFlush(
                AdminUser.builder()
                        .email(ADMIN_EMAIL)
                        .name("Admin")
                        .passwordHash(passwordEncoder.encode(ADMIN_PASSWORD))
                        .role("ADMIN")
                        .enabled(true)
                        .build()
        );
    }

    @Test
    void login_whenCredentialsValid_returnsTokenPayload() throws Exception {
        var mvcResult = mockMvc.perform(post("/admin/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(ADMIN_EMAIL, ADMIN_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value(ADMIN_EMAIL))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.expiresAt").exists())
                .andReturn();

        JsonNode root = TEST_JSON.readTree(mvcResult.getResponse().getContentAsString());
        assertThat(root.path("data").path("accessToken").asText()).isNotBlank();
    }

    private static ObjectMapper testObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Test
    void adminRoute_whenMissingBearer_returns401Json() throws Exception {
        mockMvc.perform(post("/admin/investor-applications/1/accept"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.error.message").value("Unauthorized"));
    }

    @Test
    void adminRoute_whenBearerInvalid_returns401Json() throws Exception {
        mockMvc.perform(post("/admin/investor-applications/1/accept")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));
    }
}
