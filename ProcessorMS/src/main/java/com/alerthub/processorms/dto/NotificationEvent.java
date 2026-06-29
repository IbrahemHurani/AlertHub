package com.alerthub.processorms.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class NotificationEvent {
    private String to;
    private String message;
}
