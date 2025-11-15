package com.example.taskManager.services;

import com.example.taskManager.exception.TaskNotFoundException;
import com.example.taskManager.exception.TaskOperationException;
import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.TaskRepository;
import org.springframework.dao.DataAccessException;
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
        try {
            task.setUser(user);
            return taskRepository.save(task);
        } catch (DataAccessException e) {
            throw new TaskOperationException("Failed to create task");
        }
    }

    /**
     * Get all tasks for a specific user.
     */
    public List<Task> getTasksForUser(String username) {
        try {
            return taskRepository.findByUserUsername(username);
        } catch (DataAccessException e) {
            throw new TaskOperationException("Failed to fetch tasks");
        }
    }

    /**
     * Get a single task by ID for a specific user.
     */
    public Task getTaskByIdForUser(Long id, String username) {
        return taskRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(() -> new TaskNotFoundException("Task not found or access denied"));
    }

    /**
     * Update a task for a specific user.
     */
    public Task updateTaskForUser(Long id, Task updatedTask, String username) {
        Task existing = getTaskByIdForUser(id, username); // ensures ownership

        try {
            existing.setTitle(updatedTask.getTitle());
            existing.setDescription(updatedTask.getDescription());
            existing.setCompleted(updatedTask.isCompleted());
            return taskRepository.save(existing);
        } catch (DataAccessException e) {
            throw new TaskOperationException("Failed to update task");
        }
    }

    /**
     * Delete a task for a specific user.
     */
    public void deleteTaskForUser(Long id, String username) {
        Task existing = getTaskByIdForUser(id, username); // ensures ownership
        try {
            taskRepository.delete(existing);
        } catch (DataAccessException e) {
            throw new TaskOperationException("Failed to delete task");
        }
    }
}
