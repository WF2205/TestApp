package com.example.TodoListApp.dto;

import com.example.TodoListApp.entity.Notification;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Map;

public class NotificationRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;
    
    @NotBlank(message = "Message is required")
    @Size(min = 1, max = 500, message = "Message must be between 1 and 500 characters")
    private String message;
    
    private Notification.NotificationType type;
    private Notification.NotificationPriority priority;
    private LocalDateTime expiresAt;
    private String todoId;
    private String actionUrl;
    private Map<String, Object> metadata;
    
    // Constructors
    public NotificationRequest() {}
    
    public NotificationRequest(String title, String message, Notification.NotificationType type) {
        this.title = title;
        this.message = message;
        this.type = type;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Notification.NotificationType getType() {
        return type;
    }
    
    public void setType(Notification.NotificationType type) {
        this.type = type;
    }
    
    public Notification.NotificationPriority getPriority() {
        return priority;
    }
    
    public void setPriority(Notification.NotificationPriority priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public String getTodoId() {
        return todoId;
    }
    
    public void setTodoId(String todoId) {
        this.todoId = todoId;
    }
    
    public String getActionUrl() {
        return actionUrl;
    }
    
    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }
    
    public Map<String, Object> getMetadata() {
        return metadata;
    }
    
    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
    
    // Helper method to convert to Notification entity
    public Notification toNotification(String userId) {
        Notification notification = new Notification();
        notification.setTitle(this.title);
        notification.setMessage(this.message);
        notification.setUserId(userId);
        notification.setType(this.type);
        notification.setPriority(this.priority != null ? this.priority : Notification.NotificationPriority.MEDIUM);
        notification.setExpiresAt(this.expiresAt);
        notification.setTodoId(this.todoId);
        notification.setActionUrl(this.actionUrl);
        notification.setMetadata(this.metadata);
        return notification;
    }
}