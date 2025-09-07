package com.example.TodoListApp.repository;

import com.example.TodoListApp.entity.Todo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends MongoRepository<Todo, String> {
    
    List<Todo> findByUserIdAndIsDeletedFalse(String userId);
    
    List<Todo> findByUserIdAndStatusAndIsDeletedFalse(String userId, Todo.TodoStatus status);
    
    List<Todo> findByUserIdAndPriorityAndIsDeletedFalse(String userId, Todo.TodoPriority priority);
    
    List<Todo> findByUserIdAndTagsContainingAndIsDeletedFalse(String userId, String tag);
    
    @Query("{ 'userId': ?0, 'dueDate': { $lt: ?1 }, 'status': { $ne: 'COMPLETED' }, 'isDeleted': false }")
    List<Todo> findOverdueTodosByUserId(String userId, LocalDateTime now);
    
    @Query("{ 'userId': ?0, 'dueDate': { $gte: ?1, $lte: ?2 }, 'status': { $ne: 'COMPLETED' }, 'isDeleted': false }")
    List<Todo> findTodosDueSoonByUserId(String userId, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'userId': ?0, 'title': { $regex: ?1, $options: 'i' }, 'isDeleted': false }")
    List<Todo> findByUserIdAndTitleContainingIgnoreCase(String userId, String title);
    
    @Query("{ 'userId': ?0, 'description': { $regex: ?1, $options: 'i' }, 'isDeleted': false }")
    List<Todo> findByUserIdAndDescriptionContainingIgnoreCase(String userId, String description);
    
    @Query("{ 'userId': ?0, 'createdAt': { $gte: ?1, $lte: ?2 }, 'isDeleted': false }")
    List<Todo> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'userId': ?0, 'updatedAt': { $gte: ?1, $lte: ?2 }, 'isDeleted': false }")
    List<Todo> findByUserIdAndUpdatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    @Query("{ 'userId': ?0, 'completedAt': { $gte: ?1, $lte: ?2 }, 'isDeleted': false }")
    List<Todo> findByUserIdAndCompletedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    long countByUserIdAndIsDeletedFalse(String userId);
    
    long countByUserIdAndStatusAndIsDeletedFalse(String userId, Todo.TodoStatus status);
    
    long countByUserIdAndPriorityAndIsDeletedFalse(String userId, Todo.TodoPriority priority);
    
    Optional<Todo> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);
    
    List<Todo> findByIsDeletedTrue();
    
    @Query("{ 'userId': ?0, 'isDeleted': false }")
    List<Todo> findAllActiveTodosByUserId(String userId);
}