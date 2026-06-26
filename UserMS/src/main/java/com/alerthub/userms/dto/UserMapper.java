package com.alerthub.userms.dto;

import com.alerthub.userms.entity.Role;
import com.alerthub.userms.entity.User;

import java.util.List;

public class UserMapper {
    public static User toEntity(UserRequestDTO userRequestDTO) {
        User user = new User();
        user.setUsername(userRequestDTO.getUsername());
        user.setPassword(userRequestDTO.getPassword());
        user.setEmail(userRequestDTO.getEmail());
        user.setPhone(userRequestDTO.getPhone());
        return user;
    }
    public static UserResponseDTO toDTO(User user) {
        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setUserId(user.getId());
        userResponseDTO.setUsername(user.getUsername());
        userResponseDTO.setEmail(user.getEmail());
        userResponseDTO.setPhone(user.getPhone());
        List<Role> roles = user.getRoles();
        if (roles != null) {
            userResponseDTO.setRoles(roles.stream().map(UserMapper::toRoleDTO).toList());
        }
        return userResponseDTO;
    }

    public static RoleResponseDTO toRoleDTO(Role role) {
        RoleResponseDTO roleResponseDTO = new RoleResponseDTO();
        roleResponseDTO.setId(role.getId());
        roleResponseDTO.setRole(role.getRole());
        return roleResponseDTO;
    }
}
