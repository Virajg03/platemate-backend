package com.platemate.controller;

import com.platemate.config.security.JwtUtil;
import com.platemate.enums.Role;
import com.platemate.exception.ResourceAlreadyExistsException;
import com.platemate.model.User;
import com.platemate.service.UserService;
import com.platemate.service.TiffinProviderService;
import com.platemate.service.CustomerService;
import com.platemate.repository.TiffinProviderRepository;
import com.platemate.repository.CustomerRepository;
import com.platemate.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
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

    @Autowired
    private TiffinProviderService tiffinProviderService;

    @Autowired
    private TiffinProviderRepository tiffinProviderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String token = jwtUtil.generateToken(userDetails);
        String refreshToken = jwtUtil.generateRefreshToken(userDetails);

        // Get role and format it for client (Android expects "Provider" not "ROLE_PROVIDER")
        User user = userService.getUserDetailsByUsername(username);
        Role userRole = user.getRole();
        String roleString = formatRoleForClient(userRole);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("refreshToken", refreshToken);
        response.put("username", username);
        response.put("role", roleString);
        response.put("userId", user.getId()); // Include userId in login response

        return ResponseEntity.ok(response);
    }

    @Transactional
    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@RequestBody Map<String, String> signupRequest) {
        String username = signupRequest.get("username");
        String email = signupRequest.get("email");
        String password = signupRequest.get("password");
        String roleStr = signupRequest.getOrDefault("role", "ROLE_CUSTOMER");
        Role role;
        try {
            role = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            role = Role.ROLE_CUSTOMER; // fallback if invalid role passed
        }

        // Check if username already exists
        if (userService.getUserDetailsByUsername(username) != null) {
            throw new ResourceAlreadyExistsException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResourceAlreadyExistsException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        User savedUser = userService.createUser(user);

        // Auto-create TiffinProvider if role is PROVIDER
        if (role == Role.ROLE_PROVIDER) {
            // Check if provider already exists (shouldn't, but safety check)
            if (tiffinProviderRepository.findByUser_Id(savedUser.getId()) == null) {
                // Create provider with default values
                tiffinProviderService.createProviderWithDefaults(savedUser);
            }
        }

        // Auto-create Customer if role is CUSTOMER
        if (role == Role.ROLE_CUSTOMER) {
            // Check if customer already exists (shouldn't, but safety check)
            if (customerRepository.findByUser_IdAndIsDeletedFalse(savedUser.getId()).isEmpty()) {
                // Create customer with default values
                customerService.createCustomerWithDefaults(savedUser);
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("message", "User created successfully");
        response.put("userId", savedUser.getId());
        response.put("username", savedUser.getUsername());

        return ResponseEntity.ok(response);
    }

    /**
     * Format Role enum to client-friendly string format
     * Converts ROLE_PROVIDER → "Provider", ROLE_CUSTOMER → "Customer", etc.
     */
    private String formatRoleForClient(Role role) {
        if (role == null) {
            return "Customer";
        }
        switch (role) {
            case ROLE_PROVIDER:
                return "Provider";
            case ROLE_CUSTOMER:
                return "Customer";
            case DELIVERY_PARTNER:
                return "Delivery Partner";
            case ROLE_ADMIN:
                return "Admin";
            default:
                return "Customer";
        }
    }
}