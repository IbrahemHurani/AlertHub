package com.alerthub.userms.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private Long phone;
}
