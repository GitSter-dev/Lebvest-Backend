package com.application.lebvest;

import org.springframework.boot.SpringApplication;

public class TestLebvestApplication {

    public static void main(String[] args) {
        SpringApplication.from(LebvestApplication::main).with(TestcontainersConfiguration.class).run(args);
    }

}
