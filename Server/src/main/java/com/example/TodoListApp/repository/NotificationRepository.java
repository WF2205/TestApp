package com.example.TodoListApp.repository;

import com.example.TodoListApp.entity.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends MongoRepository<Notification, String> {
    
    List<Notification> findByUserIdAndIsDeletedFalseOrderByCreatedAtDesc(String userId);
    
    List<Notification> findByUserIdAndStatusAndIsDeletedFalse(String userId, Notification.NotificationStatus status);
    
    List<Notification> findByUserIdAndTypeAndIsDeletedFalse(String userId, Notification.NotificationType type);
    
    List<Notification> findByUserIdAndPriorityAndIsDeletedFalse(String userId, Notification.NotificationPriority priority);
    
    @Query("{ 'userId': ?0, 'readAt': null, 'isDeleted': false }")
    List<Notification> findUnreadNotificationsByUserId(String userId);
    
    @Query("{ 'userId': ?0, 'readAt': { $ne: null }, 'isDeleted': false }")
    List<Notification> findReadNotificationsByUserId(String userId);
    
    @Query("{ 'userId': ?0, 'expiresAt': { $lt: ?1 }, 'isDeleted': false }")
    List<Notification> findExpiredNotificationsByUserId(String userId, LocalDateTime now);
    
    @Query("{ 'userId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 }, 'isDeleted': false }")
    List<Notification> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'userId': ?0, 'sentAt': { $gte: ?1, $lte: ?2 }, 'isDeleted': false }")
    List<Notification> findByUserIdAndSentAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'userId': ?0, 'readAt': { $gte: ?1, $lte: ?2 }, 'isDeleted': false }")
    List<Notification> findByUserIdAndReadAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'status': 'PENDING', 'createdAt': { $lt: ?0 } }")
    List<Notification> findPendingNotificationsOlderThan(LocalDateTime threshold);
    
    @Query("{ 'status': 'SENT', 'expiresAt': { $lt: ?0 } }")
    List<Notification> findExpiredSentNotifications(LocalDateTime now);
    
    @Query("{ 'todoId': ?0, 'isDeleted': false }")
    List<Notification> findByTodoId(String todoId);
    
    long countByUserIdAndIsDeletedFalse(String userId);
    
    long countByUserIdAndStatusAndIsDeletedFalse(String userId, Notification.NotificationStatus status);
    
    long countByUserIdAndTypeAndIsDeletedFalse(String userId, Notification.NotificationType type);
    
    long countByUserIdAndReadAtNullAndIsDeletedFalse(String userId);
    
    Optional<Notification> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);
    
    List<Notification> findByIsDeletedTrue();
    
    @Query("{ 'userId': ?0, 'isDeleted': false }")
    List<Notification> findAllActiveNotificationsByUserId(String userId);
}