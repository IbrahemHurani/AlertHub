package com.alerthub.sms_service.service;

import com.alerthub.sms_service.dto.NotificationMessage;

public interface SmsService {
    void process(NotificationMessage message);
}