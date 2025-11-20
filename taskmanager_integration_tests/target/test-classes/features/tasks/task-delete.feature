Feature: Task deletion

  Scenario: Delete an existing task
    Given the authentication service is available
    When I register a user with username "random" and password "password123"
    And I am logged in as "context" with password "context"
    And I have a task with title "Write tests to delete"
    When I delete that task
    Then the response status should be 204
