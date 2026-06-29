package com.alerthub.logger_service.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.alerthub.logger_service.model.LogEntry;

public interface LogRepository extends MongoRepository<LogEntry, String> {
    List<LogEntry> findByServiceName(String serviceName);
    List<LogEntry> findByLogLevel(String logLevel);
}