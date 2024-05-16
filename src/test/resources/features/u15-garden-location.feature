Feature: U15 (Garden location API and autocomplete):
  As Kaia, I want to specify an actual address for my different recorded gardens,
  so that I can get consistent information relevant to my area

Background:
  Given I "Kaia" "Pene", 67 am a user with email "kaia@email.com" and password "TestPassword10!"
  And I as user "kaia@email.com" am logged in with "TestPassword10!"
  And I as user "kaia@email.com" have another garden "Kaia's Second Garden" located in "Dunedin", "New Zealand"

  Scenario Outline: AC1 - On the create new garden form, when I add a location, I can specify a full address made up of
    one field each for each of street address and number, suburb, city, postcode, and country.
    Given I specify a valid address with <streetAddress>, <suburb>, <city>, <postcode>, and <country>
    When I submit the create garden form
    Then The garden is created successfully with that location
    Examples:
      | streetAddress         | suburb             | city             | postcode   | country        |
      | "20 Kirkwood Avenue"  | "Upper Riccarton"  | "Christchurch"   | "8041"     | "New Zealand"  |
      | "Science Road"        | "Riccarton"        | "Christchurch"   | "8041"     | "New Zealand"  |

  Scenario Outline: AC2 - On the edit garden form, when I add a location, I can specify a full address made up of
  one field each for each of street address and number, suburb, city, postcode, and country.
    Given I specify a valid address with <streetAddress>, <suburb>, <city>, <postcode>, and <country>
    When I submit the edit garden form
    Then The garden details are successfully updated
    Examples:
      | streetAddress         | suburb             | city             | postcode   | country        |
      | "20 Kirkwood Avenue"  | "Upper Riccarton"  | "Christchurch"   | "8041"     | "New Zealand"  |
      | "Science Road"        | "Riccarton"        | "Christchurch"   | "8041"     | "New Zealand"  |

  Scenario Outline: AC3.1 - Given I am providing an address for a garden, then I must include values for both the country and
  the city.
    Given I specify a valid address with <streetAddress>, <suburb>, <city>, <postcode>, and <country>
    When I submit the create garden form
    Then The garden is created successfully with that location
    Examples:
      | streetAddress | suburb  | city              | postcode  | country        |
      | ""            | ""      | "Christchurch"    | ""        | "New Zealand"  |
      | ""            | ""      | "Dunedin"         | ""        | "New Zealand"  |

  Scenario Outline: AC3.2 - Given I am providing an address for a garden, then I must include values for both the country and
  the city.
    Given I specify an invalid address with <streetAddress>, <suburb>, <city>, <postcode>, and <country>
    When I submit the create garden form
    Then The garden is not created
    Examples:
      | streetAddress   | suburb  | city          | postcode  | country        |
      | ""              | "Ilam"  | ""            | "8041"    | "New Zealand"  |
      | ""              | ""      | "Dunedin"     | ""        | ""             |
      | "Cookie St 17"  | ""      | ""            | ""        | ""             |


  Scenario Outline: AC4 - Given I am providing an address for a garden, then I may additionally include any of the street
  name and number, suburb or postcode.
    Given I specify a valid address with <streetAddress>, <suburb>, <city>, <postcode>, and <country>
    When I submit the create garden form
    Then The garden is created successfully with that location
    Examples:
      | streetAddress         | suburb             | city             | postcode   | country        |
      | "20 Kirkwood Avenue"  | ""                 | "Christchurch"   | ""         | "New Zealand"  |
      | ""                    | "Riccarton"        | "Christchurch"   | ""         | "New Zealand"  |
      | ""                    | ""                 | "Christchurch"   | "8041"     | "New Zealand"  |
      | "Science Road"        | "Riccarton"        | "Christchurch"   | "8041"     | "New Zealand"  |