Feature: U16 As Sarah, I want to be able to change my password over email, so that I can still access my account even if I forget my password.

  Background:
    Given "Sarah" "Thompson", 28 is a user with email "sarah@email.com" and password "TestPassword10!"

  Scenario: AC1 - I can access the lost password form
    Given I am on the login page
    When I hit the “Forgot your password?” link
    Then I see a form asking me for my email address

  Scenario Outline: AC2 - I submit an empty or malformed email address
    Given I am on the lost password form
    When I enter an empty or malformed email address <weakEmail>
    And I click the submit button
    Then an error message tells me "Email must be in the form 'jane@doe.nz'"
    Examples:
      | weakEmail            |
      | "bademail.com"       |
      | "@gmail.com"         |
      | "hello@gmail"        |
      | "123456789"          |
      | "!@#$%^&*()"         |
      | ""                   |



  Scenario: AC3 - I submit a valid email not known to the system
    Given I am on the lost password form
    When I enter a valid email "john@gmail.com" that is not known to the system
    And I click the submit button
    Then a confirmation message tells me "An email was sent to the address if it was recognised"

  Scenario: AC4 - I submit an email known to the system
    Given I am on the lost password form
    When I enter an email "sarah@email.com" that is known to the system
    And I click the submit button
    Then a confirmation message tells me "An email was sent to the address if it was recognised"
    And an email is sent with a link containing a unique reset token

  Scenario: AC5 - I reset password using the received email link
    Given I go to the received email in the email "sarah@email.com"
    When I click the clickable link in the email
    Then I am taken to the reset password form

  Scenario Outline: AC6 - I enter mismatching passwords on the reset form
    Given I as user "sarah@email.com" with password "TestPassword10!" am on the reset password form
    When I enter two different passwords <firstPassword>, <secondPassword>
    Then My password does not get updated
    Examples:
      | firstPassword  | secondPassword |
      | "Password1#"   | "Password2#"   |
      | "nomatcH123!"  | "nOmatch123!"  |
      | "PASSWORd1!"   | "PASSWOrD1!"   |

  Scenario Outline: AC7 - I enter a weak password
    Given I as user "sarah@email.com" am logged in with "TestPassword10!"
    And I as user "sarah@email.com" with password "TestPassword10!" am on the reset password form
    When I enter a weak password <weakPassword>
    Then My password does not get updated
    Examples:
      | weakPassword           |
      | " "                    |
      | "a"                    |
      | "password"             |
      | "123456789"            |
      | "Password1"            |
      | "PASSWORD1"            |
      | "Password!"            |
      | "password1!"           |

  Scenario: AC8 - I enter a valid password
    Given I as user "sarah@email.com" with password "TestPassword10!" am on the reset password form
    When I enter "Password1#" in both new and retype fields and hit the save button
    Then my password is updated
    And I am redirected to the login page






