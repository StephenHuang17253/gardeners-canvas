Feature: U1 As Sarah, I want to be able to change my password, so that I can keep my account secured with a new password in case my password gets leaked

  Scenario: AC1
    Given I am on the edit profile form,
    When I hit the change password button,
    Then a dedicated form is shown with three text fields: “old password”, “new password”, and “retype password”

  Scenario: AC2
    Given I am on the change password form,
    When I enter an old password that does not match the password in file,
    Then an error message tells me “Your old password is incorrect

  Scenario: AC3
    Given I am on the change password form,
    And I enter two different passwords in “new”
    And “retype password” fields, when I hit the save button,
    Then an error message tells me “The new passwords do not match”.

  Scenario: AC4
    Given I am on the change password form
    And I enter a weak password (e.g., contains any other fields from the user profile form, is below 8 char long, does not contain a variation of different types of characters with one lowercase letter, one uppercase letter, one digit, one special character),
    When I hit the save button
    Then an error message tells “Your password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character.”

  Scenario: AC5
    Given I am on the change password form,
    When I enter fully compliant details
    And I click the “Submit” button
    Then my password is updated, and an email is sent to my email address to confirm that my password was updated

  Scenario: AC6
    Given I am on the change password form
    When I hit the cancel button
    Then I am sent back to my view details page, and no changes have been made to my password.