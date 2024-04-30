Feature: U4 As Sarah, I want to edit my user profile so that I can keep my details accurate.
  Background:
    Given Given i am editing a user profile

  Scenario Outline: AC3 - Given I am on the edit profile form
    When I enter valid values for first name <fname>, last name <lname>, email address <email>, and date of birth <date>
    And I click the "Submit" button
    Then I will be a user with first name <fname>, last name <lname>, email address <email>, and date of birth <date>
    Examples:
    | fname | lname  | email           | date       |
    |"Alice"|"Anders"|"alice@email.com"|"12/04/2001"|

  Scenario: AC4 - I click the check box marked “I have no surname” ticked
    When I check the check box marked "I have no surname"
    And I click the "Submit" button
    Then My surname will become ""

  Scenario Outline: AC5 - I enter invalid values for either first or last name
    When I enter invalid values for my first name <fname> and last name <lname>
    And I click the "Submit" button
    Then No details are changed
    Examples:
      | fname | lname   |
      | "##"  | "Doe"   |
      | "Jane"| "$$#doe"|
      | "123" | "456"   |
      | ""    | "Doe"   |
      | "Jane"| ""      |


  Scenario Outline: AC6 - I enter a first or last name that is more than 64 characters.
    When I enter invalid values for my first name <fname> and last name <lname>
    And I click the "Submit" button
    Then No details are changed
    Examples:
      |fname  | lname |
      |"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" | "Doe" |
      | "Jane"                                                | "abcdefghijklmnopqrstuvwxyzzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"|


  Scenario Outline: AC7 - I enter an empty or malformed email address
    When I enter invalid value for my email <email>
    And I click the "Submit" button
    Then No details are changed
    Examples:
      | email|
      |" "   |
      |"user_123gmail.co.nz"|
      |"john.doe@h."        |
      |"test@test.c"        |
      |"test@.com"          |
      |"@test.com"          |
      |"abc-@mail.com"      |
      |"abc..def@mail.com"  |
      |".abc@mail.com"      |
      |"abc.def@mail#archive.com"|
      |"abc.def@mail..com"       |


  Scenario: AC8 I enter an email address associated to an account that already exists
    Given There exists an old user with email "example@email.com"
    When I enter invalid value for my email "example@email.com"
    And I click the "Submit" button
    Then No details are changed


  Scenario: AC10 - I enter a date of birth for someone younger than 13 years old
    When I enter an invalid value for date of birth "01/02/2023"
    And I click the "Submit" button
    Then No details are changed


  Scenario: AC11 - I enter a date of birth for someone older than 120
    When I enter an invalid value for date of birth "01/01/1903"
    And I click the "Submit" button
    Then No details are changed

