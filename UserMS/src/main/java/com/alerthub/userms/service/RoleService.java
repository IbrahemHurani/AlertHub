package com.alerthub.userms.service;

import com.alerthub.userms.entity.Role;

import java.util.List;

public interface RoleService {
    List<Role> getAll();

    Role getById(Long id);

    Role create(Role role);

    Role update(Long id, Role role);

    void delete(Long id);
}
