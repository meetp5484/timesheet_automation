Feature: Login Feature

  Scenario: Successful login
    Given User is on login page
    And Click on Microsoft login button
    And Enter User name and password
    Then Click on Login Button
    And Verify User On Dashboard Page
