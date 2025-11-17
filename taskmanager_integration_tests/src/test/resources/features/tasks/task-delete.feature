Feature: Task deletion

  Scenario: Delete an existing task
    Given I am logged in as "testuser" with password "password123"
    And I have a task with title "Write tests to delete"
    When I delete that task
    Then the response status should be 204
