package com.platemate.controller;

import com.platemate.config.security.JwtUtil;
import com.platemate.enums.Role;
import com.platemate.model.User;
import com.platemate.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        try {
            String username = loginRequest.get("username");
            String password = loginRequest.get("password");

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            String token = jwtUtil.generateToken(userDetails);
            String refreshToken = jwtUtil.generateRefreshToken(userDetails);

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("refreshToken", refreshToken);
            response.put("username", username);
            response.put("role", userService.getUserDetailsByUsername(username).getRole());

            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Invalid credentials");
            return ResponseEntity.status(401).body(response);
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> signupRequest) {
        try {
            String username = signupRequest.get("username");
            String email = signupRequest.get("email");
            String password = signupRequest.get("password");
            String roleStr = signupRequest.getOrDefault("role", "ROLE_USER");
            Role role;
            try {
                role = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                role = Role.ROLE_CUSTOMER; // fallback if invalid role passed
            }

            // Check if user already exists
            Optional<User> existingUser = userService.getUserById(Long.valueOf(1)); // This is just a check
            if (userService.getUserDetailsByUsername(username) != null) {
                Map<String, Object> response = new HashMap<>();
                response.put("error", "Username already exists");
                return ResponseEntity.status(400).body(response);
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);

            User savedUser = userService.createUser(user);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "User created successfully");
            response.put("userId", savedUser.getId());
            response.put("username", savedUser.getUsername());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to create user: " + e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
}