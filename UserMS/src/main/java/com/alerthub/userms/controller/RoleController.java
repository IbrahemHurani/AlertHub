package com.alerthub.userms.controller;

import com.alerthub.userms.client.SecurityClient;
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

    @Autowired
    private SecurityClient securityClient; // حقن الـ SecurityClient للفحص

    // ميثود مساعدة لفحص الصلاحيات من الـ Security Service
    private boolean isNotAuthorized(Long userId, String permission) {
        if (userId == null) return true;
        SecurityClient.AuthorizeRequest request = new SecurityClient.AuthorizeRequest();
        request.setUserId(userId);
        request.setPermission(permission);
        try {
            ResponseEntity<SecurityClient.AuthorizeResponse> response = securityClient.authorizeUserPermission(request);
            return response.getBody() == null || !response.getBody().isAuthorized();
        } catch (Exception e) {
            return true;
        }
    }

    @PostMapping("/create")
    ResponseEntity<?> createRole(
            @RequestAttribute("currentUserId") Long currentUserId,
            @Valid @RequestBody Role role) {

        // تعديل أو إنشاء الأدوار ميزة حصرية للـ Admin (نستعمل صلاحية القراءة العامة كمثال أو صلاحية مخصصة للأدمن)
        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return new ResponseEntity<>(roleService.create(role), HttpStatus.CREATED);
    }

    @GetMapping("/getroles")
    ResponseEntity<?> getAllRoles(@RequestAttribute("currentUserId") Long currentUserId) {
        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return new ResponseEntity<>(roleService.getAll(), HttpStatus.OK);
    }

    @GetMapping("/getrole/{id}")
    ResponseEntity<?> getRole(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long id) {

        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return new ResponseEntity<>(roleService.getById(id), HttpStatus.OK);
    }

    @PutMapping("/update/{id}")
    ResponseEntity<?> updateRole(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long id,
            @Valid @RequestBody Role role) {

        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        return new ResponseEntity<>(roleService.update(id, role), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRole(
            @RequestAttribute("currentUserId") Long currentUserId,
            @PathVariable Long id) {

        if (isNotAuthorized(currentUserId, "read")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("FORBIDDEN");
        }
        roleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}