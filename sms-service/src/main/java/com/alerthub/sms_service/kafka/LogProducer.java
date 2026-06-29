package com.alerthub.sms_service.kafka;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.alerthub.sms_service.dto.LogMessage;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class LogProducer {

    private final KafkaTemplate<String, LogMessage> kafkaTemplate;

    @Value("${spring.application.name}")
    private String serviceName;

    public void send(String level, String message) {
        kafkaTemplate.send("logs", LogMessage.builder()
                .serviceName(serviceName)
                .logLevel(level)
                .message(message)
                .timestamp(Instant.now())
                .build());
    }
}