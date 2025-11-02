package com.example.taskManager.unit;

import com.example.taskManager.TaskManagerApplication;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskManagerApplicationUnitTest {

    @Test
    void mainMethodShouldInvokeStartSuccessfully() {
        // Mock SpringApplication.run()
        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            ConfigurableApplicationContext mockContext = mock(ConfigurableApplicationContext.class);
            mocked.when(() -> SpringApplication.run(TaskManagerApplication.class, new String[]{}))
                  .thenReturn(mockContext);

            // Act & Assert
            assertDoesNotThrow(() -> TaskManagerApplication.main(new String[]{}));

            // Verify correct static call
            mocked.verify(() -> SpringApplication.run(TaskManagerApplication.class, new String[]{}));
        }
    }

    @Test
    void startMethodShouldReturnNonNullContext() {
        ConfigurableApplicationContext fakeContext = mock(ConfigurableApplicationContext.class);

        try (MockedStatic<SpringApplication> mocked = mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(TaskManagerApplication.class, new String[]{}))
                  .thenReturn(fakeContext);

            // Act
            ConfigurableApplicationContext result = TaskManagerApplication.start(new String[]{});

            // Assert
            assertNotNull(result, "Context should not be null");
            assertSame(fakeContext, result, "Returned context should match mocked instance");

            // Verify
            mocked.verify(() -> SpringApplication.run(TaskManagerApplication.class, new String[]{}));
        }
    }
}
