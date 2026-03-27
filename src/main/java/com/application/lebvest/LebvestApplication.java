package com.application.lebvest;

import com.application.lebvest.properties.AppFrontendProperties;
import com.application.lebvest.properties.AppMailProperties;
import com.application.lebvest.properties.AppTemplateProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({
        AppMailProperties.class,
        AppTemplateProperties.class,
        AppFrontendProperties.class
})
public class LebvestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LebvestApplication.class, args);
    }

}
