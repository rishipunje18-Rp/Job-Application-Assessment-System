package com.jobportal.service;

import com.jobportal.model.User;
import com.jobportal.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User register(User user) {
        // Validate
        if (user.getName() == null || user.getName().trim().isEmpty()) {
            throw new RuntimeException("Name is required");
        }
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }

        // Check duplicate email
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        // Default role
        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            user.setRole("STUDENT");
        }

        return userRepository.save(user);
    }

    public User login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found with this email");
        }

        User user = userOpt.get();
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        return user;
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }
}
