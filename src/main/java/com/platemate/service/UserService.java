package com.platemate.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.platemate.enums.ImageType;
import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Customer;
import com.platemate.model.TiffinProvider;
import com.platemate.model.User;
import com.platemate.repository.CustomerRepository;
import com.platemate.repository.ImageRepository;
import com.platemate.repository.TiffinProviderRepository;
import com.platemate.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressService addressService;

    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private TiffinProviderRepository tiffinProviderRepository;
    
    @Autowired
    private ImageRepository imageRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Load customer data if user is a customer
            if (user.getRole() != null && user.getRole().name().equals("ROLE_CUSTOMER")) {
                Optional<Customer> customerOpt = customerRepository.findByUser_IdAndIsDeletedFalse(id);
                if (customerOpt.isPresent()) {
                    Customer customer = customerOpt.get();
                    // Set customer fullName as transient field on User
                    user.setFullName(customer.getFullName());
                    
                    // Load profile image ID for customer
                    // Use customerId as ownerId (same pattern as provider)
                    Long profileImageId = imageRepository.findIdByImageTypeAndOwnerId(ImageType.CUSTOMER_PROFILE, customer.getId());
                    if (profileImageId != null) {
                        user.setProfileImageId(profileImageId);
                    }
                }
            }
            // Load provider data if user is a provider
            else if (user.getRole() != null && user.getRole().name().equals("ROLE_PROVIDER")) {
                TiffinProvider provider = tiffinProviderRepository.findByUser_Id(id);
                if (provider != null) {
                    // Load profile image ID for provider
                    // Use providerId as ownerId (matches TiffinProviderService.loadExtras pattern)
                    Long profileImageId = imageRepository.findIdByImageTypeAndOwnerId(ImageType.PROVIDER_PROFILE, provider.getId());
                    if (profileImageId != null) {
                        user.setProfileImageId(profileImageId);
                    }
                }
            }
        }
        return userOpt;
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(Long id, User updatedUser) {
        return userRepository.findById(id).map(user -> {
            user.setUsername(updatedUser.getUsername());
            user.setPassword(updatedUser.getPassword());
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            return userRepository.save(user);
        }).orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
    
    public User getUserDetailsByUsername(String username) {
        return userRepository.getUserDetailsByUsername(username);
    }

    // Helper method for authorization
    public boolean isOwnerOrAdmin(String username, Long userId) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = getUserDetailsByUsername(username);
        
        // Check if user is admin
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        // Check if user is trying to access their own data
        boolean isOwner = user != null && user.getId().equals(userId);
        
        return isAdmin || isOwner;
    }
}
