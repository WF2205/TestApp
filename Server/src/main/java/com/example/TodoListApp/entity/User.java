package com.example.TodoListApp.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "users")
public class User {
    
    @Id
    private String id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Indexed(unique = true)
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Indexed(unique = true)
    private String email;
    
    private String firstName;
    private String lastName;
    
    @Indexed(unique = true)
    private String githubId;
    
    // Password fields for username/password authentication
    private String password; // Encrypted password
    private String passwordSalt; // Salt for password hashing
    private boolean passwordEnabled; // Whether password auth is enabled for this user
    
    private String avatarUrl;
    private String avatarFileName;
    private String avatarContentType;
    private Long avatarSize;
    
    // Binary avatar data stored in database
    @JsonIgnore
    private byte[] avatarData;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;
    
    private List<String> roles;
    private boolean active;
    
    // Constructors
    public User() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.active = true;
        this.roles = List.of("USER");
        this.passwordEnabled = false; // Default to OAuth only
    }
    
    public User(String username, String email, String githubId) {
        this();
        this.username = username;
        this.email = email;
        this.githubId = githubId;
    }
    
    // Getters and Setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getGithubId() {
        return githubId;
    }
    
    public void setGithubId(String githubId) {
        this.githubId = githubId;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getPasswordSalt() {
        return passwordSalt;
    }
    
    public void setPasswordSalt(String passwordSalt) {
        this.passwordSalt = passwordSalt;
    }
    
    public boolean isPasswordEnabled() {
        return passwordEnabled;
    }
    
    public void setPasswordEnabled(boolean passwordEnabled) {
        this.passwordEnabled = passwordEnabled;
    }
    
    public String getAvatarUrl() {
        return avatarUrl;
    }
    
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
    
    public String getAvatarFileName() {
        return avatarFileName;
    }
    
    public void setAvatarFileName(String avatarFileName) {
        this.avatarFileName = avatarFileName;
    }
    
    public String getAvatarContentType() {
        return avatarContentType;
    }
    
    public void setAvatarContentType(String avatarContentType) {
        this.avatarContentType = avatarContentType;
    }
    
    public Long getAvatarSize() {
        return avatarSize;
    }
    
    public void setAvatarSize(Long avatarSize) {
        this.avatarSize = avatarSize;
    }
    
    public byte[] getAvatarData() {
        return avatarData;
    }
    
    public void setAvatarData(byte[] avatarData) {
        this.avatarData = avatarData;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }
    
    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
    
    public List<String> getRoles() {
        return roles;
    }
    
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    // Helper methods
    public String getFullName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return username;
    }
    
    public boolean hasAvatar() {
        return avatarData != null && avatarData.length > 0;
    }
    
    /**
     * Get avatar data as Base64 encoded string for JSON serialization
     */
    public String getAvatarDataBase64() {
        if (avatarData != null && avatarData.length > 0) {
            return java.util.Base64.getEncoder().encodeToString(avatarData);
        }
        return null;
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", githubId='" + githubId + '\'' +
                ", active=" + active +
                '}';
    }
}