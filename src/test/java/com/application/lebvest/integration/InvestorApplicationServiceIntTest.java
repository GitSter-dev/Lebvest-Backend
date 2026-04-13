package com.application.lebvest.integration;

import com.application.lebvest.TestcontainersConfiguration;
import com.application.lebvest.models.entities.InvestorAccount;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.entities.SetPasswordToken;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import com.application.lebvest.repositories.InvestorAccountRepository;
import com.application.lebvest.repositories.InvestorApplicationRepository;
import com.application.lebvest.repositories.SetPasswordTokenRepository;
import com.application.lebvest.services.HandlebarsRendererService;
import com.application.lebvest.services.MailService;
import com.application.lebvest.services.S3Service;
import com.application.lebvest.services.SetPasswordTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class InvestorApplicationServiceIntTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    InvestorApplicationRepository investorApplicationRepository;

    @Autowired
    InvestorAccountRepository investorAccountRepository;

    @Autowired
    SetPasswordTokenRepository setPasswordTokenRepository;

    @Autowired
    SetPasswordTokenService setPasswordTokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    // Mock away the services that need MinIO / Mailpit / Handlebars
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
        when(handlebarsRendererService.renderTemplate(anyString(), anyMap())).thenReturn("<html/>");
    }

    @Test
    void apply_whenValidRequest_returns201() throws Exception {
        stubPresignedUrl();

        mockMvc.perform(post("/investors/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "firstname": "John",
                              "lastname": "Doe",
                              "email": "john@example.com",
                              "identityDocumentName": "passport.pdf",
                              "addressProofDocumentName": "bill.pdf"
                            }
                            """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.identityDocumentPresignedUrl").value("http://fake-s3/presigned"))
                .andExpect(jsonPath("$.data.addressProofDocumentPresignedUrl").value("http://fake-s3/presigned"));

        // Verify the entity was persisted
        assertThat(investorApplicationRepository.count()).isEqualTo(1);
        assertThat(setPasswordTokenRepository.count()).isZero();
    }

    @Test
    void apply_whenDuplicateEmail_returns409() throws Exception {
        stubPresignedUrl();

        String body = """
            {
              "firstname": "A", "lastname": "B",
              "email": "dup@test.com",
              "identityDocumentName": "a.pdf",
              "addressProofDocumentName": "b.pdf"
            }
            """;

        // First call succeeds
        mockMvc.perform(post("/investors/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        // Second call with same email returns 409
        mockMvc.perform(post("/investors/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    @Test
    void apply_whenMissingFields_returns400() throws Exception {
        mockMvc.perform(post("/investors/application")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {"firstname": "", "lastname": "Doe", "email": "not-an-email"}
                            """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptInvestorApplication_whenPending_createsTokenAndMarksAccepted() throws Exception {
        InvestorApplication application = saveApplication("approved@example.com", InvestorApplicationStatus.PENDING);

        mockMvc.perform(post("/admin/investor-applications/{applicationId}/accept", application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.applicationStatus").value("ACCEPTED"));

        InvestorApplication persisted = investorApplicationRepository.findById(application.getId()).orElseThrow();
        assertThat(persisted.getApplicationStatus()).isEqualTo(InvestorApplicationStatus.ACCEPTED);
        assertThat(setPasswordTokenRepository.count()).isEqualTo(1);
    }

    @Test
    void rejectInvestorApplication_whenPending_removesTokensAndMarksRejected() throws Exception {
        InvestorApplication application = saveApplication("rejected@example.com", InvestorApplicationStatus.PENDING);
        setPasswordTokenRepository.save(
                SetPasswordToken.builder()
                        .token("stale-token")
                        .application(application)
                        .build()
        );

        mockMvc.perform(post("/admin/investor-applications/{applicationId}/reject", application.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.applicationStatus").value("REJECTED"));

        InvestorApplication persisted = investorApplicationRepository.findById(application.getId()).orElseThrow();
        assertThat(persisted.getApplicationStatus()).isEqualTo(InvestorApplicationStatus.REJECTED);
        assertThat(setPasswordTokenRepository.count()).isZero();
    }

    @Test
    void setPassword_whenTokenValid_createsInvestorAccountAndConsumesToken() throws Exception {
        InvestorApplication application = saveApplication("auth@example.com", InvestorApplicationStatus.ACCEPTED);
        String token = extractToken(setPasswordTokenService.createSetPasswordUrl(application));

        mockMvc.perform(post("/investors/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "token": "%s",
                              "password": "Password123"
                            }
                            """.formatted(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(200))
                .andExpect(jsonPath("$.data.email").value("auth@example.com"));

        assertThat(investorAccountRepository.count()).isEqualTo(1);
        InvestorAccount account = investorAccountRepository.findByEmail("auth@example.com").orElseThrow();
        assertThat(passwordEncoder.matches("Password123", account.getPasswordHash())).isTrue();
        assertThat(setPasswordTokenRepository.count()).isZero();
    }

    @Test
    void setPassword_whenTokenInvalid_returns400() throws Exception {
        mockMvc.perform(post("/investors/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "token": "invalid-token",
                              "password": "Password123"
                            }
                            """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400));
    }

    @Test
    void setPassword_whenAccountAlreadyExists_returns409() throws Exception {
        InvestorApplication application = saveApplication("duplicate-account@example.com", InvestorApplicationStatus.ACCEPTED);
        String token = extractToken(setPasswordTokenService.createSetPasswordUrl(application));
        investorAccountRepository.save(
                InvestorAccount.builder()
                        .email(application.getEmail())
                        .passwordHash(passwordEncoder.encode("Password123"))
                        .enabled(true)
                        .application(application)
                        .build()
        );

        mockMvc.perform(post("/investors/auth/set-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                              "token": "%s",
                              "password": "Password123"
                            }
                            """.formatted(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.statusCode").value(409));
    }

    private void stubPresignedUrl() throws Exception {
        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        when(mockPresigned.url()).thenReturn(URI.create("http://fake-s3/presigned").toURL());
        when(s3Service.presignUrl(anyString())).thenReturn(mockPresigned);
    }

    private InvestorApplication saveApplication(String email, InvestorApplicationStatus status) {
        return investorApplicationRepository.saveAndFlush(
                InvestorApplication.builder()
                        .firstname("John")
                        .lastname("Doe")
                        .email(email)
                        .applicationStatus(status)
                        .build()
        );
    }

    private String extractToken(String setPasswordUrl) {
        return setPasswordUrl.substring(setPasswordUrl.indexOf("token=") + 6);
    }

}
