package com.alerthub.processorms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class AlertHubApplication {
//first commit
    public static void main(String[] args) {
        SpringApplication.run(AlertHubApplication.class, args);
    }

}
