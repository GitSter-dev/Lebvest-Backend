package com.application.lebvest.services;

import com.application.lebvest.models.entities.InvestorSignupApplication;
import com.application.lebvest.properties.AppFrontendProperties;
import com.application.lebvest.properties.AppMailProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestorSignupEmailService {

    private final MailService mailService;
    private final TemplateRendererService templateRendererService;
    private final AppMailProperties mailProperties;
    private final AppFrontendProperties frontendProperties;

    public void sendConfirmationEmail(InvestorSignupApplication application) throws IOException {
        String styles = templateRendererService.loadStyles("emails", "investor-signup-confirmation");
        String html = templateRendererService.renderMailTemplate("investor-signup-confirmation", Map.of(
                "styles", styles,
                "title", "Application Received",
                "year", Year.now().toString(),
                "firstName", application.getFirstName(),
                "lastName", application.getLastName(),
                "email", application.getEmail(),
                "phoneNumber", application.getPhoneNumber(),
                "nationality", application.getNationality(),
                "countryOfResidence", application.getCountryOfResidence()
        ));

        mailService.sendHtmlMail(
                mailProperties.getSubjects().getInvestorSignupConfirmation(),
                application.getEmail(),
                html
        );
        log.info("Sent signup confirmation email to [{}]", application.getEmail());
    }

    public void sendAdminNotificationEmail(InvestorSignupApplication application) throws IOException {
        String adminDashboardUrl = frontendProperties.getBaseUrl() + "/admin/applications/" + application.getId();
        String styles = templateRendererService.loadStyles("emails", "admin-signup-notification");

        Map<String, String> context = new HashMap<>();
        context.put("styles", styles);
        context.put("title", "New Investor Application");
        context.put("year", Year.now().toString());
        context.put("firstName", application.getFirstName());
        context.put("lastName", application.getLastName());
        context.put("email", application.getEmail());
        context.put("phoneNumber", application.getPhoneNumber());
        context.put("nationality", application.getNationality());
        context.put("countryOfResidence", application.getCountryOfResidence());
        context.put("applicationId", application.getId().toString());
        context.put("adminDashboardUrl", adminDashboardUrl);

        String html = templateRendererService.renderMailTemplate("admin-signup-notification", context);

        mailService.sendHtmlMail(
                mailProperties.getSubjects().getAdminSignupNotification(),
                mailProperties.getAdminEmail(),
                html
        );
        log.info("Sent admin notification email for application [{}]", application.getId());
    }
}
