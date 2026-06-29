package com.alerthub.sms_service.dto;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// direction -> producer(out)
//Record that send succeeded/ filed

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogMessage {
    private String serviceName;
    private String logLevel;
    private String message;
    private Instant timestamp;


}
