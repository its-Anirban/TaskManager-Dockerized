package com.example.taskManager.repository;

import com.example.taskManager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find a user by their unique username.
     */
    Optional<User> findByUsername(String username);

    /**
     * Check if a username already exists.
     * Used during registration to prevent duplicates.
     */
    boolean existsByUsername(String username);
}
