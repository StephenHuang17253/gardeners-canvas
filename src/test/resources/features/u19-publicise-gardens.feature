Feature: U19 As Inaya, I want to be able to make my garden public so that others can see what I’m growing.

  Background:
    Given "Kaia" "Pene", 67 is a user with email "kaia@email.com" and password "TestPassword10!"
    And User "kaia@email.com" has a garden "Kaia's Public Garden" located in "Christchurch", "New Zealand"
    And I as user "kaia@email.com" am logged in with "TestPassword10!"
    Given User "kaia@email.com" is on my garden details page for "Kaia's Public Garden"

  Scenario: AC1 - When I mark my garden as public, my garden is visible in search results
    When I mark a checkbox labelled "Make my garden public"
    Then My garden will be visible in search results

Scenario: AC2 - When I add an optional description of the garden, the description is persisted
  When I add an optional description
  And I am creating a new garden "Kaia's Described Garden"
  Then the new description is persisted


Scenario: AC3 - When I remove the description of the garden, the description is deleted
  When I remove the description of the garden
  And I am creating a new garden "Kaia's Blank Garden"
  Then the new description is deleted

Scenario: AC4 - When I am editing a garden and add an optional description, the description is persisted
  When I add an optional description
  And I am editing an existing garden
  Then the edited description is persisted


Scenario: AC5 - When editing a garden and delete the description of the garden, the description is deleted
  When I remove the description of the garden
  And I am editing an existing garden
  Then the edited description is deleted

Scenario Outline: AC6 - When I enter an invalid description, an error message appears
  When I enter an invalid description <description>
  And I am creating a new garden "Kaia's Invalid Garden"
  Then an error message appears
  Examples:
  | description|
  | '1234%#!#' |





