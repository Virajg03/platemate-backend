package com.platemate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.platemate.model.User;
import com.platemate.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PROVIDER')")
    public ResponseEntity<List<User>> getAllUsers(Authentication authentication) {
        System.out.println("Authenticated user: " + authentication.getName());
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')  or @userService.isOwnerOrAdmin(authentication.name, #id)")
    public ResponseEntity<User> getUserById(@PathVariable Long id, Authentication authentication) {
        System.out.println("Authenticated user: " + authentication.getName() + " requesting user: " + id);
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<User> createUser(@RequestBody User user, Authentication authentication) {
        System.out.println("Authenticated user: " + authentication.getName());
        User createdUser = userService.createUser(user);
        return ResponseEntity.ok(createdUser);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN') or @userService.isOwnerOrAdmin(authentication.name, #id)")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user, Authentication authentication) {
        try {
            System.out.println("Authenticated user: " + authentication.getName() + " updating user: " + id);
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        System.out.println("Authenticated user: " + authentication.getName() + " deleting user: " + id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
