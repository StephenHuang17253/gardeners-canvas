Feature: As Oliver, I want to be able to move my plant around my 2D garden grid, so I can model and plan the layout of my garden.

Background:
  Given I "Oliver" "Doe", 18 am a user with email "oliver@email.com" and password "TestPassword10!"
  And I as user "oliver@email.com" am logged in with "TestPassword10!"
  And User "oliver@email.com" has a garden "oliver's Garden" located in "Christchurch", "New Zealand"

  Scenario: AC1 - There is a palette window on the garden 2D page
    Given I as user "oliver@email.com" is on my two-D garden page for "oliver's Garden"
    Then I see a palette window containing all my plants with relevant information as well as a save garden and clear all button