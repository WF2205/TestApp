package com.example.TodoListApp.repository;

import com.example.TodoListApp.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByGithubId(String githubId);
    
    List<User> findByActiveTrue();
    
    List<User> findByRolesContaining(String role);
    
    @Query("{ 'username': { $regex: ?0, $options: 'i' } }")
    List<User> findByUsernameContainingIgnoreCase(String username);
    
    @Query("{ 'email': { $regex: ?0, $options: 'i' } }")
    List<User> findByEmailContainingIgnoreCase(String email);
    
    @Query("{ 'firstName': { $regex: ?0, $options: 'i' } }")
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    
    @Query("{ 'lastName': { $regex: ?0, $options: 'i' } }")
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    boolean existsByGithubId(String githubId);
}