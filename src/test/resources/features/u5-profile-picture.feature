Feature: Add a profile picture to user
  Background:
    Given I am logged in and on the profile page

  Scenario: AC3
    Given I choose a valid profile picture
    When I submit my profile picture
    Then My profile picture is updated

  Scenario: AC4
    Given I choose a non png nor jpg nor svg profile picture
    When I submit my profile picture
    Then My profile picture is not updated

  Scenario: AC5
    Given I choose a profile picture larger than 10MB
    When I submit my profile picture
    Then My profile picture is not updated

