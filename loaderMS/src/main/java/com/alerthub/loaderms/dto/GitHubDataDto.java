package com.alerthub.loaderms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitHubDataDto {

    private String manager_id;
    private String projects;
    private String assignee;
    private String label;

    // @JsonProperty preserves the upstream typo as the JSON key while keeping the Java field name correct
    @JsonProperty("devloper_id")
    private String developerId;

    private Integer issue;
    private String environment;
    private String user_story;
    private Integer point;
    private String sprint;
}
