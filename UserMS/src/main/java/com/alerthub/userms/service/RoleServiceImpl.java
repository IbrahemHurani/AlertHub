package com.alerthub.userms.service;

import com.alerthub.userms.dao.RoleRepository;
import com.alerthub.userms.entity.Role;
import com.alerthub.userms.entity.User;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl implements RoleService{

    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initInitialRoles() {
        if (roleRepository.count() == 0) {
            List<Role> initialRoles = List.of(
                new Role("createAction"),
                new Role("updateAction"),
                new Role("deleteAction"),
                new Role("createMetric"),
                new Role("updateMetric"),
                new Role("deleteMetric"),
                new Role("triggerScan"),
                new Role("triggerProcess"),
                new Role("triggerEvaluation"),
                new Role("read")
            );
            roleRepository.saveAll(initialRoles);
            System.out.println(">> The table was successfully saved to the database when the project was run!");
        }
    }

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
    @Transactional
    public void delete(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        if (role.getUsers() != null) {
            for (User user : role.getUsers()) {
                user.getRoles().remove(role);
            }
        }

        roleRepository.delete(role);
    }
}
