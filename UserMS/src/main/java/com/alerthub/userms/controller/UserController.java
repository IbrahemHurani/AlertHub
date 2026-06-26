package com.alerthub.userms.controller;

import com.alerthub.userms.dto.UserRequestDTO;
import com.alerthub.userms.dto.UserResponseDTO;
import com.alerthub.userms.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/create")
    ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO dto) {
        return new ResponseEntity<>(userService.create(dto),  HttpStatus.CREATED);
    }
    @GetMapping("/getusers")
    ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
    }
    @GetMapping("/getuser/{id}")
    ResponseEntity<UserResponseDTO> getUser(@PathVariable Long id) {
        return new ResponseEntity<>(userService.getById(id), HttpStatus.OK);
    }
    @PutMapping("/update/{id}")
    ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,@Valid @RequestBody UserRequestDTO dto) {
        return new ResponseEntity<>(userService.update(id,dto), HttpStatus.OK);
    }
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<UserResponseDTO> deleteUser(@PathVariable Long id)
    {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/grantPermissions/{userId}")
    ResponseEntity<UserResponseDTO> grantPermissions(@PathVariable Long userId,
                                                     @Valid @RequestBody List<Long> permissionsIds) {
        return new ResponseEntity<>(userService.grantPermissions(userId,permissionsIds), HttpStatus.OK);
    }
}
