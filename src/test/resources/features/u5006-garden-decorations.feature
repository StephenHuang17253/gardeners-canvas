Feature: As Liam, I want to be able to add decorations such as fountains and gnomes to my gardens.

  Background:
    Given I "Oliver" "Doe", 18 am a user with email "oliver@email.com" and password "TestPassword10!"
    And I as user "oliver@email.com" am logged in with "TestPassword10!"
    And User "oliver@email.com" has a garden "oliver's Garden" located in "Christchurch", "New Zealand"

  Scenario: AC3 - Given I have placed a decoration and saved it, when I view the garden in 3D, then I can see it in the garden.
    Given There is a garden decoration "fountain" saved in my garden "oliver's Garden"
    When I open the 3D view
    Then I can see the garden decoration "fountain"