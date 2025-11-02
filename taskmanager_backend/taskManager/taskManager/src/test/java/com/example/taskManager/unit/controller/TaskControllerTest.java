package com.example.taskManager.unit.controller;

import com.example.taskManager.controller.TaskController;
import com.example.taskManager.exception.GlobalExceptionHandler;
import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.UserRepository;
import com.example.taskManager.services.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {TaskController.class, GlobalExceptionHandler.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(new UsernamePasswordAuthenticationToken(username, null, List.of()));
        SecurityContextHolder.setContext(context);
    }

    private Task createSampleTask(Long id) {
        Task t = new Task();
        t.setId(id);
        t.setTitle("Sample Task " + id);
        t.setDescription("Description " + id);
        t.setCompleted(false);
        return t;
    }

    // --- GET ALL ---
    @Test
    @DisplayName("GET /api/tasks - should return list of tasks for logged-in user")
    void shouldReturnListOfTasksForUser() throws Exception {
        mockAuthenticatedUser("demoUser");
        List<Task> tasks = Arrays.asList(createSampleTask(1L), createSampleTask(2L));
        when(taskService.getTasksForUser("demoUser")).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title", is("Sample Task 1")))
                .andExpect(jsonPath("$[1].title", is("Sample Task 2")));

        verify(taskService).getTasksForUser("demoUser");
    }

    // --- GET BY ID ---
    @Test
    @DisplayName("GET /api/tasks/{id} - should return task by ID for user")
    void shouldReturnTaskByIdForUser() throws Exception {
        mockAuthenticatedUser("demoUser");
        Task task = createSampleTask(10L);
        when(taskService.getTaskByIdForUser(10L, "demoUser")).thenReturn(task);

        mockMvc.perform(get("/api/tasks/{id}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.title", is("Sample Task 10")));

        verify(taskService).getTaskByIdForUser(10L, "demoUser");
    }

    @Test
    @DisplayName("GET /api/tasks/{id} - missing ID should return 404")
    void shouldReturn404WhenTaskNotFound() throws Exception {
        mockAuthenticatedUser("demoUser");
        when(taskService.getTaskByIdForUser(99L, "demoUser"))
                .thenThrow(new RuntimeException("Task not found"));

        mockMvc.perform(get("/api/tasks/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Task not found")));

        verify(taskService).getTaskByIdForUser(99L, "demoUser");
    }

    // --- CREATE ---
    @Test
    @DisplayName("POST /api/tasks - should create new task for user")
    void shouldCreateTaskForUser() throws Exception {
        mockAuthenticatedUser("demoUser");

        User user = new User();
        user.setId(1L);
        user.setUsername("demoUser");
        when(userRepository.findByUsername("demoUser")).thenReturn(Optional.of(user));

        Task toCreate = new Task();
        toCreate.setTitle("New Task");
        toCreate.setDescription("Test description");

        Task saved = createSampleTask(101L);
        saved.setTitle("New Task");
        saved.setDescription("Test description");

        when(taskService.createTaskForUser(any(Task.class), eq(user))).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(101)))
                .andExpect(jsonPath("$.title", is("New Task")));

        verify(taskService).createTaskForUser(any(Task.class), eq(user));
    }

    // --- UPDATE ---
    @Test
    @DisplayName("PUT /api/tasks/{id} - should update task for user")
    void shouldUpdateTaskForUser() throws Exception {
        mockAuthenticatedUser("demoUser");

        Task update = new Task();
        update.setTitle("Updated Task");
        update.setDescription("Updated Desc");
        update.setCompleted(true);

        Task updated = createSampleTask(5L);
        updated.setTitle("Updated Task");
        updated.setDescription("Updated Desc");
        updated.setCompleted(true);

        when(taskService.updateTaskForUser(eq(5L), any(Task.class), eq("demoUser"))).thenReturn(updated);

        mockMvc.perform(put("/api/tasks/{id}", 5L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(5)))
                .andExpect(jsonPath("$.title", is("Updated Task")))
                .andExpect(jsonPath("$.completed", is(true)));

        verify(taskService).updateTaskForUser(eq(5L), any(Task.class), eq("demoUser"));
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - should return 404 when task not found")
    void shouldReturn404OnUpdateWhenMissing() throws Exception {
        mockAuthenticatedUser("demoUser");

        Task update = new Task();
        update.setTitle("Ghost Task");

        when(taskService.updateTaskForUser(eq(999L), any(Task.class), eq("demoUser")))
                .thenThrow(new RuntimeException("Task not found"));

        mockMvc.perform(put("/api/tasks/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Task not found")));
    }

    // --- DELETE ---
    @Test
    @DisplayName("DELETE /api/tasks/{id} - should delete task for user")
    void shouldDeleteTaskForUser() throws Exception {
        mockAuthenticatedUser("demoUser");
        doNothing().when(taskService).deleteTaskForUser(7L, "demoUser");

        mockMvc.perform(delete("/api/tasks/{id}", 7L))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTaskForUser(7L, "demoUser");
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - should handle missing task with 404")
    void shouldReturn404OnDeleteWhenMissing() throws Exception {
        mockAuthenticatedUser("demoUser");
        doThrow(new RuntimeException("Task not found"))
                .when(taskService).deleteTaskForUser(888L, "demoUser");

        mockMvc.perform(delete("/api/tasks/{id}", 888L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Task not found")));
    }
}
