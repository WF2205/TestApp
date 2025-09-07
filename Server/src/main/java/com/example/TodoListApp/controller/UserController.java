package com.example.TodoListApp.controller;

import com.example.TodoListApp.config.CustomOAuth2User;
import com.example.TodoListApp.config.CustomUserDetailsService;
import com.example.TodoListApp.entity.User;
import com.example.TodoListApp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;
    
    private String getUserId(Object principal) {
        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getId();
        } else if (principal instanceof CustomUserDetailsService.CustomUserPrincipal) {
            return ((CustomUserDetailsService.CustomUserPrincipal) principal).getUser().getId();
        }
        throw new IllegalArgumentException("Unsupported authentication principal type: " +
            (principal != null ? principal.getClass().getName() : "null"));
    }
    
    private User getUser(Object principal) {
        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getUser();
        } else if (principal instanceof CustomUserDetailsService.CustomUserPrincipal) {
            return ((CustomUserDetailsService.CustomUserPrincipal) principal).getUser();
        }
        throw new IllegalArgumentException("Unsupported authentication principal type: " +
            (principal != null ? principal.getClass().getName() : "null"));
    }

    @GetMapping("/me")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        // Always fetch the latest user data from database to ensure we have the most up-to-date information
        Optional<User> userOpt = userService.findById(userId);
        if (userOpt.isPresent()) {
            return ResponseEntity.ok(userOpt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/me")
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal Object principal,
                                            @RequestBody Map<String, String> profileData) {
        String userId = getUserId(principal);
        String firstName = profileData.get("firstName");
        String lastName = profileData.get("lastName");
        String email = profileData.get("email");

        User updatedUser = userService.updateProfile(userId, firstName, lastName, email);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(@AuthenticationPrincipal Object principal,
                                                          @RequestParam("file") MultipartFile file) {
        try {
            String userId = getUserId(principal);
            String filename = userService.uploadAvatar(userId, file);
            
            Map<String, String> response = Map.of(
                "message", "Avatar uploaded successfully",
                "filename", filename
            );
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            Map<String, String> response = Map.of(
                "error", "Failed to upload avatar: " + e.getMessage()
            );
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me/avatar")
    public ResponseEntity<byte[]> getAvatar(@AuthenticationPrincipal Object principal) {
        try {
            String userId = getUserId(principal);
            byte[] avatarData = userService.getAvatar(userId);
            String contentType = userService.getAvatarContentType(userId);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(avatarData.length);
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(avatarData);
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> deleteAvatar(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        userService.deleteAvatar(userId);
        
        Map<String, String> response = Map.of(
            "message", "Avatar deleted successfully"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable String userId) {
        Optional<User> user = userService.findById(userId);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        Optional<User> user = userService.findByUsername(username);
        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(@RequestParam String query) {
        // For now, return empty list - can be implemented later with proper search
        List<User> users = List.of();
        return ResponseEntity.ok(users);
    }

    // Admin endpoints
    @GetMapping("/admin/all")
    public ResponseEntity<List<User>> getAllUsers(@AuthenticationPrincipal Object principal) {
        // Check if user is admin
        User user = getUser(principal);
        if (!user.getRoles().contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<User> users = userService.findAllActiveUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/admin/{userId}/deactivate")
    public ResponseEntity<Map<String, String>> deactivateUser(@AuthenticationPrincipal Object principal,
                                                            @PathVariable String userId) {
        // Check if user is admin
        User user = getUser(principal);
        if (!user.getRoles().contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        userService.deactivateUser(userId);
        Map<String, String> response = Map.of(
            "message", "User deactivated successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/admin/{userId}/activate")
    public ResponseEntity<Map<String, String>> activateUser(@AuthenticationPrincipal Object principal,
                                                         @PathVariable String userId) {
        // Check if user is admin
        User user = getUser(principal);
        if (!user.getRoles().contains("ADMIN")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        userService.activateUser(userId);
        Map<String, String> response = Map.of(
            "message", "User activated successfully"
        );
        return ResponseEntity.ok(response);
    }
}