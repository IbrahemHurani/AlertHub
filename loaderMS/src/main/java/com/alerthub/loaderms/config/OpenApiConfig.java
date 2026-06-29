package com.alerthub.loaderms.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loaderOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AlertHub - Loader Microservice")
                        .description("Reads data files from GitHub, Jira, and ClickUp providers, transforms them, and stores records in the platform_information table.")
                        .version("1.0.0"));
    }
}
