package com.application.lebvest.services;

import com.application.lebvest.InvestorApplicationRepository;
import com.application.lebvest.models.dtos.ApiResponseDto;
import com.application.lebvest.models.dtos.InvestorApplicationRequestDto;
import com.application.lebvest.models.dtos.InvestorApplicationResponseDto;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.enums.InvestorApplicationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestorApplicationService {

    private final InvestorApplicationRepository investorApplicationRepository;
    private static final String INVESTOR_DOCUMENTS_PATH_PREFIX = "investor_documents/pending";

    public ApiResponseDto<InvestorApplicationResponseDto> apply(
            InvestorApplicationRequestDto req
    ) {
        if (investorApplicationRepository.existsByEmail(req.email())) {
            //TODO: Replace with custom exception
            throw new RuntimeException("Investor application with this email already exists");
        }

        var application = InvestorApplication.builder()
                .firstname(req.firstname())
                .lastname(req.lastname())
                .email(req.email())
                .applicationStatus(InvestorApplicationStatus.PENDING)
                .build();

        application = investorApplicationRepository.save(application);
        Long requestId = application.getId();

        String identityKey = generateDocumentKey(req.identityDocumentName(), requestId);
        String addressKey = generateDocumentKey(req.addressProofDocumentName(), requestId);

        application.setIdentityDocumentKey(identityKey);
        application.setAddressProofDocumentKey(addressKey);
        investorApplicationRepository.save(application);

        // TODO: replace with real presigned URLs once object storage is integrated
        var data = InvestorApplicationResponseDto.builder()
                .identityDocumentPresignedUrl("pending-presign:" + identityKey)
                .addressProofDocumentPresignedUrl("pending-presign:" + addressKey)
                .build();

        //TODO: Send emails
        return ApiResponseDto.ok(HttpStatus.CREATED.value(), data);
    }

    private String generateDocumentKey(String filename, Long requestId) {
        LocalDate d = LocalDate.now();
        String baseName = normalizeFilename(filename);
        String objectId = UUID.randomUUID().toString();
        return String.format(
                "%s/%04d/%02d/%02d/%d/%s_%s",
                INVESTOR_DOCUMENTS_PATH_PREFIX,
                d.getYear(),
                d.getMonthValue(),
                d.getDayOfMonth(),
                requestId,
                objectId,
                baseName
        );
    }

    private static String normalizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return "file";
        }

        // 1. Remove path traversal (both / and \)
        String name = filename.replace('\\', '/');
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
            name = name.substring(lastSlash + 1);
        }

        // 2. Normalize Unicode (avoid sneaky homoglyph attacks)
        name = Normalizer.normalize(name, Normalizer.Form.NFKC);

        // 3. Trim + lowercase
        name = name.trim().toLowerCase();

        // 4. Replace spaces with underscores
        name = name.replaceAll("\\s+", "_");

        // 5. Remove anything not safe (keep letters, numbers, dot, underscore, dash)
        name = name.replaceAll("[^a-z0-9._-]", "");

        // 6. Prevent hidden files like ".env"
        if (name.startsWith(".")) {
            name = "file" + name;
        }

        // 7. Handle extension safely (avoid "file.pdf.exe")
        int lastDot = name.lastIndexOf('.');
        if (lastDot != -1) {
            String base = name.substring(0, lastDot);
            String ext = name.substring(lastDot + 1);

            // Optional: enforce allowed extensions
            ext = ext.length() > 10 ? ext.substring(0, 10) : ext;

            name = base + "." + ext;
        }

        // 8. Limit length (S3 key sanity)
        int MAX_LENGTH = 100;
        if (name.length() > MAX_LENGTH) {
            name = name.substring(0, MAX_LENGTH);
        }

        // 9. Fallback if empty after cleaning
        if (name.isBlank()) {
            name = "file";
        }

        return name;
    }
}
