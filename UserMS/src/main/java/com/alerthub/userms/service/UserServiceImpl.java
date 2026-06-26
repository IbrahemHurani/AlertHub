package com.alerthub.userms.service;

import com.alerthub.userms.dao.RoleRepository;
import com.alerthub.userms.dao.UserRepository;
import com.alerthub.userms.dto.UserMapper;
import com.alerthub.userms.dto.UserRequestDTO;
import com.alerthub.userms.dto.UserResponseDTO;
import com.alerthub.userms.entity.Role;
import com.alerthub.userms.entity.User;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

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

    @Override
    public UserResponseDTO grantPermissions(Long userId, List<Long> permissionsIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User doesnt exist in the System"));
        List<Role> rolesToGrant = roleRepository.findAllById(permissionsIds);
        if(rolesToGrant.isEmpty() && !permissionsIds.isEmpty()){
            throw new ResourceNotFoundException("None of the provided role IDs were found");
        }
        user.setRoles(rolesToGrant);

        return UserMapper.toDTO(userRepository.save(user));
    }

    @Override
    public UserResponseDTO revokePermissions(Long userId, List<Long> permissionsIds) {
        User user = userRepository.findById(userId)
                .orElseThrow(()->new RuntimeException("User doesnt exist in the System"));
        List<Role> rolesToRevoke = roleRepository.findAllById(permissionsIds);
        if(rolesToRevoke.isEmpty() && !permissionsIds.isEmpty()){
            throw new ResourceNotFoundException("None of the provided role IDs were found");
        }
        if (user.getRoles() != null) {
            user.getRoles().removeIf(role -> rolesToRevoke.stream()
                    .anyMatch(r -> r.getId().equals(role.getId())));
        }

        return UserMapper.toDTO(userRepository.save(user));
    }
}
