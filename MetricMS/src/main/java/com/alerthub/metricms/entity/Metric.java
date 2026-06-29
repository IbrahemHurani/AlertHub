package com.alerthub.metricms.entity;

import com.alerthub.metricms.enums.Label;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "metric")
public class Metric {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    @NotNull(message = "userId is required")
    private Integer userId;

    @Column(nullable = false)
    @NotBlank(message = "name is required")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "label is required")
    private Label label;

    @Column(nullable = false)
    @NotNull(message = "threshold is required")
    @Min(value = 1, message = "threshold must be at least 1")
    private Integer threshold;

    @Column(name = "time_frame_hours", nullable = false)
    @NotNull(message = "timeFrameHours is required")
    @Min(value = 1, message = "timeFrameHours must be between 1 and 24")
    @Max(value = 24, message = "timeFrameHours must be between 1 and 24")
    private Integer timeFrameHours;

    public Metric() {
    }

    public Metric(Long id, Integer userId, String name, Label label, Integer threshold, Integer timeFrameHours) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.label = label;
        this.threshold = threshold;
        this.timeFrameHours = timeFrameHours;
    }

}