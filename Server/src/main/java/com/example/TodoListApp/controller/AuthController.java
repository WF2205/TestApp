package com.example.TodoListApp.controller;

import com.example.TodoListApp.config.CustomOAuth2User;
import com.example.TodoListApp.config.CustomUserDetailsService;
import com.example.TodoListApp.dto.LoginRequest;
import com.example.TodoListApp.dto.RegisterRequest;
import com.example.TodoListApp.entity.User;
import com.example.TodoListApp.service.AuthService;
import com.example.TodoListApp.service.PasswordService;
import com.example.TodoListApp.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private PasswordService passwordService;
    
    @Autowired
    private UserService userService;
    
    /**
     * Register a new user
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = authService.register(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "User registered successfully");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Login with username/email and password
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            User user = authService.authenticate(request);
            
            // Create Spring Security authentication with authorities
            Collection<GrantedAuthority> authorities = user.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                    .collect(Collectors.toList());
            
            CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
                new CustomUserDetailsService.CustomUserPrincipal(user);
            
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, 
                null, // No password in token for security
                authorities
            );
            
            // Set authentication in SecurityContext
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Ensure session is created and authentication is persisted
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                SecurityContextHolder.getContext());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login successful");
            response.put("user", createUserResponse(user));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get login information (for GitHub OAuth)
     */
    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> loginInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("loginUrl", "/oauth2/authorization/github");
        response.put("message", "Please login with GitHub");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Login Success - Redirect back to iOS app
     */
    @GetMapping("/success")
    public ResponseEntity<String> loginSuccess() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Login Successful</title>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                    }
                    .container {
                        text-align: center;
                        padding: 2rem;
                        background: rgba(255, 255, 255, 0.1);
                        border-radius: 20px;
                        backdrop-filter: blur(10px);
                    }
                    .success-icon {
                        font-size: 4rem;
                        margin-bottom: 1rem;
                    }
                    .message {
                        font-size: 1.5rem;
                        margin-bottom: 1rem;
                    }
                    .redirect-message {
                        font-size: 1rem;
                        opacity: 0.8;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="success-icon">✅</div>
                    <div class="message">Login Successful!</div>
                    <div class="redirect-message">Redirecting back to TodoList app...</div>
                </div>
                <script>
                    // Redirect to iOS app after 2 seconds
                    setTimeout(function() {
                        window.location.href = 'todolistios://oauth/success';
                    }, 2000);
                </script>
            </body>
            </html>
            """;
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
    
    /**
     * Complete OAuth flow for iOS app - Create a mock OAuth user for testing
     */
    @PostMapping("/oauth/complete")
    public ResponseEntity<Map<String, Object>> completeOAuthForMobile(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // For now, create a mock OAuth user since we can't access the OAuth session from iOS
            // In a real implementation, you'd need to pass the OAuth token or user info from the iOS app
            
            User user = new User();
            user.setId("oauth_user_" + System.currentTimeMillis());
            user.setUsername("github_user");
            user.setEmail("github@example.com");
            user.setFirstName("GitHub");
            user.setLastName("User");
            user.setGithubId("12345");
            user.setPasswordEnabled(false);
            user.setActive(true);
            user.setRoles(java.util.Arrays.asList("USER"));
            
            // Save or update user
            User savedUser = userService.save(user);
            
            // Create authentication token for the user
            Collection<GrantedAuthority> authorities = savedUser.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            
            CustomUserDetailsService.CustomUserPrincipal principal =
                    new CustomUserDetailsService.CustomUserPrincipal(savedUser);
            
            UsernamePasswordAuthenticationToken userAuth =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(userAuth);
            
            // Store in session
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
            
            // Prepare response
            response.put("success", true);
            response.put("message", "OAuth login completed successfully");
            response.put("user", createUserResponse(savedUser));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to complete OAuth: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Login Failure - Redirect back to iOS app
     */
    @GetMapping("/failure")
    public ResponseEntity<String> loginFailure() {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Login Failed</title>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        height: 100vh;
                        margin: 0;
                        background: linear-gradient(135deg, #ff6b6b 0%, #ee5a24 100%);
                        color: white;
                    }
                    .container {
                        text-align: center;
                        padding: 2rem;
                        background: rgba(255, 255, 255, 0.1);
                        border-radius: 20px;
                        backdrop-filter: blur(10px);
                    }
                    .error-icon {
                        font-size: 4rem;
                        margin-bottom: 1rem;
                    }
                    .message {
                        font-size: 1.5rem;
                        margin-bottom: 1rem;
                    }
                    .redirect-message {
                        font-size: 1rem;
                        opacity: 0.8;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="error-icon">❌</div>
                    <div class="message">Login Failed!</div>
                    <div class="redirect-message">Redirecting back to TodoList app...</div>
                </div>
                <script>
                    // Redirect to iOS app after 2 seconds
                    setTimeout(function() {
                        window.location.href = 'todolistios://oauth/failure';
                    }, 2000);
                </script>
            </body>
            </html>
            """;
        
        return ResponseEntity.ok()
                .header("Content-Type", "text/html; charset=UTF-8")
                .body(html);
    }
    
    /**
     * Logout Success
     */
    @GetMapping("/logout-success")
    public ResponseEntity<Map<String, String>> logoutSuccess() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful!");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/test")
    public ResponseEntity<Map<String, String>> test() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Test endpoint working!");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout() {
        SecurityContextHolder.clearContext();
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }
    
    /**
     * Enable password authentication for OAuth users
     */
    @PostMapping("/enable-password")
    public ResponseEntity<Map<String, Object>> enablePassword(
            @RequestParam String password,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("User not authenticated");
            }
            
            User user = (User) authentication.getPrincipal();
            User updatedUser = authService.enablePasswordAuth(user, password);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password authentication enabled");
            response.put("user", createUserResponse(updatedUser));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Change password
     */
    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new IllegalArgumentException("User not authenticated");
            }
            
            User user = (User) authentication.getPrincipal();
            User updatedUser = authService.changePassword(user, oldPassword, newPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password changed successfully");
            response.put("user", createUserResponse(updatedUser));
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check password strength
     */
    @PostMapping("/check-password-strength")
    public ResponseEntity<Map<String, Object>> checkPasswordStrength(@RequestParam String password) {
        Map<String, Object> response = new HashMap<>();
        response.put("isValid", passwordService.isValidPassword(password));
        response.put("strength", passwordService.getPasswordStrength(password));
        return ResponseEntity.ok(response);
    }
    
    /**
     * Create user response without sensitive information
     */
    private Map<String, Object> createUserResponse(User user) {
        Map<String, Object> userResponse = new HashMap<>();
        userResponse.put("id", user.getId());
        userResponse.put("username", user.getUsername());
        userResponse.put("email", user.getEmail());
        userResponse.put("firstName", user.getFirstName());
        userResponse.put("lastName", user.getLastName());
        userResponse.put("fullName", user.getFullName());
        userResponse.put("avatarUrl", user.getAvatarUrl());
        userResponse.put("githubId", user.getGithubId());
        userResponse.put("passwordEnabled", user.isPasswordEnabled());
        userResponse.put("roles", user.getRoles());
        userResponse.put("active", user.isActive());
        userResponse.put("createdAt", user.getCreatedAt());
        userResponse.put("lastLoginAt", user.getLastLoginAt());
        return userResponse;
    }
    
    /**
     * OAuth token exchange endpoint for mobile apps
     */
    @PostMapping("/oauth/token")
    public ResponseEntity<Map<String, Object>> exchangeOAuthToken(
            @RequestParam String code,
            @RequestParam String grant_type,
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            HttpServletRequest request) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Validate the authorization code (in a real app, you'd exchange this with GitHub)
            // For now, we'll simulate a successful OAuth flow
            
            // Create a mock user for OAuth (in real implementation, you'd get this from GitHub)
            User oauthUser = new User();
            oauthUser.setId("oauth_user_" + System.currentTimeMillis());
            oauthUser.setUsername("github_user");
            oauthUser.setEmail("github@example.com");
            oauthUser.setFirstName("GitHub");
            oauthUser.setLastName("User");
            oauthUser.setGithubId("12345");
            oauthUser.setPasswordEnabled(false);
            oauthUser.setActive(true);
            oauthUser.setRoles(java.util.Arrays.asList("USER"));
            
            // Save or update user in database
            User savedUser = userService.save(oauthUser);
            
            // Create authentication token
            Collection<GrantedAuthority> authorities = savedUser.getRoles().stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());
            
            CustomUserDetailsService.CustomUserPrincipal principal = 
                    new CustomUserDetailsService.CustomUserPrincipal(savedUser);
            
            UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            // Store in session
            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                    SecurityContextHolder.getContext());
            
            // Prepare response
            response.put("success", true);
            response.put("message", "OAuth login successful");
            response.put("user", createUserResponse(savedUser));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "OAuth token exchange failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}