package com.alerthub.processorms.dto;

import lombok.Data;
import java.util.List;

@Data
public class ActionDTO {
    private String id;
    private String ownerId; // project manager
    private String name;
    private String actionType; // "sms" or "email"
    private String message;
    private String to;
    private List<List<Integer>> condition; // Matrix format: [[1, 2], [3]]
}