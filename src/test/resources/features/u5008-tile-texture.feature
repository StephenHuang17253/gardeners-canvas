Feature: As Oliver, I want to better texture my tiles to match my real-life garden.

Background:
  Given I "Alexander" "Doe", 18 am a user with email "Alexander@email.com" and password "TestPassword10!"
  And I as user "Alexander@email.com" am logged in with "TestPassword10!"
  And User "Alexander@email.com" has a garden "Alexander's Garden" located in "Christchurch", "New Zealand" 

  Scenario Outline: AC1 Given I am viewing my garden in 2D, when I click the button to edit the ground texture, then I can change the texture of a tile of my garden to be grass, soil, concrete, or bark, with grass being the default.
    Given I as user "Alexander@email.com" am on my 2D garden page for "Alexander's Garden"
    When I select a texture <texture> and place it at <x>, <y> and click save
    Then my new texture <texture>, is persisted at <x>, <y>
    Examples:
      |texture|x|y|
      | "Grass"|1|1|
      | "Soil" |2|2|
      | "Bark" |3|3|
      | "Concrete"|4|4|
      | "Stone Path"|5|5|
      | "Pebble Path"|6|6|