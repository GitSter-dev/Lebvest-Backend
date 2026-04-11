package com.application.lebvest.integration;

import com.application.lebvest.repositories.InvestorApplicationRepository;
import com.application.lebvest.TestcontainersConfiguration;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
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

    // Mock away the services that need MinIO / Mailpit / Handlebars
    @MockitoBean
    S3Service s3Service;

    @MockitoBean
    S3Presigner s3Presigner;

    @MockitoBean
    MailService mailService;

    @MockitoBean
    HandlebarsRendererService handlebarsRendererService;

    @Autowired
    SetPasswordTokenRepository setPasswordTokenRepository;

    @BeforeEach
    void setUp() {
        setPasswordTokenRepository.deleteAll();
        investorApplicationRepository.deleteAll();
    }

    @Test
    void apply_whenValidRequest_returns201() throws Exception {
        // Stub S3 to return a fake presigned URL
        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        when(mockPresigned.url()).thenReturn(URI.create("http://fake-s3/presigned").toURL());
        when(s3Service.presignUrl(anyString())).thenReturn(mockPresigned);

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
    }

    @Test
    void apply_whenDuplicateEmail_returns409() throws Exception {
        PresignedPutObjectRequest mockPresigned = mock(PresignedPutObjectRequest.class);
        when(mockPresigned.url()).thenReturn(URI.create("http://fake-s3/presigned").toURL());
        when(s3Service.presignUrl(anyString())).thenReturn(mockPresigned);

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

}
