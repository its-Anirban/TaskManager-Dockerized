Feature: Task fetching

  Scenario: Fetch tasks for logged-in user
    Given the authentication service is available
    When I register a user with username "random" and password "password123"
    And I am logged in as "context" with password "context"
    When I request all tasks
    Then the response status should be 200
    And the response should be a list
