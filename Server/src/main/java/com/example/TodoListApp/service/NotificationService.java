package com.example.TodoListApp.service;

import com.example.TodoListApp.entity.Notification;
import com.example.TodoListApp.repository.NotificationRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("${notification.exchange.name}")
    private String exchangeName;

    @Value("${notification.routing.key}")
    private String routingKey;

    public List<Notification> findAllByUserId(String userId) {
        return notificationRepository.findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(userId);
    }

    public Optional<Notification> findByIdAndUserId(String id, String userId) {
        return notificationRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
    }

    public List<Notification> findByStatus(String userId, Notification.NotificationStatus status) {
        return notificationRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, status);
    }

    public List<Notification> findByType(String userId, Notification.NotificationType type) {
        return notificationRepository.findByUserIdAndTypeAndIsDeletedFalse(userId, type);
    }

    public List<Notification> findByPriority(String userId, Notification.NotificationPriority priority) {
        return notificationRepository.findByUserIdAndPriorityAndIsDeletedFalse(userId, priority);
    }

    public List<Notification> findUnreadNotifications(String userId) {
        return notificationRepository.findUnreadNotificationsByUserId(userId);
    }

    public List<Notification> findReadNotifications(String userId) {
        return notificationRepository.findReadNotificationsByUserId(userId);
    }

    public List<Notification> findExpiredNotifications(String userId) {
        return notificationRepository.findExpiredNotificationsByUserId(userId, LocalDateTime.now());
    }

    public List<Notification> findByCreatedDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return notificationRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
    }

    public List<Notification> findBySentDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return notificationRepository.findByUserIdAndSentAtBetween(userId, start, end);
    }

    public List<Notification> findByReadDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return notificationRepository.findByUserIdAndReadAtBetween(userId, start, end);
    }

    public List<Notification> findByTodoId(String todoId) {
        return notificationRepository.findByTodoId(todoId);
    }

    public Notification createNotification(String userId, String title, String message, 
                                        Notification.NotificationType type, String todoId) {
        return createNotification(userId, title, message, type, todoId, 
                                Notification.NotificationPriority.MEDIUM, null);
    }

    public Notification createNotification(String userId, String title, String message, 
                                        Notification.NotificationType type, String todoId,
                                        Notification.NotificationPriority priority, LocalDateTime expiresAt) {
        Notification notification = new Notification(title, message, userId, type);
        notification.setTodoId(todoId);
        notification.setPriority(priority);
        notification.setExpiresAt(expiresAt);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setStatus(Notification.NotificationStatus.PENDING);
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Send to message queue for processing
        sendToQueue(savedNotification);
        
        return savedNotification;
    }

    public Notification updateNotification(String id, String userId, Notification updatedNotification) {
        Optional<Notification> existingNotificationOpt = notificationRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (existingNotificationOpt.isEmpty()) {
            throw new RuntimeException("Notification not found");
        }
        
        Notification existingNotification = existingNotificationOpt.get();
        
        existingNotification.setTitle(updatedNotification.getTitle());
        existingNotification.setMessage(updatedNotification.getMessage());
        existingNotification.setType(updatedNotification.getType());
        existingNotification.setPriority(updatedNotification.getPriority());
        existingNotification.setExpiresAt(updatedNotification.getExpiresAt());
        existingNotification.setActionUrl(updatedNotification.getActionUrl());
        existingNotification.setMetadata(updatedNotification.getMetadata());
        
        return notificationRepository.save(existingNotification);
    }

    public Notification markAsRead(String id, String userId) {
        Optional<Notification> notificationOpt = notificationRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (notificationOpt.isEmpty()) {
            throw new RuntimeException("Notification not found");
        }
        
        Notification notification = notificationOpt.get();
        notification.markAsRead();
        
        return notificationRepository.save(notification);
    }

    public void markAllAsRead(String userId) {
        List<Notification> unreadNotifications = findUnreadNotifications(userId);
        
        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }
        
        notificationRepository.saveAll(unreadNotifications);
    }

    public void deleteNotification(String id, String userId) {
        Optional<Notification> notificationOpt = notificationRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (notificationOpt.isEmpty()) {
            throw new RuntimeException("Notification not found");
        }
        
        Notification notification = notificationOpt.get();
        notification.setDeleted(true);
        notification.setDeletedAt(LocalDateTime.now());
        
        notificationRepository.save(notification);
    }

    public void deleteAllNotifications(String userId) {
        List<Notification> notifications = findAllByUserId(userId);
        
        for (Notification notification : notifications) {
            notification.setDeleted(true);
            notification.setDeletedAt(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(notifications);
    }

    public long countByUserId(String userId) {
        return notificationRepository.countByUserIdAndIsDeletedFalse(userId);
    }

    public long countByStatus(String userId, Notification.NotificationStatus status) {
        return notificationRepository.countByUserIdAndStatusAndIsDeletedFalse(userId, status);
    }

    public long countByType(String userId, Notification.NotificationType type) {
        return notificationRepository.countByUserIdAndTypeAndIsDeletedFalse(userId, type);
    }

    public long countUnreadByUserId(String userId) {
        return notificationRepository.countByUserIdAndReadAtNullAndIsDeletedFalse(userId);
    }

    public List<Notification> findAllActiveNotifications(String userId) {
        return notificationRepository.findAllActiveNotificationsByUserId(userId);
    }

    public void sendToQueue(Notification notification) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, notification);
        } catch (Exception e) {
            // Log error and mark notification as failed
            notification.setStatus(Notification.NotificationStatus.FAILED);
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to send notification to queue", e);
        }
    }

    public void processNotificationFromQueue(Notification notification) {
        try {
            // Simulate notification processing (email, SMS, push notification, etc.)
            Thread.sleep(1000); // Simulate processing time
            
            notification.markAsSent();
            notificationRepository.save(notification);
            
        } catch (Exception e) {
            notification.markAsFailed();
            notificationRepository.save(notification);
            throw new RuntimeException("Failed to process notification", e);
        }
    }

    public void cleanupExpiredNotifications() {
        List<Notification> expiredNotifications = notificationRepository.findExpiredSentNotifications(LocalDateTime.now());
        
        for (Notification notification : expiredNotifications) {
            notification.setDeleted(true);
            notification.setDeletedAt(LocalDateTime.now());
        }
        
        notificationRepository.saveAll(expiredNotifications);
    }

    public void cleanupOldPendingNotifications(int hoursOld) {
        LocalDateTime threshold = LocalDateTime.now().minusHours(hoursOld);
        List<Notification> oldPendingNotifications = notificationRepository.findPendingNotificationsOlderThan(threshold);
        
        for (Notification notification : oldPendingNotifications) {
            notification.markAsFailed();
        }
        
        notificationRepository.saveAll(oldPendingNotifications);
    }

    public void sendWelcomeNotification(String userId) {
        createNotification(
            userId,
            "Welcome to TodoList App!",
            "Thank you for joining us. Start creating your first todo to get organized!",
            Notification.NotificationType.USER_WELCOME,
            null,
            Notification.NotificationPriority.LOW,
            LocalDateTime.now().plusDays(7) // Expires in 7 days
        );
    }

    public void sendSystemAnnouncement(String title, String message, List<String> userIds) {
        for (String userId : userIds) {
            createNotification(
                userId,
                title,
                message,
                Notification.NotificationType.SYSTEM_ANNOUNCEMENT,
                null,
                Notification.NotificationPriority.MEDIUM,
                LocalDateTime.now().plusDays(30) // Expires in 30 days
            );
        }
    }
}