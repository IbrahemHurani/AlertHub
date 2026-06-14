package com.alerthub.userms.service;

import com.alerthub.userms.dao.UserRepository;
import com.alerthub.userms.dto.UserMapper;
import com.alerthub.userms.dto.UserRequestDTO;
import com.alerthub.userms.dto.UserResponseDTO;
import com.alerthub.userms.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Override
    public List<UserResponseDTO> getAll() {
        return userRepository.findAll().stream().map(UserMapper::toDTO).toList();
    }

    @Override
    public UserResponseDTO getById(Long id) {
        return UserMapper.toDTO(userRepository.findById(id).orElseThrow(()->new RuntimeException("User doesnt exist in the System")));
    }

    @Override
    public UserResponseDTO create(UserRequestDTO dto) {
        return UserMapper.toDTO(userRepository.save(UserMapper.toEntity(dto)));
    }

    @Override
    public UserResponseDTO update(Long id, UserRequestDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(()->new RuntimeException("User doesnt exist in the System"));
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public void delete(Long id) {
        if(!userRepository.existsById(id)){
            throw new RuntimeException("User doesnt exist in the System");
        }
        userRepository.deleteById(id);
    }
}
