Feature: U7 As Sarah, I want to be able to change my password, so that I can keep my account secured with a new password in case my password gets leaked
  Background:
    Given "Sarah" "Thompson", 36 is a user with email "sarah@email.com" and password "TestPassword10!"
    And I as user "sarah@email.com" am logged in with "TestPassword10!"
    Given I as user "sarah@email.com" with password "TestPassword10!" am on the change password page

  Scenario: AC2 - Entering passwords that don't match the old password
    When I enter an old password "NewPassword10!" that does not match the current password
    Then The password does not get updated

  Scenario: AC3 - I enter two different password that don't match
    When I enter two different new passwords: "myNewPassword10!" and "AnotherNewPassword10!"
    Then The password does not get updated

  Scenario Outline: AC4 - I enter a weak password e.g. under 8 chars, No variation in char
    When I enter the weak password: <weakPassword>
    Then The password does not get updated
    Examples:
      | weakPassword |
      | "noEight"              |
      |     "a"                |
      |     "noNumbersPass!"   |
      | "noSpecialLetters10"   |
      | "allllowercase"        |
      | "ALLUPPERCASE"         |
      | "10!"                  |
      | ""                     |

  Scenario: AC5 - I enter a fully compliant and then my password is updated
    When I enter fully compliant password: "EpicNewPassword10!"
    Then The password is updated
