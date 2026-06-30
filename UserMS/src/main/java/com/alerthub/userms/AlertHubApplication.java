package com.alerthub.userms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "com.alerthub.userms.client")
public class AlertHubApplication {
//first commit
    public static void main(String[] args) {
        SpringApplication.run(AlertHubApplication.class, args);
    }

}
