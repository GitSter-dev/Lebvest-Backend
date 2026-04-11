package com.application.lebvest.unit;

import com.application.lebvest.repositories.InvestorApplicationRepository;
import com.application.lebvest.configs.AdminProperties;
import com.application.lebvest.models.dtos.InvestorApplicationRequestDto;
import com.application.lebvest.producers.InvestorEventsProducer;
import com.application.lebvest.services.HandlebarsRendererService;
import com.application.lebvest.services.InvestorApplicationService;
import com.application.lebvest.services.S3Service;
import com.application.lebvest.services.StylesLoaderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class InvestorApplicationServiceTest {

    @InjectMocks
    InvestorApplicationService investorApplicationService;


    @Mock
    InvestorApplicationRepository investorApplicationRepository;

    @Mock
    S3Service s3Service;

    @Mock
    Clock clock;

    @Mock
    Supplier<UUID> uuidSupplier;

    @Mock
    InvestorEventsProducer investorEventsProducer;

    @Mock
    HandlebarsRendererService handlebarsRendererService;

    @Mock
    StylesLoaderService stylesLoaderService;

    @Mock
    AdminProperties adminProperties;

    private static final LocalDate FIXED_DATE = LocalDate.of(2025, 6, 15);
    private static final UUID FIXED_UUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final String EXPECTED_PREFIX = "investor-documents/pending";

    private void stubClockAndUuid(LocalDate date) {
        when(clock.instant()).thenReturn(date.atStartOfDay(ZoneOffset.UTC).toInstant());
        when(clock.getZone()).thenReturn(ZoneOffset.UTC);
        when(uuidSupplier.get()).thenReturn(FIXED_UUID);
    }


    @Test
    void normalizeFilename_whenFilenameIsNull_returnsLiteralFile() {
        assertEquals("file", InvestorApplicationService.normalizeFilename(null));
    }

    @Test
    void normalizeFilename_whenFilenameIsBlank_returnsLiteralFile() {
        assertEquals("file", InvestorApplicationService.normalizeFilename(""));
    }

    @Test
    void normalizeFilename_whenFilenameContainsSlashes_returnsStringAfterLastSlash() {
        assertEquals("filename.pdf", InvestorApplicationService.normalizeFilename("\\test1/test2//test3/\\test3\\//filename.pdf"));
    }

    @Test
    void normalizeFilename_whenFilenameContainsUppercase_returnsLowercase() {
        assertEquals("document.pdf", InvestorApplicationService.normalizeFilename("DOCUMENT.PDF"));
    }

    @Test
    void normalizeFilename_whenFilenameContainsSpaces_replacesWithUnderscores() {
        assertEquals("my_document.pdf", InvestorApplicationService.normalizeFilename("my document.pdf"));
    }

    @Test
    void normalizeFilename_whenFilenameContainsUnsafeChars_removesUnsafeChars() {
        assertEquals("file.pdf", InvestorApplicationService.normalizeFilename("fi@le!.pdf"));
    }

    @Test
    void normalizeFilename_whenFilenameStartsWithDot_prependsLiteralFile() {
        assertEquals("file.env", InvestorApplicationService.normalizeFilename(".env"));
    }

    @Test
    void normalizeFilename_whenExtensionExceeds10Chars_truncatesExtension() {
        assertEquals("file.abcdefghij", InvestorApplicationService.normalizeFilename("file.abcdefghijklmnop"));
    }

    @Test
    void normalizeFilename_whenFilenameExceeds100Chars_truncatesTo100Chars() {
        String tooLong = "a".repeat(101);
        assertTrue(InvestorApplicationService.normalizeFilename(tooLong).length() <= 100);
    }

    @Test
    void normalizeFilename_whenFilenameBecomesEmptyAfterCleaning_returnsLiteralFile() {
        assertEquals("file", InvestorApplicationService.normalizeFilename("@@@!!!###"));
    }

    @Test
    void normalizeFilename_whenFilenameContainsUnicodeHomoglyphs_normalizesCorrectly() {
        String homoglyph = "\uFF41\uFF42\uFF43.pdf";
        assertEquals("abc.pdf", InvestorApplicationService.normalizeFilename(homoglyph));
    }

    @Test
    void normalizeFilename_whenFilenameHasLeadingAndTrailingSpaces_trimsThem() {
        assertEquals("document.pdf", InvestorApplicationService.normalizeFilename("  document.pdf  "));
    }

    @Test
    void generateDocumentKey_whenValid_returnsExactExpectedKey() {
        stubClockAndUuid(FIXED_DATE);
        String result = investorApplicationService.generateDocumentKey("document.pdf", 42L);
        assertEquals(
                "investor-documents/pending/2025/06/15/42/123e4567-e89b-12d3-a456-426614174000_document.pdf",
                result
        );
    }

    @Test
    void generateDocumentKey_whenFilenameIsNull_throwsException() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> investorApplicationService.generateDocumentKey(null, 42L));
        assertEquals("filename and requestId must not be null", ex.getMessage());
    }

    @Test
    void generateDocumentKey_whenRequestIdIsNull_throwsException() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> investorApplicationService.generateDocumentKey("document.pdf", null));
        assertEquals("filename and requestId must not be null", ex.getMessage());
    }

    @Test
    void generateDocumentKey_whenBothNull_throwsException() {
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> investorApplicationService.generateDocumentKey(null, null));
        assertEquals("filename and requestId must not be null", ex.getMessage());
    }

    @Test
    void generateDocumentKey_alwaysStartsWithPathPrefix() {
        stubClockAndUuid(FIXED_DATE);
        String result = investorApplicationService.generateDocumentKey("file.pdf", 1L);
        assertTrue(result.startsWith(EXPECTED_PREFIX));
    }

    @Test
    void generateDocumentKey_containsRequestId() {
        stubClockAndUuid(FIXED_DATE);
        String result = investorApplicationService.generateDocumentKey("file.pdf", 99L);
        assertTrue(result.contains("/99/"));
    }

    @Test
    void generateDocumentKey_containsFormattedDate() {
        stubClockAndUuid(FIXED_DATE);
        String result = investorApplicationService.generateDocumentKey("file.pdf", 1L);
        assertTrue(result.contains("/2025/06/15/"));
    }

    @Test
    void generateDocumentKey_whenFilenameHasSlashes_normalizesBeforeAppending() {
        stubClockAndUuid(FIXED_DATE);
        String result = investorApplicationService.generateDocumentKey("some/path/document.pdf", 1L);
        assertTrue(result.endsWith("_document.pdf"));
        assertFalse(result.contains("some/path"));
    }

    @Test
    void generateDocumentKey_whenFilenameHasUppercase_lowercasesFilename() {
        stubClockAndUuid(FIXED_DATE);
        String result = investorApplicationService.generateDocumentKey("DOCUMENT.PDF", 1L);
        assertTrue(result.endsWith("_document.pdf"));
    }

    @Test
    void generateDocumentKey_monthAndDayArePaddedWithZero() {
        stubClockAndUuid(LocalDate.of(2025, 6, 5));
        String result = investorApplicationService.generateDocumentKey("file.pdf", 1L);
        assertTrue(result.contains("/2025/06/05/"));
    }
}