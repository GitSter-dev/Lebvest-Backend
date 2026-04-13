package com.application.lebvest.integration;

import com.application.lebvest.TestcontainersConfiguration;
import com.application.lebvest.models.entities.InvestorAccount;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import com.application.lebvest.repositories.InvestorAccountRepository;
import com.application.lebvest.repositories.InvestorApplicationRepository;
import com.application.lebvest.repositories.SetPasswordTokenRepository;
import com.application.lebvest.services.HandlebarsRendererService;
import com.application.lebvest.services.MailService;
import com.application.lebvest.services.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
class InvestorAuthLoginIntTest {

    private static final String INVESTOR_EMAIL = "investor-login@example.com";
    private static final String INVESTOR_PASSWORD = "InvestorLoginTestPwd1";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InvestorApplicationRepository investorApplicationRepository;

    @Autowired
    InvestorAccountRepository investorAccountRepository;

    @Autowired
    SetPasswordTokenRepository setPasswordTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @MockitoBean
    S3Service s3Service;

    @MockitoBean
    S3Presigner s3Presigner;

    @MockitoBean
    MailService mailService;

    @MockitoBean
    HandlebarsRendererService handlebarsRendererService;

    @BeforeEach
    void setUp() {
        investorAccountRepository.deleteAll();
        setPasswordTokenRepository.deleteAll();
        investorApplicationRepository.deleteAll();

        InvestorApplication application = investorApplicationRepository.saveAndFlush(
                InvestorApplication.builder()
                        .firstname("Jane")
                        .lastname("Investor")
                        .email(INVESTOR_EMAIL)
                        .applicationStatus(InvestorApplicationStatus.ACCEPTED)
                        .build()
        );

        investorAccountRepository.saveAndFlush(
                InvestorAccount.builder()
                        .email(INVESTOR_EMAIL)
                        .passwordHash(passwordEncoder.encode(INVESTOR_PASSWORD))
                        .enabled(true)
                        .application(application)
                        .build()
        );
    }

    @Test
    void login_whenCredentialsValid_returnsTokenPayload() throws Exception {
        mockMvc.perform(post("/investors/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(INVESTOR_EMAIL, INVESTOR_PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.email").value(INVESTOR_EMAIL))
                .andExpect(jsonPath("$.data.role").value("INVESTOR"))
                .andExpect(jsonPath("$.data.expiresAt").exists());
    }

    @Test
    void login_whenPasswordWrong_returns401() throws Exception {
        mockMvc.perform(post("/investors/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"wrong-password"}
                                """.formatted(INVESTOR_EMAIL)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401))
                .andExpect(jsonPath("$.error.message").value("Invalid email or password"));
    }

    @Test
    void login_whenEmailUnknown_returns401() throws Exception {
        mockMvc.perform(post("/investors/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"nobody@example.com","password":"x"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));
    }

    @Test
    void login_whenAccountDisabled_returns401() throws Exception {
        investorAccountRepository.deleteAll();
        InvestorApplication application = investorApplicationRepository.findAll().getFirst();
        investorAccountRepository.saveAndFlush(
                InvestorAccount.builder()
                        .email(INVESTOR_EMAIL)
                        .passwordHash(passwordEncoder.encode(INVESTOR_PASSWORD))
                        .enabled(false)
                        .application(application)
                        .build()
        );

        mockMvc.perform(post("/investors/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","password":"%s"}
                                """.formatted(INVESTOR_EMAIL, INVESTOR_PASSWORD)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.statusCode").value(401));
    }
}
