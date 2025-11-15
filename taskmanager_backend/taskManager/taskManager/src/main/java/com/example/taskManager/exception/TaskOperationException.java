package com.example.taskManager.exception;

public class TaskOperationException extends RuntimeException {
    public TaskOperationException(String message) {
        super(message);
    }
}
