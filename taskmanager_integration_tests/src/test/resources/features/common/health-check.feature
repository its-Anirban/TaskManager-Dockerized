Feature: Health check

  Scenario: Backend health endpoint responds
    Given the authentication service is available
    When I call GET "/actuator/health"
    Then the response status should be 200
