Feature: U22 (Tag moderation) : As Kaia, I want to make sure that tags added to gardens do not contain any inappropriate words
  so that the sensibilities of other gardeners are not offended.

  Background:
    Given I "Dominic" "Gorny", 21 am a user with email "dom@email.com" and password "Password1!"
    And I as user "dom@email.com" have a garden



  Scenario: AC1.1 - Given I am adding a valid tag, when I confirm the tag, then the tag is checked for offensive and
  or inappropriate words.
    Given I as user "dom@email.com" am logged in with "Password1!"
    And I previously added a tag "Bad Language"
    And My Tag "Bad Language" contained profanity
    When I enter a valid tag "Bad Language"
    Then The tag is not added to the garden

  Scenario: AC1.2 - Given I am adding a valid tag, when I confirm the tag, then the tag is checked for offensive and
  or inappropriate words.
    Given I as user "dom@email.com" am logged in with "Password1!"
    And I previously added a tag "Good Language"
    And My Tag "Good Language" did not contain profanity
    When I enter a valid tag "Good Language"
    Then the tag is "Good Language" added to my garden

  Scenario: AC2 - Given the submitted tag is evaluated for appropriateness, when it is flagged as inappropriate, then an
  error message tells me that the submitted word is not appropriate and the tag is not added to the list of user-defined tags.
    Given I as user "dom@email.com" am logged in with "Password1!"
    And I previously added a tag "Bad Language 2"
    And My Tag "Bad Language 2" contained profanity
    When I enter a valid tag "Bad Language 2"
    Then The following error message is displayed "This tag does not meet the language standards for Gardener's Grove. A warning strike has been added to your account"
    And The tag is not added to the garden

  Scenario: AC5 - Given the evaluation of a user-defined tag was delayed, when the tag has been evaluated as
  inappropriate, then the tag is removed from the garden it was assigned to, and it is not added to the list of
  user-defined tags, and the user’s count of inappropriate tags is increased by 1.
    Given I as user "dom@email.com" am logged in with "Password1!"
    And I previously added a tag "Profane Tag"
    And My Tag "Profane Tag" contained profanity
    And I as user "dom@email.com" currently have 0 strikes
    When I enter a valid tag "Profane Tag"
    Then The following error message is displayed "This tag does not meet the language standards for Gardener's Grove. A warning strike has been added to your account"
    And The tag is not added to the garden
    And I "dom@email.com" get a strike