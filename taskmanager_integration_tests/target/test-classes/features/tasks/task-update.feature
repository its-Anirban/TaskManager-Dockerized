Feature: Task update

  Scenario: Update an existing task
    Given the authentication service is available
    When I register a user with username "random" and password "password123"
    And I am logged in as "context" with password "context"
    And I have a task with title "Write tests"
    When I update that task to set completed true
    Then the response status should be 200
    And the response should contain "completed": true
