package com.alerthub.loaderms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubDataDto {

    @JsonProperty("manager_id")
    private String managerId;
    private String projects;
    private String assignee;
    private String label;

    @JsonProperty("devloper_id")
    private String developerId;

    private Integer issue;
    private String environment;

    @JsonProperty("user_story")
    private String userStory;

    private Integer point;
    private String sprint;
}
