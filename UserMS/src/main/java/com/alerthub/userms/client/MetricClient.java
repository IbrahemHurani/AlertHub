package com.alerthub.userms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "metrics-service", url = "${app.services.metrics-url}")
public interface MetricClient {

    @PostMapping("/metrics")
    Object createMetric(@RequestBody Object metric);

    @GetMapping("/metrics")
    List<Object> getAllMetrics();

    @GetMapping("/metrics/{id}")
    Object getMetricById(@PathVariable("id") Long id);

    @GetMapping("/metrics/user/{userId}")
    List<Object> getMetricsByUserId(@PathVariable("userId") Integer userId);

    @PutMapping("/metrics/{id}")
    Object updateMetric(@PathVariable("id") Long id, @RequestBody Object metric);

    @DeleteMapping("/metrics/{id}")
    String deleteMetric(@PathVariable("id") Long id);
}