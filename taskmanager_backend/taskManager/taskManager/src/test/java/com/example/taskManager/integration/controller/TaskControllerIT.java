package com.example.taskManager.integration.controller;

import com.example.taskManager.model.Task;
import com.example.taskManager.model.User;
import com.example.taskManager.repository.TaskRepository;
import com.example.taskManager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class TaskControllerIT {

    @Autowired private MockMvc mockMvc;
    @Autowired private TaskRepository taskRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;

    private User demoUser;
    private Task demoTask;

    @BeforeEach
    void setup() {
        // Clean up and initialize base entities
        taskRepository.deleteAll();
        userRepository.deleteAll();

        // Create demo user for relational integrity
        demoUser = new User("demoUser", "demoPass");
        demoUser = userRepository.save(demoUser);

        // Create a sample task assigned to demoUser
        demoTask = new Task();
        demoTask.setTitle("Integration Test Task");
        demoTask.setDescription("This is an integration test task");
        demoTask.setUser(demoUser);
        demoTask = taskRepository.save(demoTask);
    }

    @Test
    void shouldGetAllTasks() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].title").value("Integration Test Task"));
    }

    @Test
    void shouldGetTaskByIdSuccessfully() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", demoTask.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(demoTask.getId().intValue()))
                .andExpect(jsonPath("$.title").value("Integration Test Task"));
    }

    @Test
    void shouldReturnNotFoundForNonExistingTask() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", 99999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", notNullValue()))
                .andExpect(jsonPath("$.timestamp", notNullValue()));
    }

    @Test
    void shouldCreateNewTask() throws Exception {
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("Spring Boot integration test");
        newTask.setUser(demoUser); // Maintain foreign key integrity

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void shouldRejectMalformedJsonWithBadRequest() throws Exception {
        String badJson = "{\"title\":\"Missing quote}";
        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUpdateExistingTask() throws Exception {
        demoTask.setTitle("Updated Task Title");

        mockMvc.perform(put("/api/tasks/{id}", demoTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(demoTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task Title"));
    }

    @Test
    void shouldRejectMalformedJsonOnUpdate() throws Exception {
        String badJson = "{\"title\":\"Broken JSON}";
        mockMvc.perform(put("/api/tasks/{id}", demoTask.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnNotFoundWhenUpdatingNonExistingTask() throws Exception {
        Task tempTask = new Task();
        tempTask.setTitle("Temp Task");
        tempTask.setDescription("Temporary description");
        tempTask.setUser(demoUser);

        mockMvc.perform(put("/api/tasks/{id}", 88888L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(tempTask)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    void shouldDeleteTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", demoTask.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturnNotFoundWhenDeletingNonExistingTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/{id}", 77777L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", notNullValue()));
    }
}
