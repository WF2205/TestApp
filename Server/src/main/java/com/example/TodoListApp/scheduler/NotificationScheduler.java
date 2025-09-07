package com.example.TodoListApp.scheduler;

import com.example.TodoListApp.entity.User;
import com.example.TodoListApp.service.NotificationService;
import com.example.TodoListApp.service.TodoService;
import com.example.TodoListApp.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class NotificationScheduler {

    private static final Logger logger = LoggerFactory.getLogger(NotificationScheduler.class);

    @Autowired
    private TodoService todoService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserService userService;

    // Check for overdue todos every hour
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void checkOverdueTodos() {
        logger.info("Starting overdue todos check...");
        
        try {
            // Get all active users
            var users = userService.findAllActiveUsers();
            
            for (User user : users) {
                try {
                    todoService.checkAndNotifyOverdueTodos(user.getId());
                } catch (Exception e) {
                    logger.error("Error checking overdue todos for user {}: {}", user.getId(), e.getMessage());
                }
            }
            
            logger.info("Overdue todos check completed for {} users", users.size());
        } catch (Exception e) {
            logger.error("Error during overdue todos check: {}", e.getMessage());
        }
    }

    // Check for todos due soon every 6 hours
    @Scheduled(fixedRate = 21600000) // 6 hours in milliseconds
    public void checkTodosDueSoon() {
        logger.info("Starting todos due soon check...");
        
        try {
            // Get all active users
            var users = userService.findAllActiveUsers();
            
            for (User user : users) {
                try {
                    // Check for todos due in the next 24 hours
                    todoService.checkAndNotifyDueSoonTodos(user.getId(), 24);
                } catch (Exception e) {
                    logger.error("Error checking due soon todos for user {}: {}", user.getId(), e.getMessage());
                }
            }
            
            logger.info("Todos due soon check completed for {} users", users.size());
        } catch (Exception e) {
            logger.error("Error during todos due soon check: {}", e.getMessage());
        }
    }

    // Cleanup expired notifications daily at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupExpiredNotifications() {
        logger.info("Starting notification cleanup...");
        
        try {
            notificationService.cleanupExpiredNotifications();
            notificationService.cleanupOldPendingNotifications(24); // Clean up notifications older than 24 hours
            
            logger.info("Notification cleanup completed");
        } catch (Exception e) {
            logger.error("Error during notification cleanup: {}", e.getMessage());
        }
    }

    // Send welcome notifications to new users (check every 5 minutes)
    @Scheduled(fixedRate = 300000) // 5 minutes in milliseconds
    public void sendWelcomeNotifications() {
        logger.debug("Checking for new users to send welcome notifications...");
        
        try {
            // This could be enhanced to track which users have already received welcome notifications
            // For now, we'll skip this to avoid sending duplicate welcome messages
            logger.debug("Welcome notification check completed");
        } catch (Exception e) {
            logger.error("Error during welcome notification check: {}", e.getMessage());
        }
    }
}