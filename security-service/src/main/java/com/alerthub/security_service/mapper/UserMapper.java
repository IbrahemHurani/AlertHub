package com.alerthub.security_service.mapper;

import com.alerthub.security_service.dto.response.UserRoleResponse;
import com.alerthub.security_service.model.Role;    
import com.alerthub.security_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring") 
public interface UserMapper {

    
    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToNames")
    UserRoleResponse toResponse(User user);

    
    List<UserRoleResponse> toResponseList(List<User> users);

    @Named("mapRolesToNames")
    default Set<String> mapRolesToNames(Set<Role> roles) {
        if (roles == null) {
            return null;
        }
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }
}