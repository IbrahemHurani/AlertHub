package com.alerthub.processorms.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "loader-service", url = "${services.loader-service.url}")
public interface LoaderClient {
    @GetMapping("/api/platform-info/count")
    long countLabelTasks(
            @RequestParam("label") String label,
            @RequestParam("hours") int hours,
            @RequestParam("managerId") String managerId
    );
}
