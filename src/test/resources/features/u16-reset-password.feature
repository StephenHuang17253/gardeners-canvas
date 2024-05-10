Feature: U16 As Sarah, I want to be able to change my password over email, so that I can still access my account even if I forget my password.

  Scenario AC1 - I can access the lost password form
    Given I am on the login page
    When I hit the “Forgot your password?” link
    Then I see a form asking me for my email address

  Scenario AC2 - I submit an empty or malformed email address
    Given I am on the lost password form
    When I enter an empty or malformed email address and click "Submit"
    Then an error message tells me "Email address must be in the form 'jane@doe.nz'"

  Scenario AC3 - I submit a valid email not known to the system
    Given I am on the lost password form
    When I enter a valid email that is not known to the system and click "Submit"
    Then a confirmation message tells me "An email was sent to the address if it was recognised"

  Scenario AC4 - I submit an email known to the system
    Given I am on the lost password form
    When I enter an email that is known to the system and click "Submit"
    Then a confirmation message tells me "An email was sent to the address if it was recognised"
    And an email is sent to the email address with a link containing a unique reset token

  Scenario AC5 - I reset password using the received email link
    Given I received an email to reset my password
    When I go to the given URL passed in the email
    Then I am taken to the reset password form

  Scenario AC6 - I enter mismatching passwords on the reset form
    Given I am on the reset password form
    When I enter two different passwords in "new" and "retype password" fields and hit the save button
    Then The password does not get updated

  Scenario AC7 - I enter a weak password
    Given I am on the reset password form
    When I enter a weak password and hit the save button
    Then The password does not get updated

  Scenario: AC8 - I enter a valid password
    Given I am on the reset password form
    When I enter fully compliant details and hit the save button
    Then my password is updated
    And I am redirected to the login page






