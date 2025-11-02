package com.example.taskManager.unit.services;

import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;

    @BeforeEach
    void setup() {
        user = new User();
        user.setId(1L);
        user.setUsername("demoUser");

        task = new Task();
        task.setId(1L);
        task.setTitle("Mocked Task");
        task.setDescription("Unit test description");
        task.setCompleted(false);
        task.setUser(user);
    }

    @Test
    void shouldCreateTaskForUserSuccessfully() {
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task saved = taskService.createTaskForUser(task, user);

        assertNotNull(saved);
        assertEquals("Mocked Task", saved.getTitle());
        assertEquals(user, saved.getUser());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldReturnTasksForUser() {
        when(taskRepository.findByUserUsername("demoUser")).thenReturn(List.of(task));

        List<Task> tasks = taskService.getTasksForUser("demoUser");

        assertEquals(1, tasks.size());
        assertEquals("Mocked Task", tasks.get(0).getTitle());
        verify(taskRepository).findByUserUsername("demoUser");
    }

    @Test
    void shouldReturnTaskByIdForUser() {
        when(taskRepository.findByIdAndUserUsername(1L, "demoUser"))
                .thenReturn(Optional.of(task));

        Task found = taskService.getTaskByIdForUser(1L, "demoUser");

        assertEquals("Mocked Task", found.getTitle());
        assertEquals(user, found.getUser());
        verify(taskRepository).findByIdAndUserUsername(1L, "demoUser");
    }

    @Test
    void shouldThrowWhenTaskNotFoundForUser() {
        when(taskRepository.findByIdAndUserUsername(99L, "demoUser"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.getTaskByIdForUser(99L, "demoUser"));
    }

    @Test
    void shouldUpdateTaskForUserSuccessfully() {
        Task updated = new Task();
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Desc");
        updated.setCompleted(true);

        when(taskRepository.findByIdAndUserUsername(1L, "demoUser"))
                .thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task result = taskService.updateTaskForUser(1L, updated, "demoUser");

        assertEquals("Updated Title", result.getTitle());
        assertTrue(result.isCompleted());
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void shouldThrowWhenUpdatingTaskNotFoundForUser() {
        when(taskRepository.findByIdAndUserUsername(2L, "demoUser"))
                .thenReturn(Optional.empty());

        Task updated = new Task();
        updated.setTitle("Nonexistent");

        assertThrows(RuntimeException.class, () -> taskService.updateTaskForUser(2L, updated, "demoUser"));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void shouldDeleteTaskForUserSuccessfully() {
        when(taskRepository.findByIdAndUserUsername(1L, "demoUser"))
                .thenReturn(Optional.of(task));
        doNothing().when(taskRepository).delete(any(Task.class));

        assertDoesNotThrow(() -> taskService.deleteTaskForUser(1L, "demoUser"));
        verify(taskRepository).delete(any(Task.class));
    }

    @Test
    void shouldThrowWhenDeletingNonExistingTaskForUser() {
        when(taskRepository.findByIdAndUserUsername(999L, "demoUser"))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.deleteTaskForUser(999L, "demoUser"));
        verify(taskRepository, never()).delete(any(Task.class));
    }
}
