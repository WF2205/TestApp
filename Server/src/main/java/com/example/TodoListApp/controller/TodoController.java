package com.example.TodoListApp.controller;

import com.example.TodoListApp.config.CustomOAuth2User;
import com.example.TodoListApp.config.CustomUserDetailsService;
import com.example.TodoListApp.entity.Todo;
import com.example.TodoListApp.entity.User;
import com.example.TodoListApp.service.TodoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/todos")
@CrossOrigin(origins = "*")
public class TodoController {

    @Autowired
    private TodoService todoService;

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
    public ResponseEntity<List<Todo>> getAllTodos(@AuthenticationPrincipal Object principal) {
        try {
            System.out.println("Principal type: " + (principal != null ? principal.getClass().getName() : "null"));
            String userId = getUserId(principal);
            System.out.println("User ID: " + userId);
            List<Todo> todos = todoService.findAllByUserId(userId);
            System.out.println("Found " + todos.size() + " todos");
            return ResponseEntity.ok(todos);
        } catch (Exception e) {
            System.out.println("Error in getAllTodos: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getTodoById(@AuthenticationPrincipal Object principal,
                                         @PathVariable String id) {
        String userId = getUserId(principal);
        Optional<Todo> todo = todoService.findByIdAndUserId(id, userId);
        if (todo.isPresent()) {
            return ResponseEntity.ok(todo.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Todo> createTodo(@AuthenticationPrincipal Object principal,
                                        @Valid @RequestBody Todo todo) {
        String userId = getUserId(principal);
        todo.setUserId(userId);
        Todo createdTodo = todoService.createTodo(todo);
        return ResponseEntity.ok(createdTodo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> updateTodo(@AuthenticationPrincipal Object principal,
                                       @PathVariable String id,
                                       @Valid @RequestBody Todo todo) {
        String userId = getUserId(principal);
        Todo updatedTodo = todoService.updateTodo(id, userId, todo);
        return ResponseEntity.ok(updatedTodo);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTodo(@AuthenticationPrincipal Object principal,
                                                       @PathVariable String id) {
        String userId = getUserId(principal);
        todoService.deleteTodo(id, userId);
        Map<String, String> response = Map.of(
            "message", "Todo deleted successfully"
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/complete")
    public ResponseEntity<Todo> markAsCompleted(@AuthenticationPrincipal Object principal,
                                             @PathVariable String id) {
        String userId = getUserId(principal);
        todoService.markAsCompleted(id, userId);
        Optional<Todo> todo = todoService.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(todo.get());
    }

    @PutMapping("/{id}/pending")
    public ResponseEntity<Todo> markAsPending(@AuthenticationPrincipal Object principal,
                                           @PathVariable String id) {
        String userId = getUserId(principal);
        todoService.markAsPending(id, userId);
        Optional<Todo> todo = todoService.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(todo.get());
    }

    @PutMapping("/{id}/in-progress")
    public ResponseEntity<Todo> markAsInProgress(@AuthenticationPrincipal Object principal,
                                             @PathVariable String id) {
        String userId = getUserId(principal);
        todoService.markAsInProgress(id, userId);
        Optional<Todo> todo = todoService.findByIdAndUserId(id, userId);
        return ResponseEntity.ok(todo.get());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Todo>> getTodosByStatus(@AuthenticationPrincipal Object principal,
                                                    @PathVariable String status) {
        String userId = getUserId(principal);
        Todo.TodoStatus todoStatus = Todo.TodoStatus.valueOf(status.toUpperCase());
        List<Todo> todos = todoService.findByStatus(userId, todoStatus);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/priority/{priority}")
    public ResponseEntity<List<Todo>> getTodosByPriority(@AuthenticationPrincipal Object principal,
                                                       @PathVariable String priority) {
        String userId = getUserId(principal);
        Todo.TodoPriority todoPriority = Todo.TodoPriority.valueOf(priority.toUpperCase());
        List<Todo> todos = todoService.findByPriority(userId, todoPriority);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/tag/{tag}")
    public ResponseEntity<List<Todo>> getTodosByTag(@AuthenticationPrincipal Object principal,
                                                 @PathVariable String tag) {
        String userId = getUserId(principal);
        List<Todo> todos = todoService.findByTag(userId, tag);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<Todo>> getOverdueTodos(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        List<Todo> todos = todoService.findOverdueTodos(userId);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/due-soon")
    public ResponseEntity<List<Todo>> getTodosDueSoon(@AuthenticationPrincipal Object principal,
                                                    @RequestParam(defaultValue = "24") int hoursAhead) {
        String userId = getUserId(principal);
        List<Todo> todos = todoService.findTodosDueSoon(userId, hoursAhead);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Todo>> searchTodos(@AuthenticationPrincipal Object principal,
                                                @RequestParam String query) {
        String userId = getUserId(principal);
        List<Todo> todos = todoService.searchByTitle(userId, query);
        return ResponseEntity.ok(todos);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> getTodoStats(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        
        Map<String, Long> stats = Map.of(
            "total", todoService.countByUserId(userId),
            "pending", todoService.countByStatus(userId, Todo.TodoStatus.PENDING),
            "inProgress", todoService.countByStatus(userId, Todo.TodoStatus.IN_PROGRESS),
            "completed", todoService.countByStatus(userId, Todo.TodoStatus.COMPLETED),
            "cancelled", todoService.countByStatus(userId, Todo.TodoStatus.CANCELLED),
            "high", todoService.countByPriority(userId, Todo.TodoPriority.HIGH),
            "urgent", todoService.countByPriority(userId, Todo.TodoPriority.URGENT)
        );
        
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/check-overdue")
    public ResponseEntity<Map<String, String>> checkOverdueTodos(@AuthenticationPrincipal Object principal) {
        String userId = getUserId(principal);
        todoService.checkAndNotifyOverdueTodos(userId);
        
        Map<String, String> response = Map.of(
            "message", "Overdue todos check completed"
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/check-due-soon")
    public ResponseEntity<Map<String, String>> checkDueSoonTodos(@AuthenticationPrincipal Object principal,
                                                               @RequestParam(defaultValue = "24") int hoursAhead) {
        String userId = getUserId(principal);
        todoService.checkAndNotifyDueSoonTodos(userId, hoursAhead);
        
        Map<String, String> response = Map.of(
            "message", "Due soon todos check completed"
        );
        return ResponseEntity.ok(response);
    }
}