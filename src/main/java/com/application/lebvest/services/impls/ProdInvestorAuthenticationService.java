package com.application.lebvest.services.impls;

import com.application.lebvest.config.FrontendProperties;
import com.application.lebvest.config.MailProperties;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.DocumentPresignedUrlsResponseDto;
import com.application.lebvest.models.dtos.InvestorSignupDocumentsDto;
import com.application.lebvest.models.dtos.InvestorSignupRequestDto;
import com.application.lebvest.producers.AdminEventsProducer;
import com.application.lebvest.producers.InvestorEventsProducer;
import com.application.lebvest.repositories.InvestorSignupRequestRepository;
import com.application.lebvest.services.interfaces.InvestorAuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;

@Profile("prod")
@Service
@RequiredArgsConstructor
@Slf4j
public class ProdInvestorAuthenticationService implements InvestorAuthenticationService {

    private final InvestorEventsProducer investorEventsProducer;
    private final InvestorSignupRequestRepository investorSignupRequestRepository;
    private final ModelMapper modelMapper;
    private final TemplateRendererService templateRendererService;
    private final MailProperties mailProperties;
    private final FrontendProperties frontendProperties;
    private final AdminEventsProducer adminEventsProducer;
    private final ProdS3Service prodS3Service;


    @Override
    public ApiResponseDto<Map<String, DocumentPresignedUrlsResponseDto>> processInvestorSignupRequest(InvestorSignupRequestDto request, InvestorSignupDocumentsDto requestDocs) {
        return null;
    }

}
