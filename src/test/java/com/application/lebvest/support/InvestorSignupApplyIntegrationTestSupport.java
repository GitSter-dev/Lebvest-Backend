package com.application.lebvest.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.params.provider.Arguments;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

public final class InvestorSignupApplyIntegrationTestSupport {

    public static final String APPLY_PATH = "/investor/auth/apply-to-signup";

    private InvestorSignupApplyIntegrationTestSupport() {
    }

    public static Map<String, Object> validPayload(String email) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("firstName", "Ada");
        m.put("lastName", "Lovelace");
        m.put("email", email);
        m.put("phoneNumber", "+441234567890");
        m.put("nationality", "British");
        m.put("countryOfResidence", "United Kingdom");
        m.put("identityDocument", "passport.pdf");
        m.put("proofOfResidenceDocument", "bill.pdf");
        m.put("sourceOfFundsDocuments", List.of("statement.pdf"));
        return m;
    }

    public static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.com";
    }

    public static Stream<Arguments> validationCases() {
        return Stream.of(
                args("firstName is blank", set("firstName", ""), "firstName"),
                args("firstName is omitted", remove("firstName"), "firstName"),
                args("lastName is blank", set("lastName", "   "), "lastName"),
                args("lastName is omitted", remove("lastName"), "lastName"),
                args("email is blank", set("email", ""), "email"),
                args("email is invalid", set("email", "not-an-email"), "email"),
                args("phoneNumber is blank", set("phoneNumber", ""), "phoneNumber"),
                args("nationality is blank", set("nationality", ""), "nationality"),
                args("countryOfResidence is blank", set("countryOfResidence", ""), "countryOfResidence"),
                args("identityDocument is blank", set("identityDocument", ""), "identityDocument"),
                args("proofOfResidence is blank", set("proofOfResidenceDocument", ""), "proofOfResidenceDocument"),
                args("sourceOfFunds is empty", set("sourceOfFundsDocuments", List.of()), "sourceOfFundsDocuments"),
                args("sourceOfFunds is null", set("sourceOfFundsDocuments", null), "sourceOfFundsDocuments"),
                args("sourceOfFunds is omitted", remove("sourceOfFundsDocuments"), "sourceOfFundsDocuments")
        );
    }

    private static Arguments args(String scenario, UnaryOperator<Map<String, Object>> patch, String... fields) {
        return Arguments.of(scenario, patch, List.of(fields));
    }

    public static UnaryOperator<Map<String, Object>> set(String key, Object value) {
        return m -> {
            m.put(key, value);
            return m;
        };
    }

    public static UnaryOperator<Map<String, Object>> remove(String key) {
        return m -> {
            m.remove(key);
            return m;
        };
    }

    public static ResponseEntity<String> postSignup(
            RestClient restClient,
            ObjectMapper objectMapper,
            Map<String, Object> body) throws Exception {
        return restClient.post()
                .uri(APPLY_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(body))
                .exchange((req, res) -> {
                    String text = new String(res.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    return ResponseEntity.status(res.getStatusCode()).body(text);
                });
    }

    public static JsonNode parseBody(ObjectMapper objectMapper, ResponseEntity<String> response) throws Exception {
        return objectMapper.readTree(response.getBody());
    }

    public static void assertValidationError(
            ObjectMapper objectMapper,
            ResponseEntity<String> response,
            List<String> expectedFields) throws Exception {
        assertThat(response.getStatusCode().value()).isEqualTo(400);
        JsonNode root = parseBody(objectMapper, response);
        assertThat(root.get("error").get("message").asText()).isEqualTo("Validation failed");
        JsonNode details = root.get("error").get("details");
        assertThat(details).isNotNull();
        for (String field : expectedFields) {
            assertThat(details.has(field)).as("expected error for field: %s", field).isTrue();
            assertThat(details.get(field).asText()).isNotBlank();
        }
    }
}
