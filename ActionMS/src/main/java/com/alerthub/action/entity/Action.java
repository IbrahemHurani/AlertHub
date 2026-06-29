package com.alerthub.action.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import jakarta.persistence.Convert;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "action")
public class Action {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    private String name;

    @Column(name = "create_date")
    private LocalDate createDate;

    @Column(name = "recipient")
    private String to;

    private String message;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "last_run")
    private LocalDateTime lastRun;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type")
    private ActionType actionType;

    @Column(name = "run_on_time")
    private LocalTime runOnTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_on_day")
    private RunOnDay runOnDay;

    @Column(name = "is_enabled")
    private Boolean isEnabled;

    @Column(name = "is_deleted")
    private Boolean isDeleted;

    @Convert(converter = ConditionConverter.class)
    @Column(name = "action_condition", columnDefinition = "TEXT")
    private List<List<Integer>> condition;
}