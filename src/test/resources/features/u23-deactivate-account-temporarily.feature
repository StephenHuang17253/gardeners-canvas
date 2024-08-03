Feature: U23 (Deactivate account temporarily) : As Kaia, I want to make sure that users who repeatedly trying to add inappropriate tags
  are prevented to use the app for one week so that they can reflect on their behaviour.

  Background:
    Given I "Daemon" "targaryen", 41 am a user with email "daemon@email.com" and password "Password1!"
    And I as user "daemon@email.com" have a garden



  Scenario: AC1 - Given I have added four inappropriate tags, when I add a fifth inappropriate tag, then a message
  tells me that I have added an inappropriate tag for the fifth time, and I receive an email warning me that if
  I add another inappropriate tag, my account will be blocked for one week.
    Given I as user "daemon@email.com" am logged in with "Password1!"
    And I previously added a tag "Caraxes"
    And My Tag "Caraxes" contained profanity
    And I as user "daemon@email.com" currently have 4 strikes
    When I enter a valid tag "Caraxes"
    Then The following error message is displayed "You have added an inappropriate tag for the fifth time. You have been sent a warning email. If you add another inappropriate tag, you will be banned for a week."
    And The tag is not added to the garden
    And I "daemon@email.com" get a strike

