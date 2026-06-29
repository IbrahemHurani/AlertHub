package com.alerthub.loaderms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickUpDataDto {

    @JsonProperty("owner_id")
    private String ownerId;
    private String project;
    private String tag;
    private String label;

    @JsonProperty("worker_id")
    private String workerId;

    private Integer task;

    @JsonProperty("pr_env")
    private String prEnv;

    @JsonProperty("user_story")
    private String userStory;

    private Integer day;

    @JsonProperty("currant_sprint")
    private String currentSprint;
}
