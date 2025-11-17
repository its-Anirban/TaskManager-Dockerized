Feature: Task fetching

  Scenario: Fetch tasks for logged-in user
    Given I am logged in as "testuser" with password "password123"
    When I request all tasks
    Then the response status should be 200
    And the response should be a list
