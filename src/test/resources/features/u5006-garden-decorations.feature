Feature: As Liam, I want to be able to add decorations such as fountains and gnomes to my gardens.

  Background:
    Given I "Liam" "Doe", 18 am a user with email "liam@email.com" and password "TestPassword10!"
    And I as user "liam@email.com" am logged in with "TestPassword10!"
    And User "liam@email.com" has a garden "liam's Garden" located in "Christchurch", "New Zealand"

    Scenario: AC1 - There are decorations on the palette window
      Given I as user "liam@email.com" is on my two-D garden page for "liam's Garden"
      Then As user "liam@email.com" I see a palette window with a tab labelled for decorations

    Scenario: AC2 - I can place decorations and see them
      Given I as user "liam@email.com" is on my two-D garden page for "liam's Garden"
      When I As user "liam@email.com" place a decoration on my grid and press save
      Then I see my placed decoration

  Scenario: AC3 - Given I have placed a decoration and saved it, when I view the garden in 3D, then I can see it in the garden.
    Given There is a garden decoration "fountain" saved in my "liam@email.com" garden "liam's Garden"
    When I "liam@email.com" open the 3D view of my garden "liam's Garden"
    Then I can see the garden decoration "fountain" in my "liam@email.com" garden "liam's Garden"