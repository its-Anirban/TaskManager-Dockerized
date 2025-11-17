package com.example.taskManager.integration.exception;

import org.junit.jupiter.api.Test;

import com.example.taskManager.exception.TaskOperationException;

import static org.junit.jupiter.api.Assertions.*;

class TaskOperationExceptionTest {

    @Test
    void shouldStoreMessageCorrectly() {
        String message = "Task operation failed";
        TaskOperationException ex = new TaskOperationException(message);

        assertEquals(message, ex.getMessage());
    }
}
