Feature: U14 As Kaia, I want to know the current and future weather at my gardens, so I know if I need to water them
  Background:
    Given "Kaia" "Pene", 67 is a user with email "kaia@email.com" and password "TestPassword10!"
    And User "kaia@email.com" has a garden "Kaia's Other Garden" located in "Christchurch", "New Zealand"
    And I as user "kaia@email.com" am logged in with "TestPassword10!"



  Scenario: AC1 - The current weather for my location is shown.
    Given My garden is not set to a location that the location service can find
    And I as user "kaia@email.com" is on my garden details page
    Then Current weather for my location is shown

  Scenario: AC2 - The future weather for the future (3-5 days) is shown.
    Given My garden is not set to a location that the location service can find
    And I as user "kaia@email.com" is on my garden details page
    Then Future weather for my location is shown

  Scenario: AC3 -  Given the garden has a location that can’t be found, then an error message tells me “Location not found, please update your location to see the weather”.
    Given My garden is not set to a location that the location service can not find
    And I as user "kaia@email.com" is on my garden details page
    Then A Weather error message tells me “Location not found, please update your location to see the weather”