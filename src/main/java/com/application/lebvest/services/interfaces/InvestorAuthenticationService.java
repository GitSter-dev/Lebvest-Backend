package com.application.lebvest.services.interfaces;


import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.DocumentPresignedUrlsResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupDocumentsDto;
import com.application.lebvest.models.dtos.InvestorSignupRequestDto;

import java.util.Map;

public interface InvestorAuthenticationService {

    ApiResponseDto<Map<String, DocumentPresignedUrlsResponseDto>> processInvestorSignupRequest(InvestorSignupRequestDto request,
                                                                                               InvestorSignupDocumentsDto requestDocs);

}
