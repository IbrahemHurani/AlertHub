package com.alerthub.userms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "action-service", url = "${app.services.action-url}")
public interface ActionClient {

    @PostMapping("/actions")
    Object createAction(@RequestBody Object action);

    @GetMapping("/actions")
    List<Object> getAllActions();

    @GetMapping("/actions/{id}")
    Object getActionById(@PathVariable("id") Long id);

    @PutMapping("/actions/{id}")
    Object updateAction(@PathVariable("id") Long id, @RequestBody Object action);

    @DeleteMapping("/actions/{id}")
    String deleteAction(@PathVariable("id") Long id);

    // حافِظي على هذه الـ Endpoint في الـ Action MS لتشغيل الآكشن فوراً بشكل يدوي
    @PostMapping("/actions/{id}/trigger-manual")
    void triggerManualProcess(@PathVariable("id") Long id);
}