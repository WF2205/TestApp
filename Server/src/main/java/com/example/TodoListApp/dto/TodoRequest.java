package com.example.TodoListApp.dto;

import com.example.TodoListApp.entity.Todo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class TodoRequest {
    
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private Todo.TodoStatus status;
    private Todo.TodoPriority priority;
    private LocalDateTime dueDate;
    private List<String> tags;
    private List<String> attachments;
    
    // Constructors
    public TodoRequest() {}
    
    public TodoRequest(String title, String description) {
        this.title = title;
        this.description = description;
    }
    
    // Getters and Setters
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Todo.TodoStatus getStatus() {
        return status;
    }
    
    public void setStatus(Todo.TodoStatus status) {
        this.status = status;
    }
    
    public Todo.TodoPriority getPriority() {
        return priority;
    }
    
    public void setPriority(Todo.TodoPriority priority) {
        this.priority = priority;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
    
    public List<String> getTags() {
        return tags;
    }
    
    public void setTags(List<String> tags) {
        this.tags = tags;
    }
    
    public List<String> getAttachments() {
        return attachments;
    }
    
    public void setAttachments(List<String> attachments) {
        this.attachments = attachments;
    }
    
    // Helper method to convert to Todo entity
    public Todo toTodo() {
        Todo todo = new Todo();
        todo.setTitle(this.title);
        todo.setDescription(this.description);
        todo.setStatus(this.status != null ? this.status : Todo.TodoStatus.PENDING);
        todo.setPriority(this.priority != null ? this.priority : Todo.TodoPriority.MEDIUM);
        todo.setDueDate(this.dueDate);
        todo.setTags(this.tags);
        todo.setAttachments(this.attachments);
        return todo;
    }
}