package com.example.taskManager.controller;

import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.services.TaskService;
import com.example.taskManager.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;
    private final UserRepository userRepository;

    public TaskController(TaskService taskService, UserRepository userRepository) {
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    /**
     * Create a new task and automatically link it to the logged-in user.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Task savedTask = taskService.createTaskForUser(task, user);
        return new ResponseEntity<>(savedTask, HttpStatus.CREATED);
    }

    /**
     * Get all tasks for the logged-in user.
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasksForUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Task> tasks = taskService.getTasksForUser(username);
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    /**
     * Get a specific task belonging to the logged-in user.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Task task = taskService.getTaskByIdForUser(id, username);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    /**
     * Update a specific task belonging to the logged-in user.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Task task = taskService.updateTaskForUser(id, updatedTask, username);
        return new ResponseEntity<>(task, HttpStatus.OK);
    }

    /**
     * Delete a specific task belonging to the logged-in user.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        taskService.deleteTaskForUser(id, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
