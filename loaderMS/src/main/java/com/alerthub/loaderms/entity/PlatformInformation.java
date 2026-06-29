package com.alerthub.loaderms.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "platform_information")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformInformation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "owner_id")
    private String ownerId;

    private String project;

    private String tag;

    private String label;

    @Column(name = "developer_id")
    private String developerId;

    @Column(name = "task_number")
    private Integer taskNumber;

    private String environment;

    @Column(name = "user_story")
    private String userStory;

    @Column(name = "task_point")
    private Integer taskPoint;

    private String sprint;

    private String provider;
}
