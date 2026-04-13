package com.application.lebvest.services;

import com.application.lebvest.configs.AdminProperties;
import com.application.lebvest.models.entities.InvestorApplication;
import com.application.lebvest.models.events.AdminEvents;
import com.application.lebvest.models.events.InvestorEvents;
import com.application.lebvest.producers.InvestorEventsProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class InvestorApplicationEmailService {

    private static final DateTimeFormatter SUBMITTED_AT_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(ZoneOffset.UTC);

    private final Clock clock;
    private final InvestorEventsProducer investorEventsProducer;
    private final HandlebarsRendererService handlebarsRendererService;
    private final StylesLoaderService stylesLoaderService;
    private final AdminProperties adminProperties;

    public void sendApplicationConfirmation(InvestorApplication application) {
        Map<String, Object> investorCtx = new HashMap<>();
        investorCtx.put("firstname", application.getFirstname());
        investorCtx.put("lastname", application.getLastname());
        investorCtx.put("email", application.getEmail());
        investorCtx.put("styles", stylesLoaderService.loadStyles("layout", "investor-application-confirmation"));
        investorCtx.put("year", currentYear());

        String html = handlebarsRendererService.renderTemplate("investor-application-confirmation", investorCtx);
        investorEventsProducer.publishInvestorEmailEvent(
                new InvestorEvents.InvestorApplicationToInvestorEmailsEvent(
                        application.getEmail(),
                        html,
                        "Application Received — Lebvest"
                )
        );
    }

    public void sendAdminApplicationNotification(InvestorApplication application) {
        Map<String, Object> adminCtx = new HashMap<>();
        adminCtx.put("adminName", adminProperties.name());
        adminCtx.put("applicationId", application.getId());
        adminCtx.put("firstname", application.getFirstname());
        adminCtx.put("lastname", application.getLastname());
        adminCtx.put("email", application.getEmail());
        adminCtx.put("submittedAt", SUBMITTED_AT_FORMATTER.format(application.getCreatedAt()));
        adminCtx.put("styles", stylesLoaderService.loadStyles("layout", "investor-application-notification"));
        adminCtx.put("year", currentYear());

        String html = handlebarsRendererService.renderTemplate("investor-application-notification", adminCtx);
        investorEventsProducer.publishAdminEmailEvent(
                new AdminEvents.InvestorApplicationToAdminEmailsEvent(
                        adminProperties.email(),
                        html,
                        "New Investor Application — Lebvest"
                )
        );
    }

    public void sendApprovalEmail(InvestorApplication application, String setPasswordUrl) {
        Map<String, Object> investorCtx = new HashMap<>();
        investorCtx.put("firstname", application.getFirstname());
        investorCtx.put("lastname", application.getLastname());
        investorCtx.put("setPasswordUrl", setPasswordUrl);
        investorCtx.put("styles", stylesLoaderService.loadStyles("layout", "investor-application-approved"));
        investorCtx.put("year", currentYear());

        String html = handlebarsRendererService.renderTemplate("investor-application-approved", investorCtx);
        investorEventsProducer.publishInvestorEmailEvent(
                new InvestorEvents.InvestorApplicationToInvestorEmailsEvent(
                        application.getEmail(),
                        html,
                        "Application Approved — Lebvest"
                )
        );
    }

    public void sendRejectionEmail(InvestorApplication application) {
        Map<String, Object> investorCtx = new HashMap<>();
        investorCtx.put("firstname", application.getFirstname());
        investorCtx.put("lastname", application.getLastname());
        investorCtx.put("styles", stylesLoaderService.loadStyles("layout", "investor-application-rejected"));
        investorCtx.put("year", currentYear());

        String html = handlebarsRendererService.renderTemplate("investor-application-rejected", investorCtx);
        investorEventsProducer.publishInvestorEmailEvent(
                new InvestorEvents.InvestorApplicationToInvestorEmailsEvent(
                        application.getEmail(),
                        html,
                        "Application Rejected — Lebvest"
                )
        );
    }

    private int currentYear() {
        return LocalDate.now(clock).getYear();
    }
}
