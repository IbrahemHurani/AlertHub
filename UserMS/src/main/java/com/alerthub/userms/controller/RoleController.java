package com.alerthub.userms.controller;

import com.alerthub.userms.entity.Role;
import com.alerthub.userms.service.RoleService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/role")
public class RoleController {
    @Autowired
    private RoleService roleService;

    @PostMapping("/create")
    ResponseEntity<Role> createRole(@Valid @RequestBody Role role) {
        return new ResponseEntity<>(roleService.create(role),  HttpStatus.CREATED);
    }
    @GetMapping("/getroles")
    ResponseEntity<List<Role>> getAllRoles() {
        return new ResponseEntity<>(roleService.getAll(), HttpStatus.OK);
    }
    @GetMapping("/getrole/{id}")
    ResponseEntity<Role> getRole(@PathVariable Long id) {
        return new ResponseEntity<>(roleService.getById(id), HttpStatus.OK);
    }
    @PutMapping("/update/{id}")
    ResponseEntity<Role> updateRole(@PathVariable Long id,@Valid @RequestBody Role role) {
        return new ResponseEntity<>(roleService.update(id,role), HttpStatus.OK);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Role> deleteRole(@PathVariable Long id)
    {
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }

}
