Feature: U14 As Kaia, I want to know the current and future weather at my gardens, so I know if I need to water them
  Background:
    Given I "Kaia" "Pene", 67 am a user with email "kaia@email.com" and password "TestPassword10!"
    And I as user "kaia@email.com" have a garden "Kaia's Other Garden" located in "Christchurch", "New Zealand"
    And I as user "kaia@email.com" am logged in with "TestPassword10!"
    Given I as user "kaia@email.com" is on my garden details page


  Scenario: AC1 - The current weather for my location is shown.
    Then Current weather for my location is shown

  Scenario: AC2 - The future weather for the future (3-5 days) is shown.
    Then Future weather for my location is shown

  Scenario: AC3 - Garden location cannot be found
    Then No weather is shown