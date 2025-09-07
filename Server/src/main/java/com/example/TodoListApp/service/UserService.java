package com.example.TodoListApp.service;

import com.example.TodoListApp.entity.User;
import com.example.TodoListApp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User findOrCreateUserFromOAuth(String githubId, String username, String email, 
                                        String firstName, String lastName, String avatarUrl) {
        Optional<User> existingUser = userRepository.findByGithubId(githubId);
        
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            user.setLastLoginAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            
            // Update avatar URL if it has changed
            if (avatarUrl != null && !avatarUrl.equals(user.getAvatarUrl())) {
                user.setAvatarUrl(avatarUrl);
            }
            
            return userRepository.save(user);
        }
        
        // Check if user exists with same email
        Optional<User> userByEmail = userRepository.findByEmail(email);
        if (userByEmail.isPresent()) {
            User user = userByEmail.get();
            user.setGithubId(githubId);
            user.setAvatarUrl(avatarUrl);
            user.setLastLoginAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());
            return userRepository.save(user);
        }
        
        // Create new user
        User newUser = new User(username, email, githubId);
        newUser.setFirstName(firstName);
        newUser.setLastName(lastName);
        newUser.setAvatarUrl(avatarUrl);
        newUser.setLastLoginAt(LocalDateTime.now());
        
        return userRepository.save(newUser);
    }

    public Optional<User> findById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByGithubId(String githubId) {
        return userRepository.findByGithubId(githubId);
    }

    public List<User> findAllActiveUsers() {
        return userRepository.findByActiveTrue();
    }

    public List<User> findByRole(String role) {
        return userRepository.findByRolesContaining(role);
    }

    public User save(User user) {
        user.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateProfile(String userId, String firstName, String lastName, String email) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setUpdatedAt(LocalDateTime.now());
        
        return userRepository.save(user);
    }

    public String uploadAvatar(String userId, MultipartFile file) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("File must be an image");
        }
        
        // Generate unique filename for reference
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = userId + "_" + UUID.randomUUID().toString() + extension;
        
        // Compress and store binary data in database
        byte[] avatarData = compressImage(file.getBytes(), contentType);
        
        // Update user with avatar data
        user.setAvatarData(avatarData);
        user.setAvatarFileName(filename);
        user.setAvatarContentType("image/jpeg"); // Always JPEG after compression
        user.setAvatarSize((long) avatarData.length);
        user.setAvatarUrl("/api/users/me/avatar");
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
        
        return filename;
    }

    public byte[] getAvatar(String userId) throws IOException {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        if (user.getAvatarData() == null || user.getAvatarData().length == 0) {
            throw new RuntimeException("Avatar not found");
        }
        
        return user.getAvatarData();
    }

    public String getAvatarContentType(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        return user.getAvatarContentType();
    }

    public void deleteAvatar(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        
        // Clear avatar data from database
        user.setAvatarData(null);
        user.setAvatarFileName(null);
        user.setAvatarContentType(null);
        user.setAvatarSize(null);
        user.setAvatarUrl(null);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }

    public void deactivateUser(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }

    public void activateUser(String userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        
        userRepository.save(user);
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean existsByGithubId(String githubId) {
        return userRepository.existsByGithubId(githubId);
    }
    
    /**
     * Compress image to reduce storage size
     * Resizes to max 200x200 pixels and compresses JPEG quality to 0.8
     */
    private byte[] compressImage(byte[] originalData, String contentType) throws IOException {
        try {
            BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalData));
            if (originalImage == null) {
                return originalData; // Return original if can't process
            }
            
            // Calculate new dimensions (max 200x200, maintain aspect ratio)
            int maxSize = 200;
            int originalWidth = originalImage.getWidth();
            int originalHeight = originalImage.getHeight();
            
            int newWidth = originalWidth;
            int newHeight = originalHeight;
            
            if (originalWidth > maxSize || originalHeight > maxSize) {
                if (originalWidth > originalHeight) {
                    newWidth = maxSize;
                    newHeight = (originalHeight * maxSize) / originalWidth;
                } else {
                    newHeight = maxSize;
                    newWidth = (originalWidth * maxSize) / originalHeight;
                }
            }
            
            // Create compressed image
            BufferedImage compressedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = compressedImage.createGraphics();
            g2d.drawImage(originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH), 0, 0, null);
            g2d.dispose();
            
            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(compressedImage, "jpg", baos);
            
            return baos.toByteArray();
        } catch (Exception e) {
            // If compression fails, return original data
            System.err.println("Image compression failed: " + e.getMessage());
            return originalData;
        }
    }
}