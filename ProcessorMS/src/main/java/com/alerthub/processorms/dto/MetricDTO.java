package com.alerthub.processorms.dto;

import lombok.Data;

@Data
public class MetricDTO {
    private Integer id;
    private Integer userId;
    private String name;
    private String label; // e.g., "bug", "help_wanted"
    private Integer threshold;
    private Integer timeFrameHours;
}
