package com.alerthub.logger_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.alerthub.logger_service.dto.LogMessage;
import com.alerthub.logger_service.service.LogService;
import lombok.RequiredArgsConstructor;
@Component
@RequiredArgsConstructor
public class LogConsumer {
    private final LogService logService;

    @KafkaListener(topics="logs",groupId="logger-service")
    public void consume(LogMessage message){
        logService.save(message);
    }
}
