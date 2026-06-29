package com.alerthub.logger_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alerthub.logger_service.model.LogEntry;
import com.alerthub.logger_service.service.LogService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
public class LogController {

    private final LogService logService;

    @GetMapping
    public List<LogEntry> getLogs(@RequestParam(required = false) String serviceName,@RequestParam(required = false) String level) {

        if (serviceName != null) {
            return logService.getByService(serviceName);
        }
        if (level != null) {
            return logService.getByLevel(level);
        }
        return logService.getAll();
    }
}