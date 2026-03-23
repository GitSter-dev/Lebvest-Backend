package com.application.lebvest;

import com.application.lebvest.config.QueueProperties;
//import io.github.cdimascio.dotenv.Dotenv;
import com.application.lebvest.config.S3Properties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({QueueProperties.class, S3Properties.class})

public class LebvestApplication {

    public static void main(String[] args) {

//        String profile = System.getenv("SPRING_PROFILES_ACTIVE");
//        Dotenv envConfig = Dotenv.configure().filename(".env." + profile).load();
//        envConfig.entries().forEach((dotenvEntry -> {
//            System.setProperty(dotenvEntry.getKey(), dotenvEntry.getValue());
//        }));
        SpringApplication.run(LebvestApplication.class, args);
    }

}
