package com.alerthub.loaderms.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraDataDto {

    private String manager_id;
    private String projects;
    private String assignee;
    private String label;
    private String employeeID;
    private Integer issue;
    private String env;
    private String user_story;
    private Integer point;
    private String sprint;
}
