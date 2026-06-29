package com.alerthub.processorms.client;


import com.alerthub.processorms.dto.LogRequestDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "logger-service", url = "${services.logger-service.url}")
public interface LoggerClient {
    @PostMapping("/api/logs")
    void sendLog(@RequestBody LogRequestDTO logRequest);
}
