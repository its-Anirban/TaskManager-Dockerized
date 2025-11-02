package com.example.taskManager.repository;

import com.example.taskManager.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Fetch all tasks belonging to a specific user.
     */
    List<Task> findByUserUsername(String username);

    /**
     * Fetch a single task by its ID and the username of the owner.
     * Prevents users from accessing others' tasks by ID.
     */
    Optional<Task> findByIdAndUserUsername(Long id, String username);
}
