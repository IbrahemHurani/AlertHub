package com.alerthub.processorms.client;

import com.alerthub.processorms.dto.MetricDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "metric-service", url = "${services.metric-service.url}")
public interface MetricClient {
    @GetMapping("/api/metrics/{id}")
    MetricDTO getMetricById(@PathVariable("id") Integer id);
}
