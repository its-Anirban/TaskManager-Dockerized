# TaskManager Integration Tests
This module contains Cucumber + RestAssured based integration tests for TaskManager backend.
Tests target: http://localhost:8080 by default (see src/test/resources/config/application-dev.yml)

Structure:
- src/test/java/com/example/taskManager/integration/config
- src/test/java/com/example/taskManager/integration/stepdefinitions
- src/test/java/com/example/taskManager/integration/runners
- src/test/java/com/example/taskManager/integration/support
- src/test/resources/features

Run locally:
1. Start TaskManager backend on http://localhost:8080
2. From this module: `mvn test`
