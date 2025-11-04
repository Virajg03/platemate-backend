package com.platemate.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platemate.exception.ResourceNotFoundException;
import com.platemate.model.Customer;
import com.platemate.model.User;
import com.platemate.repository.CustomerRepository;
import com.platemate.repository.UserRepository;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;

    public Customer create(Customer customer) {
        return customerRepository.save(customer);
    }

    public List<Customer> listActive() {
        return customerRepository.findAllByIsDeletedFalse();
    }

    public Optional<Customer> getById(Long id) {
        return customerRepository.findById(id).filter(c -> !Boolean.TRUE.equals(c.getIsDeleted()));
    }

    public Customer update(Long id, Customer update) {
        return customerRepository.findById(id).map(c -> {
            if (update.getFullName() != null) c.setFullName(update.getFullName());
            if (update.getDateOfBirth() != null) c.setDateOfBirth(update.getDateOfBirth());
            return customerRepository.save(c);
        }).orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
    }

    public void softDelete(Long id) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id " + id));
        c.setIsDeleted(true);
        customerRepository.save(c);
    }

    public User requireUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
    }
}


