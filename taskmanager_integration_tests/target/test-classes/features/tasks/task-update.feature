Feature: Task update

  Scenario: Update an existing task
    Given I am logged in as "testuser" with password "password123"
    And I have a task with title "Write tests"
    When I update that task to set completed true
    Then the response status should be 200
    And the response should contain "completed": true
