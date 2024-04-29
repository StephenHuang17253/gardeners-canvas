Feature: U1 As Sarah, I want to register on Gardener’s Grove so that I can use its awesome features.
  Background: Given i am on the registration page

  Scenario: AC2 - I enter valid values for the registration form
    When I enter valid values for my first name "Jane" and last name "Doe", email address "janedoe@email.com", password "TestPassword10!", repeat password "TestPassword10!" and optionally date of birth "10/10/2001"
    And I click the "Sign Up" button
    Then A new user is added to database

  Scenario: AC3 - I click the check box marked "I have no surname"
    When I click the check box marked "I have no surname"
    And I enter valid values for my first name "James" and last name "", email address "james12@email.com", password "TestPassword10!", repeat password "TestPassword10!" and optionally date of birth "10/10/2001"
    And I click the "Sign Up" button
    Then A new user is added to database

  Scenario Outline: AC4 - I enter invalid values for either first or last name
    When I enter invalid values for my first name <fname> and last name <lname>
    And I click the "Sign Up" button
    Then No account is created
    Examples:
    | fname | lname   |
    | "##"  | "Doe"   |
    | "Jane"| "$$#doe"|
    | "123" | "456"   |
    | ""    | "Doe"   |
    | "Jane"| ""      |


  Scenario Outline: AC5 - I enter a first or last name that is more than 64 characters.
    When I enter invalid values for my first name <fname> and last name <lname>
    And I click the "Sign Up" button
    Then No account is created
    Examples:
    |fname  | lname |
    |"abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" | "Doe" |
    | "Jane"                                                | "abcdefghijklmnopqrstuvwxyzzABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"|


  Scenario Outline: AC6 - I enter an empty or malformed email address
    When I enter invalid value for my email <email>
    And I click the "Sign Up" button
    Then No account is created
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


  Scenario: AC7 I enter an email address associated to an account that already exists
    Given There exists a user with email "admin@email.com"
    When I enter invalid value for my email "admin@email.com"
    And I click the "Sign Up" button
    Then No account is created


  Scenario: AC9 - I enter a date of birth for someone younger than 13 years old
    When I enter an invalid value for date of birth "01/02/2023"
    And I click the "Sign Up" button
    Then No account is created


  Scenario: AC10 - I enter a date of birth for someone older than 120
    When I enter an invalid value for date of birth "01/01/1903"
    And I click the "Sign Up" button
    Then No account is created

  Scenario Outline: AC11 - I enter two different passwords
    When I enter invalid passwords for password <password> and repeat password <repeatPassword>
    And I click the "Sign Up" button
    Then No account is created
    Examples:
    | password     | repeatPassword |
    |"TestPass10!" | "TestPass200!"  |
    |"TestPass200!"| "TestPass10! "  |

    Scenario Outline: AC12 Given I am on the registration form, and I enter a weak password (i.e. is less than 8 characters,
    does not contain at least one lower case letter, one upper case letter, one number, and one special
    character), when I click the “Sign Up” button, then an error message tells me “Your password must be
    at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number,
    and one special character.”
      When I enter invalid passwords for password <password> and repeat password <password>
      And I click the "Sign Up" button
      Then No account is created
      Examples:
      | password |
      | ""       |
      |"aaa"     |
      |"aaaaaaaa"|
      |"000!0000"|
      |"password1!"|
      |"Password123"|
      |"Password!@#"|
      |"PASSWORD1!" |

