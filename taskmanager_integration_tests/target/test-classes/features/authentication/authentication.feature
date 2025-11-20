Feature: Authentication API

  Scenario: Register a new user successfully
    Given the authentication service is available
    When I register a user with username "random" and password "password123"
    Then the response status should be 201
    And the response should contain "username"

  Scenario: Login with valid credentials
    Given the authentication service is available
    When I register a user with username "random" and password "password123"
    And I login with username "context" and password "context"
    Then the response status should be 200
    And the response should contain a "token"
