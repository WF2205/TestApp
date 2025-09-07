package com.example.TodoListApp.controller;

import com.example.TodoListApp.config.CustomOAuth2User;
import com.example.TodoListApp.config.CustomUserDetailsService;
import com.example.TodoListApp.entity.Notification;
import com.example.TodoListApp.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * Helper method to extract user ID from either OAuth2 or username/password authentication
     */
    private String getUserId(Object principal) {
        if (principal instanceof CustomOAuth2User) {
            return ((CustomOAuth2User) principal).getId();
        } else if (principal instanceof CustomUserDetailsService.CustomUserPrincipal) {
            return ((CustomUserDetailsService.CustomUserPrincipal) principal).getUser().getId();
        }
        throw new IllegalArgumentException("Unsupported authentication principal type: " + 
            (principal != null ? principal.getClass().getName() : "null"));
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        List<Notification> notifications = notificationService.findAllByUserId(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Notification> getNotificationById(@AuthenticationPrincipal Object principal,
                                                        @PathVariable String id) {
        String userId = getUserId(principal);
        Optional<Notification> notification = notificationService.findByIdAndUserId(id, userId);
        if (notification.isPresent()) {
            return ResponseEntity.ok(notification.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Notification> updateNotification(@AuthenticationPrincipal Object principal,
                                                        @PathVariable String id,
                                                        @Valid @RequestBody Notification notification) {
        String userId = getUserId(principal);
        Notification updatedNotification = notificationService.updateNotification(id, userId, notification);
        return ResponseEntity.ok(updatedNotification);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNotification(@AuthenticationPrincipal Object principal,
                                                              @PathVariable String id) {
        String userId = getUserId(principal);
        notificationService.deleteNotification(id, userId);
        Map<String, String> response = Map.of(
            "message", "Notification deleted successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@AuthenticationPrincipal Object principal,
                                                       @PathVariable String id) {
        String userId = getUserId(principal);
        Notification updatedNotification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(updatedNotification);
    }

    @PutMapping("/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        notificationService.markAllAsRead(userId);
        Map<String, String> response = Map.of(
            "message", "All notifications marked as read"
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/all")
    public ResponseEntity<Map<String, String>> deleteAllNotifications(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        notificationService.deleteAllNotifications(userId);
        Map<String, String> response = Map.of(
            "message", "All notifications deleted successfully"
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        List<Notification> notifications = notificationService.findUnreadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/read")
    public ResponseEntity<List<Notification>> getReadNotifications(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        List<Notification> notifications = notificationService.findReadNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> getNotificationsByStatus(@AuthenticationPrincipal Object principal,
                                                                     @PathVariable String status) {
        String userId = getUserId(principal);
        Notification.NotificationStatus notificationStatus = Notification.NotificationStatus.valueOf(status.toUpperCase());
        List<Notification> notifications = notificationService.findByStatus(userId, notificationStatus);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@AuthenticationPrincipal Object principal,
                                                                   @PathVariable String type) {
        String userId = getUserId(principal);
        Notification.NotificationType notificationType = Notification.NotificationType.valueOf(type.toUpperCase());
        List<Notification> notifications = notificationService.findByType(userId, notificationType);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Notification>> getNotificationsByPriority(@AuthenticationPrincipal Object principal,
                                                                     @PathVariable String priority) {
        String userId = getUserId(principal);
        Notification.NotificationPriority notificationPriority = Notification.NotificationPriority.valueOf(priority.toUpperCase());
        List<Notification> notifications = notificationService.findByPriority(userId, notificationPriority);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/expired")
    public ResponseEntity<List<Notification>> getExpiredNotifications(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        List<Notification> notifications = notificationService.findExpiredNotifications(userId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/todo/{todoId}")
    public ResponseEntity<List<Notification>> getNotificationsByTodoId(@AuthenticationPrincipal Object principal,
                                                                  @PathVariable String todoId) {
        List<Notification> notifications = notificationService.findByTodoId(todoId);
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getNotificationStats(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        
        Map<String, Long> stats = Map.of(
            "total", notificationService.countByUserId(userId),
            "unread", notificationService.countUnreadByUserId(userId),
            "pending", notificationService.countByStatus(userId, Notification.NotificationStatus.PENDING),
            "sent", notificationService.countByStatus(userId, Notification.NotificationStatus.SENT),
            "read", notificationService.countByStatus(userId, Notification.NotificationStatus.READ),
            "failed", notificationService.countByStatus(userId, Notification.NotificationStatus.FAILED)
        );
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/create")
    public ResponseEntity<Notification> createNotification(@AuthenticationPrincipal Object principal,
                                                         @Valid @RequestBody Map<String, Object> notificationData) {
        String userId = getUserId(principal);
        String title = (String) notificationData.get("title");
        String message = (String) notificationData.get("message");
        String typeStr = (String) notificationData.get("type");
        String todoId = (String) notificationData.get("todoId");
        
        Notification.NotificationType type = Notification.NotificationType.valueOf(typeStr.toUpperCase());
        
        Notification notification = notificationService.createNotification(userId, title, message, type, todoId);
        return ResponseEntity.ok(notification);
    }

    @PostMapping("/welcome")
    public ResponseEntity<Map<String, String>> sendWelcomeNotification(@AuthenticationPrincipal CustomOAuth2User oauth2User) {
        String userId = oauth2User.getId();
        notificationService.sendWelcomeNotification(userId);
        
        Map<String, String> response = Map.of(
            "message", "Welcome notification sent"
        );
        return ResponseEntity.ok(response);
    }

    // Admin endpoints
    @PostMapping("/admin/announcement")
    public ResponseEntity<Map<String, String>> sendSystemAnnouncement(@AuthenticationPrincipal CustomOAuth2User oauth2User,
                                                                   @RequestBody Map<String, Object> announcementData) {
        // Check if user is admin
        if (!oauth2User.getUser().getRoles().contains("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        String title = (String) announcementData.get("title");
        String message = (String) announcementData.get("message");
        @SuppressWarnings("unchecked")
        List<String> userIds = (List<String>) announcementData.get("userIds");
        
        notificationService.sendSystemAnnouncement(title, message, userIds);
        
        Map<String, String> response = Map.of(
            "message", "System announcement sent"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/cleanup")
    public ResponseEntity<Map<String, String>> cleanupNotifications(@AuthenticationPrincipal CustomOAuth2User oauth2User) {
        // Check if user is admin
        if (!oauth2User.getUser().getRoles().contains("ADMIN")) {
            return ResponseEntity.status(403).build();
        }
        
        notificationService.cleanupExpiredNotifications();
        notificationService.cleanupOldPendingNotifications(24); // Clean up notifications older than 24 hours
        
        Map<String, String> response = Map.of(
            "message", "Notification cleanup completed"
        );
        return ResponseEntity.ok(response);
    }
}