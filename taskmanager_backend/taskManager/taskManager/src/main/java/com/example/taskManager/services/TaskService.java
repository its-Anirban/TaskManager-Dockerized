package com.example.taskManager.services;

import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.TaskRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Create a task for a specific user.
     */
    public Task createTaskForUser(Task task, User user) {
        task.setUser(user);
        return taskRepository.save(task);
    }

    /**
     * Get all tasks for a specific user.
     */
    public List<Task> getTasksForUser(String username) {
        return taskRepository.findByUserUsername(username);
    }

    /**
     * Get a single task by ID for a specific user.
     */
    public Task getTaskByIdForUser(Long id, String username) {
        return taskRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new RuntimeException("Task not found or access denied"));
    }

    /**
     * Update a task for a specific user.
     */
    public Task updateTaskForUser(Long id, Task updatedTask, String username) {
        Task existing = getTaskByIdForUser(id, username); // ensures ownership
        existing.setTitle(updatedTask.getTitle());
        existing.setDescription(updatedTask.getDescription());
        existing.setCompleted(updatedTask.isCompleted());
        return taskRepository.save(existing);
    }

    /**
     * Delete a task for a specific user.
     */
    public void deleteTaskForUser(Long id, String username) {
        Task existing = getTaskByIdForUser(id, username); // ensures ownership
        taskRepository.delete(existing);
    }
}
