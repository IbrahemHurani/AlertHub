package com.alerthub.userms.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString

public class UserRequestDTO {
    @NotEmpty(message = "Name is required")
    private String username;
    private String password;
    private String email;
    private Long phone;

}
