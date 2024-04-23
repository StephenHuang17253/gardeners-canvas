Feature: U10 Edit garden details

Background:
  Given There exists a garden "Test Garden"
  And I am on the garden edit form


  Scenario: AC2 - valid values
    When  I enter valid values for the "name", "Location", and "Size"
    And I click the edit plant form Submit button
    Then I am taken back to the Garden Page
    And The garden details have been updated

  Scenario: AC3
    Given I enter valid values for the "name", "Location", and "Size"
    When I enter an invalid name value for the "name"
    And I click the edit plant form Submit button
    Then The garden details are not updated

  Scenario: AC4
    Given I enter valid values for the "name", "Location", and "Size"
    When I enter an invalid size value for the "location"
    And I click the edit plant form Submit button
    Then The garden details are not updated

  Scenario: AC5
    Given I enter valid values for the "name", "Location", and "Size"
    When I enter an invalid location value for the "size"
    And I click the edit plant form Submit button
    Then The garden details are not updated

  Scenario: AC6
    Given I enter valid values for the "name", "Location", and "Size"
    When I enter a size using a comma
    And I click the edit plant form Submit button
    Then The garden details have been updated