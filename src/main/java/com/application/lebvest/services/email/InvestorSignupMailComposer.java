package com.application.lebvest.services.email;

import com.application.lebvest.models.entities.InvestorSignupApplication;
import com.application.lebvest.models.events.InvestorSignupApplicationAdminNotificationEmail;
import com.application.lebvest.models.events.InvestorSignupApplicationConfirmationEmail;
import com.application.lebvest.properties.AppFrontendProperties;
import com.application.lebvest.properties.AppMailProperties;
import com.application.lebvest.services.TemplateRendererService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Year;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class InvestorSignupMailComposer {

    private final TemplateRendererService templateRendererService;
    private final AppMailProperties mailProperties;
    private final AppFrontendProperties frontendProperties;

    public InvestorSignupApplicationConfirmationEmail composeInvestorSignupConfirmation(
            InvestorSignupApplication application) throws IOException {
        String styles = templateRendererService.loadStyles("emails", "investor-signup-confirmation");
        String html = templateRendererService.renderMailTemplate("investor-signup-confirmation", Map.of(
                "styles", styles,
                "title", mailProperties.getTitles().getInvestorSignupConfirmation(),
                "year", Year.now().toString(),
                "firstName", application.getFirstName(),
                "lastName", application.getLastName(),
                "email", application.getEmail(),
                "phoneNumber", application.getPhoneNumber(),
                "nationality", application.getNationality(),
                "countryOfResidence", application.getCountryOfResidence()));

        return InvestorSignupApplicationConfirmationEmail.builder()
                .from(mailProperties.getFrom())
                .to(application.getEmail())
                .subject(mailProperties.getSubjects().getInvestorSignupConfirmation())
                .html(html)
                .build();
    }

    public InvestorSignupApplicationAdminNotificationEmail composeAdminSignupNotification(
            InvestorSignupApplication application) throws IOException {
        String adminDashboardUrl = frontendProperties.getBaseUrl() + "/admin/applications/" + application.getId();
        String styles = templateRendererService.loadStyles("emails", "admin-signup-notification");

        Map<String, String> context = new HashMap<>();
        context.put("styles", styles);
        context.put("title", mailProperties.getTitles().getAdminSignupNotification());
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

        return InvestorSignupApplicationAdminNotificationEmail.builder()
                .from(mailProperties.getFrom())
                .to(mailProperties.getAdminEmail())
                .subject(mailProperties.getSubjects().getAdminSignupNotification())
                .html(html)
                .build();
    }
}
