package com.alerthub.sms_service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.alerthub.sms_service.dto.NotificationMessage;
import com.alerthub.sms_service.service.SmsService;

import lombok.RequiredArgsConstructor;



@Component
@RequiredArgsConstructor
public class SmsConsumer {
    private final SmsService smsService;

    @KafkaListener(topics = "sms", groupId = "sms-service")
    public void consume(NotificationMessage message) {
        smsService.process(message);
    }

}
