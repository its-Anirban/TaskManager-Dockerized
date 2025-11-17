Feature: Task creation

  Scenario: Create a new task for logged in user
    Given I am logged in as "testuser" with password "password123"
    When I create a task with title "Write tests" and description "Write integration tests"
    Then the response status should be 201
    And the response should contain a numeric "id"
