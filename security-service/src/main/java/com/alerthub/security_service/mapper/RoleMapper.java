package com.alerthub.security_service.mapper;

import com.alerthub.security_service.model.Role;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RoleMapper {
    
    public String toDto(Role role) {
        return role != null ? role.getName() : null;
    }
    
    public List<String> toDtoList(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toList());
    }
    
    public Set<String> toDtoSet(Set<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return Set.of();
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}
