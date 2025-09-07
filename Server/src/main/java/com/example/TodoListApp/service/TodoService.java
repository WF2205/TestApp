package com.example.TodoListApp.service;

import com.example.TodoListApp.entity.Todo;
import com.example.TodoListApp.entity.Notification;
import com.example.TodoListApp.repository.TodoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TodoService {

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private NotificationService notificationService;

    public List<Todo> findAllByUserId(String userId) {
        return todoRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    public Optional<Todo> findByIdAndUserId(String id, String userId) {
        return todoRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
    }

    public List<Todo> findByStatus(String userId, Todo.TodoStatus status) {
        return todoRepository.findByUserIdAndStatusAndIsDeletedFalse(userId, status);
    }

    public List<Todo> findByPriority(String userId, Todo.TodoPriority priority) {
        return todoRepository.findByUserIdAndPriorityAndIsDeletedFalse(userId, priority);
    }

    public List<Todo> findByTag(String userId, String tag) {
        return todoRepository.findByUserIdAndTagsContainingAndIsDeletedFalse(userId, tag);
    }

    public List<Todo> findOverdueTodos(String userId) {
        return todoRepository.findOverdueTodosByUserId(userId, LocalDateTime.now());
    }

    public List<Todo> findTodosDueSoon(String userId, int hoursAhead) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusHours(hoursAhead);
        return todoRepository.findTodosDueSoonByUserId(userId, now, future);
    }

    public List<Todo> searchByTitle(String userId, String title) {
        return todoRepository.findByUserIdAndTitleContainingIgnoreCase(userId, title);
    }

    public List<Todo> searchByDescription(String userId, String description) {
        return todoRepository.findByUserIdAndDescriptionContainingIgnoreCase(userId, description);
    }

    public List<Todo> findByCreatedDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return todoRepository.findByUserIdAndCreatedAtBetween(userId, start, end);
    }

    public List<Todo> findByUpdatedDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return todoRepository.findByUserIdAndUpdatedAtBetween(userId, start, end);
    }

    public List<Todo> findByCompletedDateRange(String userId, LocalDateTime start, LocalDateTime end) {
        return todoRepository.findByUserIdAndCompletedAtBetween(userId, start, end);
    }

    public Todo createTodo(Todo todo) {
        todo.setCreatedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        todo.setStatus(Todo.TodoStatus.PENDING);
        todo.setPriority(Todo.TodoPriority.MEDIUM);
        todo.setDeleted(false);
        
        Todo savedTodo = todoRepository.save(todo);
        
        // Send notification for todo creation
        notificationService.createNotification(
            savedTodo.getUserId(),
            "New Todo Created",
            "You have created a new todo: " + savedTodo.getTitle(),
            Notification.NotificationType.TODO_CREATED,
            savedTodo.getId()
        );
        
        return savedTodo;
    }

    public Todo updateTodo(String id, String userId, Todo updatedTodo) {
        Optional<Todo> existingTodoOpt = todoRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (existingTodoOpt.isEmpty()) {
            throw new RuntimeException("Todo not found");
        }
        
        Todo existingTodo = existingTodoOpt.get();
        
        // Track if status changed to completed
        boolean wasCompleted = existingTodo.isCompleted();
        boolean isNowCompleted = updatedTodo.getStatus() == Todo.TodoStatus.COMPLETED;
        
        // Update fields
        existingTodo.setTitle(updatedTodo.getTitle());
        existingTodo.setDescription(updatedTodo.getDescription());
        existingTodo.setStatus(updatedTodo.getStatus());
        existingTodo.setPriority(updatedTodo.getPriority());
        existingTodo.setDueDate(updatedTodo.getDueDate());
        existingTodo.setTags(updatedTodo.getTags());
        existingTodo.setAttachments(updatedTodo.getAttachments());
        existingTodo.setUpdatedAt(LocalDateTime.now());
        
        // Handle completion
        if (isNowCompleted && !wasCompleted) {
            existingTodo.setCompletedAt(LocalDateTime.now());
            
            // Send completion notification
            notificationService.createNotification(
                userId,
                "Todo Completed",
                "Congratulations! You have completed: " + existingTodo.getTitle(),
                Notification.NotificationType.TODO_COMPLETED,
                existingTodo.getId()
            );
        } else if (!isNowCompleted && wasCompleted) {
            existingTodo.setCompletedAt(null);
        }
        
        Todo savedTodo = todoRepository.save(existingTodo);
        
        // Send update notification if not completion
        if (!isNowCompleted || wasCompleted) {
            notificationService.createNotification(
                userId,
                "Todo Updated",
                "Your todo has been updated: " + savedTodo.getTitle(),
                Notification.NotificationType.TODO_UPDATED,
                savedTodo.getId()
            );
        }
        
        return savedTodo;
    }

    public void deleteTodo(String id, String userId) {
        Optional<Todo> todoOpt = todoRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (todoOpt.isEmpty()) {
            throw new RuntimeException("Todo not found");
        }
        
        Todo todo = todoOpt.get();
        todo.setDeleted(true);
        todo.setDeletedAt(LocalDateTime.now());
        todo.setUpdatedAt(LocalDateTime.now());
        
        todoRepository.save(todo);
    }

    public void markAsCompleted(String id, String userId) {
        Optional<Todo> todoOpt = todoRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (todoOpt.isEmpty()) {
            throw new RuntimeException("Todo not found");
        }
        
        Todo todo = todoOpt.get();
        todo.markAsCompleted();
        todo.setUpdatedAt(LocalDateTime.now());
        
        Todo savedTodo = todoRepository.save(todo);
        
        // Send completion notification
        notificationService.createNotification(
            userId,
            "Todo Completed",
            "Congratulations! You have completed: " + savedTodo.getTitle(),
            Notification.NotificationType.TODO_COMPLETED,
            savedTodo.getId()
        );
    }

    public void markAsPending(String id, String userId) {
        Optional<Todo> todoOpt = todoRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (todoOpt.isEmpty()) {
            throw new RuntimeException("Todo not found");
        }
        
        Todo todo = todoOpt.get();
        todo.markAsPending();
        todo.setUpdatedAt(LocalDateTime.now());
        
        todoRepository.save(todo);
    }

    public void markAsInProgress(String id, String userId) {
        Optional<Todo> todoOpt = todoRepository.findByIdAndUserIdAndIsDeletedFalse(id, userId);
        if (todoOpt.isEmpty()) {
            throw new RuntimeException("Todo not found");
        }
        
        Todo todo = todoOpt.get();
        todo.markAsInProgress();
        todo.setUpdatedAt(LocalDateTime.now());
        
        todoRepository.save(todo);
    }

    public long countByUserId(String userId) {
        return todoRepository.countByUserIdAndIsDeletedFalse(userId);
    }

    public long countByStatus(String userId, Todo.TodoStatus status) {
        return todoRepository.countByUserIdAndStatusAndIsDeletedFalse(userId, status);
    }

    public long countByPriority(String userId, Todo.TodoPriority priority) {
        return todoRepository.countByUserIdAndPriorityAndIsDeletedFalse(userId, priority);
    }

    public List<Todo> findAllActiveTodos(String userId) {
        return todoRepository.findAllActiveTodosByUserId(userId);
    }

    public void checkAndNotifyOverdueTodos(String userId) {
        List<Todo> overdueTodos = findOverdueTodos(userId);
        
        for (Todo todo : overdueTodos) {
            notificationService.createNotification(
                userId,
                "Todo Overdue",
                "Your todo is overdue: " + todo.getTitle(),
                Notification.NotificationType.TODO_OVERDUE,
                todo.getId()
            );
        }
    }

    public void checkAndNotifyDueSoonTodos(String userId, int hoursAhead) {
        List<Todo> dueSoonTodos = findTodosDueSoon(userId, hoursAhead);
        
        for (Todo todo : dueSoonTodos) {
            notificationService.createNotification(
                userId,
                "Todo Due Soon",
                "Your todo is due soon: " + todo.getTitle(),
                Notification.NotificationType.TODO_DUE_SOON,
                todo.getId()
            );
        }
    }
}