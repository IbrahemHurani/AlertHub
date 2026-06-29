package com.alerthub.loaderms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AlertHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlertHubApplication.class, args);
    }

}
