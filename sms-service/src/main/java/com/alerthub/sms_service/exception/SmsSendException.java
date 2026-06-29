package com.alerthub.sms_service.exception;

public class SmsSendException extends RuntimeException {
    public SmsSendException(String message, Throwable cause) {
        super(message, cause);
    }
}