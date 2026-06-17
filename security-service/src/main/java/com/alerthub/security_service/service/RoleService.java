package com.alerthub.security_service.service;

import com.alerthub.security_service.dto.request.AssignRoleRequest;
import com.alerthub.security_service.dto.request.RevokeRoleRequest;
import com.alerthub.security_service.dto.response.UserRoleResponse;
import com.alerthub.security_service.mapper.RoleMapper;
import com.alerthub.security_service.model.Role;
import com.alerthub.security_service.model.User;
import com.alerthub.security_service.repository.RoleRepository;
import com.alerthub.security_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.HashSet;

@Service
@RequiredArgsConstructor
@Transactional
public class RoleService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper; 
    
    public UserRoleResponse assignRoleToUser(AssignRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseGet(() -> userRepository.save(User.builder()
                        .id(request.getUserId())
                        .roles(new HashSet<>())
                        .build()));
        
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
        
        user.getRoles().add(role);
        userRepository.save(user);
        
        return buildUserRoleResponse(user);
    }
    
    public UserRoleResponse revokeRoleFromUser(RevokeRoleRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with id: " + request.getUserId()));
        
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + request.getRoleId()));
        
        user.getRoles().remove(role);
        userRepository.save(user);
        
        return buildUserRoleResponse(user);
    }
    
    @Transactional(readOnly = true)
    public UserRoleResponse getUserRoles(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return buildUserRoleResponse(user);
    }
    
    @Transactional(readOnly = true)
    public boolean userHasRole(Long userId, String permissionName) {
        if ("READ_ALL".equalsIgnoreCase(permissionName)) {
            return true;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }
        
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase("admin"));
        if (isAdmin) {
            return true;
        }
        
        return user.getRoles().stream()
                .anyMatch(role -> role.getName().equalsIgnoreCase(permissionName));
    }
    
    @Transactional(readOnly = true)
    public List<String> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(Role::getName)
                .toList();
    }
    
   private UserRoleResponse buildUserRoleResponse(User user) {
        List<String> assignedRoles = roleMapper.toDtoList(user.getRoles());
        
        boolean isAdmin = assignedRoles.stream()
                .anyMatch(role -> role.equalsIgnoreCase("admin"));

        List<String> finalRolesList;

        if (isAdmin) {
            finalRolesList = getAllRoles();
        } else {
            finalRolesList = assignedRoles;
        }

        return UserRoleResponse.builder()
                .userId(user.getId())
                .roles(finalRolesList)
                .build();
    }
}