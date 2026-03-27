package com.application.lebvest.services.interfaces;

import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorSignupApplicationResponseDto;

public interface InvestorAuthenticationService {

    ApiResponseDto<InvestorSignupApplicationResponseDto> applyToSignup(
            InvestorSignupApplicationRequestDto requestDto
    );
}
