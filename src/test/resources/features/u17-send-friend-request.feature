Feature: U17 (Send friend request): As Liam, I want to connect with my friends on Gardener’s Grove
  so that we can build a community on the app.

  Background:
    Given I "Liam" "Müller", 47 am a user with email "liam@email.com" and password "TestPassword10!"
    And I "Kaia" "Pene", 67 am a user with email "kaia@email.com" and password "TestPassword10!"

  Scenario: AC1 - nav bar takes me to manage friends page
    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    When I click on the 'Manage Friends' button
    Then I am shown a 'Manage Friends' page