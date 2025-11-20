Feature: Task creation

  Scenario: Create a new task for logged in user
    Given the authentication service is available
    When I register a user with username "random" and password "password123"
    And I am logged in as "context" with password "context"
    When I create a task with title "Write tests" and description "Write integration tests"
    Then the response status should be 201
    And the response should contain a numeric "id"
