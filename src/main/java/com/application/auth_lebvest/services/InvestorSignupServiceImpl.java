package com.application.auth_lebvest.services;

import com.application.auth_lebvest.models.dtos.ApiResponses;
import com.application.auth_lebvest.models.dtos.ApiSuccessResponse;
import com.application.auth_lebvest.models.dtos.InvestorSignup;
import com.application.auth_lebvest.models.entities.InvestorApplication;
import com.application.auth_lebvest.models.entities.User;
import com.application.auth_lebvest.models.exceptions.Exceptions;
import com.application.auth_lebvest.repositories.InvestorApplicationRepository;
import com.application.auth_lebvest.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestorSignupServiceImpl implements InvestorSignupService {

    private final InvestorApplicationRepository investorApplicationRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    public ApiSuccessResponse<ApiResponses.InvestorSignupData> signup(InvestorSignup investorSignup) {
        log.info("Checking if application exists by email: {}", investorSignup.email());

        // throw on existing application (check by email)
        if (userRepository.existsByEmail(investorSignup.email())) {
            throw new Exceptions.ResourceConflictException(
                    InvestorApplication.class,
                    "email",
                    investorSignup.email()
            );
        }

        // build investor application object, save to db and get id
        userRepository.save(User.builder()
                .firstname(investorSignup.firstname())
                .lastname(investorSignup.lastname())
                .email(investorSignup.email())
                .build());

        InvestorApplication application = InvestorApplication.builder().build();
        Long id = investorApplicationRepository.save(application).getInvestorApplicationId();

        // build doc keys from request id
        String identityKey = id + "/" + investorSignup.identityDocumentName();
        String proofOfAddressKey = id + "/" + investorSignup.proofOfAddressDocumentName();
        String selfieKey = id + "/" + investorSignup.selfieDocumentName();

        // re-save to db
        application.setIdentityDocumentKey(identityKey);
        application.setProofOfAddressDocumentKey(proofOfAddressKey);
        application.setSelfieDocumentKey(selfieKey);
        investorApplicationRepository.save(application);

        // generate presigned urls from S3Service
        Map<String, String> presignedUrls = Map.of(
                "identityDocument", s3Service.generatePresignedUrl(identityKey),
                "proofOfAddressDocument", s3Service.generatePresignedUrl(proofOfAddressKey),
                "selfieDocument", s3Service.generatePresignedUrl(selfieKey)
        );

        // build res and return
        return ApiSuccessResponse.<ApiResponses.InvestorSignupData>builder()
                .data(new ApiResponses.InvestorSignupData(presignedUrls))
                .statusCode(HttpStatus.CREATED.value())
                .message("Investor application created successfully")
                .build();
    }
}
