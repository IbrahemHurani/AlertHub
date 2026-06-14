package com.alerthub.userms.service;

import com.alerthub.userms.dto.UserRequestDTO;
import com.alerthub.userms.dto.UserResponseDTO;

import java.util.List;

public interface UserService {
    List<UserResponseDTO> getAll();

    UserResponseDTO getById(Long id);

    UserResponseDTO create(UserRequestDTO dto);

    UserResponseDTO update(Long id, UserRequestDTO dto);

    void delete(Long id);
}
