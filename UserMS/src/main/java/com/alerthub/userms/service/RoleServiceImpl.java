package com.alerthub.userms.service;

import com.alerthub.userms.dao.RoleRepository;
import com.alerthub.userms.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService{

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    @Override
    public Role getById(Long id) {
        return roleRepository.findById(id).orElseThrow(()->new RuntimeException("Role doesnt exist in the System"));
    }

    @Override
    public Role create(Role role) {
        return roleRepository.save(role);
    }

    @Override
    public Role update(Long id, Role role) {
        if(!roleRepository.existsById(id)){
            throw new RuntimeException("Role doesnt exist in the System");
        }
        return roleRepository.save(role);
    }

    @Override
    public void delete(Long id) {
        if(!roleRepository.existsById(id)){
            throw new RuntimeException("Role doesnt exist in the System");
        }
        roleRepository.deleteById(id);
    }
}
