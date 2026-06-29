package com.alerthub.logger_service.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
// what producers send over kafka 
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String serviceName;
    private String logLevel;   // INFO, DEBUG, WARN, ERROR
    private String message;
    private Instant timestamp;  // optional: logger fills it if null
}