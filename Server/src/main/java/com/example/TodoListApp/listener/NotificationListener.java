package com.example.TodoListApp.listener;

import com.example.TodoListApp.entity.Notification;
import com.example.TodoListApp.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationListener {

    private static final Logger logger = LoggerFactory.getLogger(NotificationListener.class);

    @Autowired
    private NotificationService notificationService;

    @RabbitListener(queues = "${notification.queue.name}")
    public void handleNotification(Notification notification) {
        try {
            logger.info("Processing notification: {}", notification.getId());
            
            // Process the notification
            notificationService.processNotificationFromQueue(notification);
            
            logger.info("Successfully processed notification: {}", notification.getId());
            
        } catch (Exception e) {
            logger.error("Failed to process notification: {}", notification.getId(), e);
            
            // The notification will be moved to DLQ automatically due to exception
            throw new RuntimeException("Notification processing failed", e);
        }
    }

    @RabbitListener(queues = "notification.dlq")
    public void handleFailedNotification(Notification notification) {
        logger.warn("Received failed notification in DLQ: {}", notification.getId());
        
        // Mark notification as failed in database
        try {
            notificationService.processNotificationFromQueue(notification);
        } catch (Exception e) {
            logger.error("Failed to mark notification as failed: {}", notification.getId(), e);
        }
    }
}