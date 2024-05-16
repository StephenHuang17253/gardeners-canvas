Feature: U15 (Garden location API and autocomplete):
  As Kaia, I want to specify an actual address for my different recorded gardens,
  so that I can get consistent information relevant to my area

Background:
  Given I "Kaia" "Pene", 67 am a user with email "kaia@email.com" and password "TestPassword10!"
  And I as user "kaia@email.com" am logged in with "TestPassword10!"

  Scenario Outline: AC1 - On the create new garden form, when I add a location, I can specify a full address made up of
    one field each for each of street address and number, suburb, city, postcode, and country.
    Given I specify a valid full address with <streetAddress>, <suburb>, <city>, <postcode>, and <country>
    When I click the Submit button on the create garden form
    Then The garden is created successfully with that location
    Examples:
      | streetAddress         | suburb             | city             | postcode   | country        |
      | "20 Kirkwood Avenue"  | "Upper Riccarton"  | "Christchurch"   | "8041"     | "New Zealand"  |
      | "Science Road"        | "Riccarton"        | "Christchurch"   | "8041"     | "New Zealand"  |
