Feature: As Liam, I want to be able to add decorations such as fountains and gnomes to my gardens.

  Background:
    Given I "Liam" "Doe", 18 am a user with email "liam@email.com" and password "TestPassword10!"
    And I as user "liam@email.com" am logged in with "TestPassword10!"
    And User "liam@email.com" has a garden "liam's Garden" located in "Christchurch", "New Zealand"

    Scenario: AC1 - There are decorations on the palette window
      Given I as user "liam@email.com" is on my two-D garden page for "liam's Garden"
      Then As user "liam@email.com" I see a palette window with a tab labelled for decorations