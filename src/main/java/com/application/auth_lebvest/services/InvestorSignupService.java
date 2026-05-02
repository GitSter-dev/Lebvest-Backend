package com.application.auth_lebvest.services;

import com.application.auth_lebvest.models.dtos.ApiResponses;
import com.application.auth_lebvest.models.dtos.ApiSuccessResponse;
import com.application.auth_lebvest.models.dtos.InvestorSignup;


public interface InvestorSignupService {
    ApiSuccessResponse<ApiResponses.InvestorSignupData> signup(InvestorSignup investorSignup);

}
