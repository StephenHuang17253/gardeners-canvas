Feature: U17 (Send friend request): As Liam, I want to connect with my friends on Gardener’s Grove
  so that we can build a community on the app.

  Background:
    Given I "Liam" "Müller", 47 am a user with email "liam@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Kaia" "Pene", 67, a user with email "kaia@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Inaya" "Singh", 24, a user with email "inaya@email.com" and password "TestPassword10!"


  Scenario: AC1 - nav bar takes me to manage friends page
    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    When I click on the 'Manage Friends' button
    Then I am shown a 'Manage Friends' page

  Scenario: AC2 - Given I am on the manage friends page, then I see the list of my friends with their names, their
  profile pictures, and a link to their gardens list including private and public gardens.

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    When I am on the 'Manage Friends' page
    Then I see a list of my friends with their names, profile pictures, and link to their gardens list