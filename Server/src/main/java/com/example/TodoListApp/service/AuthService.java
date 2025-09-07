package com.example.TodoListApp.service;

import com.example.TodoListApp.dto.LoginRequest;
import com.example.TodoListApp.dto.RegisterRequest;
import com.example.TodoListApp.entity.User;
import com.example.TodoListApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordService passwordService;
    
    /**
     * Register a new user with username/password authentication
     * @param request Registration request
     * @return Created user
     * @throws IllegalArgumentException if validation fails
     */
    public User register(RegisterRequest request) {
        // Validate request
        validateRegistrationRequest(request);
        
        // Check if username already exists
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }
        
        // Validate password strength
        if (!passwordService.isValidPassword(request.getPassword())) {
            throw new IllegalArgumentException("Password does not meet strength requirements. " +
                    "Password must be at least 8 characters long and contain uppercase, lowercase, digit, and special character.");
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        
        // Set password
        user.setPassword(passwordService.hashPassword(request.getPassword()));
        user.setPasswordEnabled(true);
        
        // Set timestamps
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Authenticate user with username/email and password
     * @param request Login request
     * @return Authenticated user
     * @throws IllegalArgumentException if authentication fails
     */
    public User authenticate(LoginRequest request) {
        // Find user by username or email
        Optional<User> userOpt = request.isEmail() 
            ? userRepository.findByEmail(request.getEmail())
            : userRepository.findByUsername(request.getUsername());
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }
        
        User user = userOpt.get();
        
        // Check if user is active
        if (!user.isActive()) {
            throw new IllegalArgumentException("Account is deactivated");
        }
        
        // Check if password authentication is enabled
        if (!user.isPasswordEnabled()) {
            throw new IllegalArgumentException("Password authentication is not enabled for this account. Please use GitHub login.");
        }
        
        // Verify password
        if (!passwordService.verifyPassword(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username/email or password");
        }
        
        // Update last login time
        user.setLastLoginAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Enable password authentication for an existing OAuth user
     * @param user The user to enable password auth for
     * @param password The password to set
     * @return Updated user
     */
    public User enablePasswordAuth(User user, String password) {
        if (!passwordService.isValidPassword(password)) {
            throw new IllegalArgumentException("Password does not meet strength requirements");
        }
        
        user.setPassword(passwordService.hashPassword(password));
        user.setPasswordEnabled(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Disable password authentication for a user
     * @param user The user to disable password auth for
     * @return Updated user
     */
    public User disablePasswordAuth(User user) {
        user.setPassword(null);
        user.setPasswordEnabled(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Change password for a user
     * @param user The user changing password
     * @param oldPassword Current password
     * @param newPassword New password
     * @return Updated user
     */
    public User changePassword(User user, String oldPassword, String newPassword) {
        if (!user.isPasswordEnabled()) {
            throw new IllegalArgumentException("Password authentication is not enabled");
        }
        
        // Verify old password
        if (!passwordService.verifyPassword(oldPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        // Validate new password
        if (!passwordService.isValidPassword(newPassword)) {
            throw new IllegalArgumentException("New password does not meet strength requirements");
        }
        
        // Set new password
        user.setPassword(passwordService.hashPassword(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }
    
    /**
     * Validate registration request
     * @param request Registration request
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRegistrationRequest(RegisterRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Registration request cannot be null");
        }
        
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required");
        }
        
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        
        if (request.getConfirmPassword() == null || request.getConfirmPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Confirm password is required");
        }
        
        if (!request.isPasswordMatch()) {
            throw new IllegalArgumentException("Passwords do not match");
        }
    }
}
