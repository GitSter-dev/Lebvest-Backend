package com.application.auth_lebvest.models.dtos;

import java.util.Map;

/**
 * This class groups static classes representing api response data payloads
 */
public class ApiResponses {

    public record InvestorSignupData(Map<String, String> presignedUrls) {}

}
