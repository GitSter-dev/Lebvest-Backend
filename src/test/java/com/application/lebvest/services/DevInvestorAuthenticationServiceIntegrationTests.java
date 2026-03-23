package com.application.lebvest.services;

import com.application.lebvest.TestcontainersConfiguration;
import com.application.lebvest.models.dtos.InvestorSignupDocumentsDto;
import com.application.lebvest.models.dtos.InvestorSignupRequestDto;
import com.application.lebvest.models.dtos.InvestorSignupRequestWithDocsDto;
import com.application.lebvest.models.entities.InvestorSignupRequest;
import com.application.lebvest.models.enums.InvestorSignupRequestStatus;
import com.application.lebvest.repositories.InvestorSignupRequestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
public class DevInvestorAuthenticationServiceIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private InvestorSignupRequestRepository investorSignupRequestRepository;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private static final String CONTEXT_PATH = "/api/v1";
    private static final String SIGNUP_URL = CONTEXT_PATH + "/auth/investor/request-signup";

    @BeforeEach
    void setUp() {
        investorSignupRequestRepository.deleteAll();
    }

    private InvestorSignupRequestDto buildValidRequest() {
        return new InvestorSignupRequestDto(
                "John",
                "Doe",
                "john.doe@example.com",
                "+961123456",
                LocalDate.of(1990, 5, 15),
                "Lebanese",
                "Lebanon",
                "Beirut, Lebanon",
                "Software Engineer",
                "Employment",
                75000L,
                false,
                false,
                false,
                "TAX123456",
                "Moderate",
                5
        );
    }

    private InvestorSignupDocumentsDto buildValidDocuments() {
        return new InvestorSignupDocumentsDto(
                "national-id.pdf",
                "proof-of-residence.pdf",
                List.of("address-proof.pdf"),
                List.of("bank-statement.pdf")
        );
    }

    private InvestorSignupRequestWithDocsDto buildValidRequestWrapper() {
        return new InvestorSignupRequestWithDocsDto(buildValidRequest(), buildValidDocuments());
    }

    @Test
    void processInvestorSignupRequest_shouldReturnSuccessAndPersist() throws Exception {
        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequestWrapper())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusCode").value(201))
                .andExpect(jsonPath("$.data.presignedUrls").isMap())
                .andExpect(jsonPath("$.data.presignedUrls.nationalIdOrPassportPresignedUrl").isString())
                .andExpect(jsonPath("$.data.presignedUrls.proofOfResidencePresignedUrl").isString())
                .andExpect(jsonPath("$.data.presignedUrls.addressProofDocumentsPresignedUrls").isMap())
                .andExpect(jsonPath("$.data.presignedUrls.sourceOfFundsDocumentsPresignedUrls").isMap());

        List<InvestorSignupRequest> requests = investorSignupRequestRepository.findAll();
        assertThat(requests).hasSize(1);

        InvestorSignupRequest saved = requests.get(0);
        assertThat(saved.getFirstName()).isEqualTo("John");
        assertThat(saved.getLastName()).isEqualTo("Doe");
        assertThat(saved.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(saved.getStatus()).isEqualTo(InvestorSignupRequestStatus.PENDING);
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void processInvestorSignupRequest_shouldPersistAllFields() throws Exception {
        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildValidRequestWrapper())))
                .andExpect(status().isOk());

        InvestorSignupRequest saved = investorSignupRequestRepository.findAll().get(0);

        assertThat(saved.getPhoneNumber()).isEqualTo("+961123456");
        assertThat(saved.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 5, 15));
        assertThat(saved.getNationality()).isEqualTo("Lebanese");
        assertThat(saved.getCountryOfResidence()).isEqualTo("Lebanon");
        assertThat(saved.getAddress()).isEqualTo("Beirut, Lebanon");
        assertThat(saved.getOccupation()).isEqualTo("Software Engineer");
        assertThat(saved.getSourceOfFunds()).isEqualTo("Employment");
        assertThat(saved.getEstimatedAnnualIncome()).isEqualTo(75000L);
        assertThat(saved.getPep()).isFalse();
        assertThat(saved.getRelativeOrFamilyPepStatus()).isFalse();
        assertThat(saved.getIsUsPerson()).isFalse();
        assertThat(saved.getTaxIdNumber()).isEqualTo("TAX123456");
        assertThat(saved.getRiskTolerance()).isEqualTo("Moderate");
        assertThat(saved.getYearsOfExperience()).isEqualTo(5);
    }

    @Test
    void processInvestorSignupRequest_withMissingFirstName_shouldReturn400() throws Exception {
        InvestorSignupRequestDto invalidRequest = new InvestorSignupRequestDto(
                "", "Doe", "john.doe@example.com", "+961123456",
                LocalDate.of(1990, 5, 15), "Lebanese", "Lebanon", "Beirut, Lebanon",
                "Software Engineer", "Employment", 75000L,
                false, false, false, "TAX123456", "Moderate", 5
        );

        InvestorSignupRequestWithDocsDto wrapper =
                new InvestorSignupRequestWithDocsDto(invalidRequest, buildValidDocuments());

        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.message").value("Validation failed"));

        assertThat(investorSignupRequestRepository.findAll()).isEmpty();
    }

    @Test
    void processInvestorSignupRequest_withInvalidEmail_shouldReturn400() throws Exception {
        InvestorSignupRequestDto invalidRequest = new InvestorSignupRequestDto(
                "John", "Doe", "not-an-email", "+961123456",
                LocalDate.of(1990, 5, 15), "Lebanese", "Lebanon", "Beirut, Lebanon",
                "Software Engineer", "Employment", 75000L,
                false, false, false, "TAX123456", "Moderate", 5
        );

        InvestorSignupRequestWithDocsDto wrapper =
                new InvestorSignupRequestWithDocsDto(invalidRequest, buildValidDocuments());

        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest());

        assertThat(investorSignupRequestRepository.findAll()).isEmpty();
    }

    @Test
    void processInvestorSignupRequest_withFutureDateOfBirth_shouldReturn400() throws Exception {
        InvestorSignupRequestDto invalidRequest = new InvestorSignupRequestDto(
                "John", "Doe", "john.doe@example.com", "+961123456",
                LocalDate.now().plusYears(1), "Lebanese", "Lebanon", "Beirut, Lebanon",
                "Software Engineer", "Employment", 75000L,
                false, false, false, "TAX123456", "Moderate", 5
        );

        InvestorSignupRequestWithDocsDto wrapper =
                new InvestorSignupRequestWithDocsDto(invalidRequest, buildValidDocuments());

        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest());

        assertThat(investorSignupRequestRepository.findAll()).isEmpty();
    }

    @Test
    void processInvestorSignupRequest_withBlankNationalId_shouldReturn400() throws Exception {
        InvestorSignupDocumentsDto invalidDocs = new InvestorSignupDocumentsDto(
                "", "proof-of-residence.pdf",
                List.of("address-proof.pdf"),
                List.of("bank-statement.pdf")
        );

        InvestorSignupRequestWithDocsDto wrapper =
                new InvestorSignupRequestWithDocsDto(buildValidRequest(), invalidDocs);

        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrapper)))
                .andExpect(status().isBadRequest());

        assertThat(investorSignupRequestRepository.findAll()).isEmpty();
    }

    @Test
    void processInvestorSignupRequest_withNullBody_shouldReturn400() throws Exception {
        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void processInvestorSignupRequest_duplicateEmail_shouldFail() throws Exception {
        String body = objectMapper.writeValueAsString(buildValidRequestWrapper());

        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk());

        mockMvc.perform(post(SIGNUP_URL).contextPath(CONTEXT_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict());
    }
}
