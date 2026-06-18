package com.alerthub.security_service.service;

import com.alerthub.security_service.dto.request.AssignRoleRequest;
import com.alerthub.security_service.dto.request.RevokeRoleRequest;
import com.alerthub.security_service.dto.response.UserRoleResponse;
import com.alerthub.security_service.mapper.RoleMapper;
import com.alerthub.security_service.model.Role;
import com.alerthub.security_service.model.User;
import com.alerthub.security_service.repository.RoleRepository;
import com.alerthub.security_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoleService - service layer tests")
class RoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleService roleService;

    private Role createActionRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        createActionRole = Role.builder().id(1L).name("createAction").build();
        adminRole = Role.builder().id(99L).name("admin").build();
    }

    // ---------- assignRoleToUser ----------

    @Test
    @DisplayName("assignRoleToUser: adds the role to an existing user")
    void assignRoleToUser_existingUser_assignsRole() {
        User user = User.builder().id(1001L).roles(new HashSet<>()).build();
        AssignRoleRequest request = AssignRoleRequest.builder().userId(1001L).roleId(1L).build();

        when(userRepository.findById(1001L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(createActionRole));
        when(userRepository.save(user)).thenReturn(user);
        when(roleMapper.toDtoList(any())).thenReturn(List.of("createAction"));

        UserRoleResponse response = roleService.assignRoleToUser(request);

        assertEquals(1001L, response.getUserId());
        assertTrue(response.getRoles().contains("createAction"));
        assertTrue(user.getRoles().contains(createActionRole));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("assignRoleToUser: creates the user when it does not exist yet")
    void assignRoleToUser_userNotFound_createsUser() {
        AssignRoleRequest request = AssignRoleRequest.builder().userId(2002L).roleId(1L).build();

        when(userRepository.findById(2002L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(createActionRole));
        when(roleMapper.toDtoList(any())).thenReturn(List.of("createAction"));

        UserRoleResponse response = roleService.assignRoleToUser(request);

        assertEquals(2002L, response.getUserId());
        assertTrue(response.getRoles().contains("createAction"));
        verify(userRepository, atLeastOnce()).save(any(User.class));
    }

    @Test
    @DisplayName("assignRoleToUser: throws when the role does not exist")
    void assignRoleToUser_roleNotFound_throws() {
        User user = User.builder().id(1001L).roles(new HashSet<>()).build();
        AssignRoleRequest request = AssignRoleRequest.builder().userId(1001L).roleId(404L).build();

        when(userRepository.findById(1001L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roleService.assignRoleToUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // ---------- revokeRoleFromUser ----------

    @Test
    @DisplayName("revokeRoleFromUser: removes the role from the user")
    void revokeRoleFromUser_removesRole() {
        User user = User.builder().id(1001L).roles(new HashSet<>(Set.of(createActionRole))).build();
        RevokeRoleRequest request = RevokeRoleRequest.builder().userId(1001L).roleId(1L).build();

        when(userRepository.findById(1001L)).thenReturn(Optional.of(user));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(createActionRole));
        when(userRepository.save(user)).thenReturn(user);
        when(roleMapper.toDtoList(any())).thenReturn(List.of());

        UserRoleResponse response = roleService.revokeRoleFromUser(request);

        assertEquals(1001L, response.getUserId());
        assertFalse(user.getRoles().contains(createActionRole));
        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("revokeRoleFromUser: throws when the user does not exist")
    void revokeRoleFromUser_userNotFound_throws() {
        RevokeRoleRequest request = RevokeRoleRequest.builder().userId(999L).roleId(1L).build();

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roleService.revokeRoleFromUser(request));
        verify(userRepository, never()).save(any(User.class));
    }

    // ---------- getUserRoles ----------

    @Test
    @DisplayName("getUserRoles: returns the roles of an existing user")
    void getUserRoles_found_returnsRoles() {
        User user = User.builder().id(1001L).roles(new HashSet<>(Set.of(createActionRole))).build();

        when(userRepository.findById(1001L)).thenReturn(Optional.of(user));
        when(roleMapper.toDtoList(any())).thenReturn(List.of("createAction"));

        UserRoleResponse response = roleService.getUserRoles(1001L);

        assertEquals(1001L, response.getUserId());
        assertEquals(List.of("createAction"), response.getRoles());
    }

    @Test
    @DisplayName("getUserRoles: throws when the user does not exist")
    void getUserRoles_notFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roleService.getUserRoles(999L));
    }

    // ---------- userHasRole (authorization) ----------

    @Test
    @DisplayName("userHasRole: 'read' is always granted to every user without touching the database")
    void userHasRole_read_alwaysTrue() {
        assertTrue(roleService.userHasRole(1001L, "read"));
        assertTrue(roleService.userHasRole(1001L, "READ"));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("userHasRole: an admin user is granted every permission")
    void userHasRole_adminUser_true() {
        User admin = User.builder().id(1L).roles(new HashSet<>(Set.of(adminRole))).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));

        assertTrue(roleService.userHasRole(1L, "deleteAction"));
    }

    @Test
    @DisplayName("userHasRole: returns true when the user holds the requested permission")
    void userHasRole_hasPermission_true() {
        User user = User.builder().id(1001L).roles(new HashSet<>(Set.of(createActionRole))).build();
        when(userRepository.findById(1001L)).thenReturn(Optional.of(user));

        assertTrue(roleService.userHasRole(1001L, "createAction"));
    }

    @Test
    @DisplayName("userHasRole: returns false when the user lacks the requested permission")
    void userHasRole_missingPermission_false() {
        User user = User.builder().id(1001L).roles(new HashSet<>(Set.of(createActionRole))).build();
        when(userRepository.findById(1001L)).thenReturn(Optional.of(user));

        assertFalse(roleService.userHasRole(1001L, "deleteAction"));
    }

    @Test
    @DisplayName("userHasRole: throws when the user does not exist")
    void userHasRole_userNotFound_throws() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> roleService.userHasRole(999L, "createAction"));
    }

    // ---------- getAllRoles ----------

    @Test
    @DisplayName("getAllRoles: returns the names of every role")
    void getAllRoles_returnsRoleNames() {
        Role read = Role.builder().id(10L).name("read").build();
        when(roleRepository.findAll()).thenReturn(List.of(createActionRole, read));

        List<String> roles = roleService.getAllRoles();

        assertEquals(2, roles.size());
        assertTrue(roles.contains("createAction"));
        assertTrue(roles.contains("read"));
    }
}
