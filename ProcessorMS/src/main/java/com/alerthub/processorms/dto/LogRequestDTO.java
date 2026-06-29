package com.alerthub.processorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LogRequestDTO {
    private LocalDateTime timestamp;
    private String serviceName;
    private String logLevel;
    private String message;
}
