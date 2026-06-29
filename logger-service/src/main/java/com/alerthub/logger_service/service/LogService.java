package com.alerthub.logger_service.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.alerthub.logger_service.dto.LogMessage;
import com.alerthub.logger_service.model.LogEntry;
import com.alerthub.logger_service.repository.LogRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    public LogEntry save(LogMessage message) {
        LogEntry entry = LogEntry.builder()
                .serviceName(message.getServiceName())
                .logLevel(message.getLogLevel())
                .message(message.getMessage())
                .timestamp(message.getTimestamp() != null ? message.getTimestamp() : Instant.now())
                .build();
        return logRepository.save(entry);
    }

    public List<LogEntry> getAll() {
        return logRepository.findAll();
    }

    public List<LogEntry> getByService(String serviceName) {
        return logRepository.findByServiceName(serviceName);
    }

    public List<LogEntry> getByLevel(String logLevel) {
        return logRepository.findByLogLevel(logLevel);
    }
}