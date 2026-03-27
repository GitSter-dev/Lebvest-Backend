package com.application.lebvest;

import com.application.lebvest.config.IntegrationTestSecurityConfiguration;
import com.application.lebvest.config.InvestorSignupApiContainersConfiguration;
import com.application.lebvest.config.InvestorSignupS3BucketTestConfiguration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestClient;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import static com.application.lebvest.support.InvestorSignupApplyIntegrationTestSupport.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for POST /investor/auth/apply-to-signup (dev stack).
 * <p>Requires Docker. Run: {@code ./mvnw -Dtest=InvestorSignupApplyIntegrationTest test}</p>
 */
@DisplayName("Investor Signup Application API (dev)")
// @Testcontainers before @SpringBootTest so AfterAll closes Spring before containers stop (clean teardown).
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"dev", "investor-api-it"})
@Import({InvestorSignupS3BucketTestConfiguration.class, IntegrationTestSecurityConfiguration.class})
class InvestorSignupApplyDevIntegrationTest extends InvestorSignupApiContainersConfiguration {

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private RestClient restClient;

    @BeforeEach
    void setUp() {
        restClient = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + port + "/api/v1")
                .build();
    }

    @Nested
    @DisplayName("Given a valid application")
    class SuccessfulApplication {

        @Test
        @DisplayName("should return 201 status in body with presigned upload URLs")
        void returnsCreatedWithPresignedUrls() throws Exception {
            var body = validPayload(uniqueEmail("happy"));

            var response = postSignup(restClient, objectMapper, body);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            JsonNode root = parseBody(objectMapper, response);
            assertThat(root.get("statusCode").asInt()).isEqualTo(201);

            JsonNode data = root.get("data");
            assertThat(data.get("identityDocumentPresignedUrl").asText()).isNotBlank();
            assertThat(data.get("proofOfResidenceDocumentPresignedUrl").asText()).isNotBlank();
            assertThat(data.get("sourceOfFundsDocumentsPresignedUrls")).hasSize(1);
        }

        @Test
        @DisplayName("should normalize uploaded filenames in generated presigned URLs")
        void normalizesFilenames() throws Exception {
            var body = validPayload(uniqueEmail("norm"));
            body.put("identityDocument", "My  Passport!!.PDF");
            body.put("proofOfResidenceDocument", "Utility Bill #1.pdf");
            body.put("sourceOfFundsDocuments", List.of("Bank Stmt!!!.pdf"));

            var response = postSignup(restClient, objectMapper, body);

            assertThat(response.getStatusCode().value()).isEqualTo(200);
            JsonNode data = parseBody(objectMapper, response).get("data");

            assertThat(data.get("identityDocumentPresignedUrl").asText())
                    .contains("my-passport.pdf");
            assertThat(data.get("proofOfResidenceDocumentPresignedUrl").asText())
                    .contains("utility-bill-1.pdf");

            String sofUrl = data.get("sourceOfFundsDocumentsPresignedUrls").elements().next().asText();
            assertThat(sofUrl).contains("bank-stmt.pdf");
        }
    }

    @Nested
    @DisplayName("Given a duplicate email")
    class DuplicateEmail {

        @Test
        @DisplayName("should return 409 when the exact same email already exists")
        void exactDuplicate() throws Exception {
            String email = uniqueEmail("dupe");
            assertThat(postSignup(restClient, objectMapper, validPayload(email)).getStatusCode().value()).isEqualTo(200);

            var second = postSignup(restClient, objectMapper, validPayload(email));

            assertThat(second.getStatusCode().value()).isEqualTo(409);
            assertThat(parseBody(objectMapper, second).get("error").get("message").asText())
                    .contains("already exists");
        }

        @Test
        @DisplayName("should return 409 when emails differ only in casing")
        void caseInsensitiveDuplicate() throws Exception {
            String unique = UUID.randomUUID().toString();
            assertThat(postSignup(restClient, objectMapper, validPayload("Mixed-" + unique + "@example.com"))
                    .getStatusCode().value()).isEqualTo(200);

            var second = postSignup(restClient, objectMapper, validPayload("mixed-" + unique + "@example.com"));

            assertThat(second.getStatusCode().value()).isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("Given invalid request payload")
    class Validation {

        @ParameterizedTest(name = "when {0}")
        @MethodSource("com.application.lebvest.support.InvestorSignupApplyIntegrationTestSupport#validationCases")
        @DisplayName("should return 400 with field-level error details")
        void rejectsSingleFieldViolation(
                String scenario,
                UnaryOperator<Map<String, Object>> patch,
                List<String> expectedFields
        ) throws Exception {
            var body = validPayload(uniqueEmail("val"));
            patch.apply(body);

            assertValidationError(objectMapper, postSignup(restClient, objectMapper, body), expectedFields);
        }

        @Test
        @DisplayName("should report all field violations when multiple fields are invalid")
        void reportsMultipleViolations() throws Exception {
            var body = validPayload(uniqueEmail("multi"));
            body.put("firstName", "");
            body.put("email", "bad");
            body.put("sourceOfFundsDocuments", List.of());

            assertValidationError(objectMapper, postSignup(restClient, objectMapper, body),
                    List.of("firstName", "email", "sourceOfFundsDocuments"));
        }
    }

    @Nested
    @DisplayName("Given a malformed request")
    class MalformedRequest {

        @Test
        @DisplayName("should return 4xx for broken JSON body")
        void malformedJson() {
            int status = restClient.post()
                    .uri(APPLY_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{")
                    .exchange((req, res) -> res.getStatusCode().value());

            assertThat(status).isBetween(400, 499);
        }

        @Test
        @DisplayName("should return 4xx for wrong Content-Type")
        void wrongContentType() throws Exception {
            int status = restClient.post()
                    .uri(APPLY_PATH)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(objectMapper.writeValueAsString(validPayload(uniqueEmail("ct"))))
                    .exchange((req, res) -> res.getStatusCode().value());

            assertThat(status).isBetween(400, 499);
        }
    }
}
