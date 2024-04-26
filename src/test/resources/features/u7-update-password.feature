Feature: U7 As Sarah, I want to be able to change my password, so that I can keep my account secured with a new password in case my password gets leaked

  Background:
    Given There exists a user with email "admin@email.com"

  Scenario: AC2 - Entering passwords that don't match the old password
    When I enter an old password <oldPassword> that does not match the password <passwordInFile> in file
    And I click the “Submit” button
    Then The password does not get updated


  Scenario Outline: AC3 - I enter two different password that don't match
    When I enter two different new passwords: <newPassword> and <retypePassword>
    Then The password does not get updated
    Examples:
      | newPassword | retypePassword |
      |       a      |         a       |

  Scenario Outline: AC4 - I enter a weak password e.g. under 8 chars, No variation in char
    When I enter the weak password: <weakPassword>
    And I click the “Submit” button
    Then The password does not get updated
    Examples:
      | weakPassword |

  Scenario: AC5
    Given I am on the change password form
    When I enter fully compliant details
    And I click the “Submit” button
    Then The password is updated
