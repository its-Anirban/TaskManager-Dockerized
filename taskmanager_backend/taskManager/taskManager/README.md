# Task Manager

Task Manager is a Spring Boot application that exposes a RESTful API for creating, updating, listing, and deleting tasks. It uses Spring Data JPA with an in-memory H2 database by default and includes unit tests for the service and controller layers.

## Getting Started

1. Ensure you have Java 17+ and Maven installed, or use the provided Maven wrapper.
2. Clone the repository and navigate into the project directory `taskManager/taskManager`.
3. Run the application locally:

```bash
./mvnw spring-boot:run
```

The service starts on `http://localhost:8080`.

## Running Tests

Execute the test suite with Maven:

```bash
./mvnw test
```

## Code Coverage

Jacoco is configured to generate coverage reports during the `verify` phase:

```bash
./mvnw verify
```

After the command completes, open `target/site/jacoco/index.html` in your browser to explore the coverage report.

## Common Maven Commands

- `./mvnw clean`: Remove build artifacts.
- `./mvnw package`: Build the executable JAR.
- `./mvnw spring-boot:run`: Start the application with the Maven plugin.

## API Overview

| Method | Endpoint        | Description          |
|--------|-----------------|----------------------|
| GET    | `/tasks`        | Retrieve all tasks   |
| POST   | `/tasks`        | Create a new task    |
| PUT    | `/tasks/{id}`   | Update an existing task |
| DELETE | `/tasks/{id}`   | Delete a task        |

Each endpoint accepts/returns JSON payloads and uses validation annotations to enforce required fields.
