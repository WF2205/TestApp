package com.example.TodoListApp.config;

import com.example.TodoListApp.entity.User;
import com.example.TodoListApp.service.UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserService userService;

    public CustomOAuth2UserService(UserService userService) {
        this.userService = userService;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = new DefaultOAuth2UserService().loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        if ("github".equals(registrationId)) {
            return processGitHubUser(oauth2User);
        }
        
        throw new OAuth2AuthenticationException("Unsupported OAuth2 provider: " + registrationId);
    }

    private OAuth2User processGitHubUser(OAuth2User oauth2User) {
        Map<String, Object> attributes = oauth2User.getAttributes();
        
        String githubId = String.valueOf(attributes.get("id"));
        String username = (String) attributes.get("login");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String avatarUrl = (String) attributes.get("avatar_url");
        
        // Handle case where email might be null (GitHub users can hide their email)
        if (email == null) {
            email = username + "@github.local"; // Fallback email
        }
        
        // Handle case where name might be null
        if (name == null) {
            name = username;
        }
        
        // Split name into first and last name
        String[] nameParts = name.split(" ", 2);
        String firstName = nameParts[0];
        String lastName = nameParts.length > 1 ? nameParts[1] : null;
        
        // Create or update user
        User user = userService.findOrCreateUserFromOAuth(githubId, username, email, firstName, lastName, avatarUrl);
        
        return new CustomOAuth2User(oauth2User.getAttributes(), oauth2User.getAuthorities(), user);
    }
}