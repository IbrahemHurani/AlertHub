package com.alerthub.sms_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage {
    @NotBlank(message = "recipient (to) is required")
    private String to;        // phone number in E.164, e.g. +972501234567
    @NotBlank(message = "message is required")
    private String message;   // text content from the action
    private Long actionId;     // optional, for traceability
}