package com.alerthub.sms_service.sender;

public interface SmsSender {
    void send(String to, String message);
}