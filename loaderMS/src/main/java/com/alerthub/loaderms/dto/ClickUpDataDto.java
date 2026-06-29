package com.alerthub.loaderms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClickUpDataDto {

    private String owner_id;
    private String project;
    private String tag;
    private String label;
    private String worker_id;
    private Integer task;
    private String pr_env;
    private String user_story;
    private Integer day;

    // @JsonProperty preserves the upstream typo as the JSON key while keeping the Java field name correct
    @JsonProperty("currant_sprint")
    private String currentSprint;
}
