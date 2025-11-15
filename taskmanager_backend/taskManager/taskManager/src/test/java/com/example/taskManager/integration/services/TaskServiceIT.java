package com.example.taskManager.integration.services;

import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import com.example.taskManager.services.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceIT {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User demoUser;

    @BeforeEach
    void setup() {
        taskRepository.deleteAll();
        userRepository.deleteAll();

        demoUser = new User("demoUser", "demoPass");
        demoUser = userRepository.save(demoUser);
    }

    @Test
    void shouldCreateAndRetrieveTasksForUserSuccessfully() {
        Task task = new Task();
        task.setTitle("User-Specific Integration Task");
        task.setDescription("Verifying user-scoped task creation");

        // Create task for demoUser
        Task saved = taskService.createTaskForUser(task, demoUser);
        assertNotNull(saved.getId(), "Task ID should be generated after save");

        // Retrieve tasks for that user
        List<Task> tasks = taskService.getTasksForUser("demoUser");
        assertFalse(tasks.isEmpty(), "User task list should not be empty");
        assertEquals("User-Specific Integration Task", tasks.get(0).getTitle());
    }

    @Test
    void shouldGetSingleTaskByIdForUser() {
        Task task = new Task();
        task.setTitle("Single Fetch Task");
        task.setDescription("Testing getTaskByIdForUser");
        Task saved = taskService.createTaskForUser(task, demoUser);

        Task fetched = taskService.getTaskByIdForUser(saved.getId(), "demoUser");
        assertNotNull(fetched);
        assertEquals("Single Fetch Task", fetched.getTitle());
    }

    @Test
    void shouldUpdateTaskForUserSuccessfully() {
        Task task = new Task();
        task.setTitle("Initial Title");
        task.setDescription("Initial Description");
        task.setCompleted(false);

        Task saved = taskService.createTaskForUser(task, demoUser);

        Task updated = new Task();
        updated.setTitle("Updated Title");
        updated.setDescription("Updated Description");
        updated.setCompleted(true);

        Task result = taskService.updateTaskForUser(saved.getId(), updated, "demoUser");
        assertEquals("Updated Title", result.getTitle());
        assertTrue(result.isCompleted(), "Task should be marked as completed");
    }

    @Test
    void shouldDeleteTaskForUserSuccessfully() {
        Task task = new Task();
        task.setTitle("Delete Me");
        task.setDescription("Will be removed soon");
        Task saved = taskService.createTaskForUser(task, demoUser);

        taskService.deleteTaskForUser(saved.getId(), "demoUser");
        assertTrue(taskRepository.findById(saved.getId()).isEmpty(), "Task should be deleted from DB");
    }

    @Test
    void shouldThrowWhenAccessingTaskOfAnotherUser() {
        // Create task for demoUser
        Task task = new Task();
        task.setTitle("Protected Task");
        task.setDescription("Should not be accessible to others");
        Task saved = taskService.createTaskForUser(task, demoUser);

        // Create another user
        User otherUser = new User("otherUser", "otherPass");
        userRepository.save(otherUser);

        // Prepare the throwing call separately
        Long taskId = saved.getId();
        String otherUsername = otherUser.getUsername();

        // Single invocation inside assertThrows
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.getTaskByIdForUser(taskId, otherUsername),
                "Access should be denied for other users");

        assertTrue(ex.getMessage().toLowerCase().contains("not found"),
                "Exception message should mention 'not found' or 'access denied'");
    }

    @Test
    void shouldThrowWhenUpdatingNonExistingTask() {
        Task nonExistent = new Task();
        nonExistent.setTitle("Ghost Task");
        nonExistent.setDescription("Does not exist");
        nonExistent.setCompleted(false);

        Long invalidTaskId = 99999L;
        String username = demoUser.getUsername();

        // Only one method call inside lambda
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskForUser(invalidTaskId, nonExistent, username));

        assertTrue(ex.getMessage().toLowerCase().contains("not found"),
                "Exception should indicate 'not found'");
    }

}
