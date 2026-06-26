package com.alerthub.userms.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String email;
    private Long phone;
    private List<RoleResponseDTO> roles;
}
