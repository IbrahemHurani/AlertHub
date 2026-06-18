package com.alerthub.security_service.controller;

import com.alerthub.security_service.config.JwtUtils;
import com.alerthub.security_service.dto.request.AssignRoleRequest;
import com.alerthub.security_service.dto.request.AuthorizeRequest;
import com.alerthub.security_service.dto.request.RevokeRoleRequest;
import com.alerthub.security_service.dto.response.AuthorizeResponse;
import com.alerthub.security_service.dto.response.UserRoleResponse;
import com.alerthub.security_service.service.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {
    
    private final RoleService roleService;
    private final JwtUtils jwtUtils;
    
    @PostMapping("/token")
    public ResponseEntity<?> generateToken(@RequestParam Long userId) {
        UserRoleResponse userRoles = roleService.getUserRoles(userId);
        String token = jwtUtils.generateJwtToken(userId, userRoles.getRoles());
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/assign")
    public ResponseEntity<UserRoleResponse> assignRoleToUser(@RequestBody AssignRoleRequest request) {
        UserRoleResponse response = roleService.assignRoleToUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping("/revoke")
    public ResponseEntity<UserRoleResponse> revokeRoleFromUser(@RequestBody RevokeRoleRequest request) {
        UserRoleResponse response = roleService.revokeRoleFromUser(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<UserRoleResponse> getUserRoles(@PathVariable Long userId) {
        UserRoleResponse response = roleService.getUserRoles(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @PostMapping("/authorize")
    public ResponseEntity<AuthorizeResponse> authorizeUserPermission(@RequestBody AuthorizeRequest request) {
        boolean hasPermission = roleService.userHasRole(request.getUserId(), request.getPermission());
        
        AuthorizeResponse response = AuthorizeResponse.builder()
                .userId(request.getUserId())
                .permission(request.getPermission())
                .authorized(hasPermission)
                .build();
        
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<String>> getAllRoles() {
        List<String> roles = roleService.getAllRoles();
        return ResponseEntity.status(HttpStatus.OK).body(roles);
    }
}