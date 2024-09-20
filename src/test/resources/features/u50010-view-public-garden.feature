Feature: As Oliver, I want to be able to view other peoples public gardens in 3D, to get inspiration for my own gardens.

Background:
  Given I "Oliver" "Doe", 18 am a user with email "oliver@email.com" and password "TestPassword10!"
  And I as user "oliver@email.com" am logged in with "TestPassword10!"
  And I "oliver@email.com" am friends with "Jeff" "Stewart", 44, a user with email "jeffstewart@email.com" and password "TestPassword10!"
  And User "jeffstewart@email.com" has a garden "jeff's Garden" located in "Christchurch", "New Zealand" 

  Scenario: AC1 - Given I am browsing public gardens, when I am viewing a public garden, and it has a 3D view, then there is a button to navigate to the 3D view of the garden.
    Given I try to navigate to "jeffstewart@email.com"'s 3d garden "jeff's Garden"
    Then I am able to view the 3D garden