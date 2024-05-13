Feature: U10 (Edit garden details): As Kaia, I want to edit information about my garden so I can keep it up to date

Background:
  Given I "Kaia" "Pene", 67 am a user with email "kaia@email.com" and password "TestPassword10!"
  And I as user "kaia@email.com" have a garden "Kaia's Garden" located in "Christchurch", "New Zealand"
  And I as user "kaia@email.com" am logged in with "TestPassword10!"

  Scenario: AC1 - “Edit” button takes you to prepopulated edit garden form
    Given I as user "kaia@email.com" is on my garden details page for "Kaia's Garden"
    When I click the edit garden button
    Then I see the edit garden form where all the details are prepopulated


  Scenario Outline: AC2 can submit valid values
    Given I am on the garden edit form
    And  I enter valid garden values for the <name>, <city>, <country> and <size>
    When I click the Submit button on the edit garden form
    Then The garden details have been updated
    And I am taken back to the garden details page
    Examples:
      | name            | city            | country        | size       |
      | "gard"          | "Townsville"    | "France"       | "7"        |
      | "ward"          | "Läkeside"      | "Switzerland"  | "15"       |
      | "Everywhere"    | "Rivèrdale"     | "Italy"        | "7.9"      |
      | "ward"          | "Hílltop"       | "Spain"        | "7.9"      |
      | "ward-connect"  | "Súnset City"   | "Brazil"       | "7.9"      |
      | "ward-cÁÕăect"  | "Ocëanview"     | "Portugal"     | "0.1"      |
      | "ward-connect"  | "Súnset City"   | "Brazil"       | "0.01"      |
      | "ward-cÁÕăect"  | "Ocëanview"     | "Portugal"     | "8000000"      |

  Scenario Outline: AC3 can't submit non-alphanumeric names
    Given I am on the garden edit form
    And I enter an invalid garden name value <Name>
    When I click the Submit button on the edit garden form
    Then The garden details are not updated
    Examples:
      | Name              |
      | "13@fifty_laner"  |
      | "sgsha!asdksad"   |
      | "nowhereçua§il"   |
      | "louois_hobson!"  |
      | "[Alexandra]"     |
      | "Clone #12"       |
      | " "               |
      | ""                |

  Scenario Outline: AC4 - can't submit invalid size
    Given I am on the garden edit form
    And I enter an invalid garden size value <size>
    When I click the Submit button on the edit garden form
    Then The garden details are not updated
    Examples:
      | size              |
      | "13@fifty_laner"  |
      | "sgsha!asdksad"   |
      | "nowhereçua§il"   |
      | "12.6.5"          |
      | "six"             |
      | "12,4.3"          |
      | "Null"            |
      | "0.009"           |
      | "8000000.01"      |
      | "0.00"            |


  Scenario Outline: AC5 - cannot submit invalid location
    Given I am on the garden edit form
    And I enter invalid garden location values <City>, <Country>
    When I click the Submit button on the edit garden form
    Then The garden details are not updated
    Examples:
      | City            | Country         |
      | "a"             | "a"             |
      | "13@fifty_laner"| "sgsha!asdksad" |
      | "nowhereçua§i?" | "a"             |



  Scenario: AC6 - can enter a size with a comma instead of full stop
    Given I am on the garden edit form
    And I enter 1,5 as a size
    When I click the Submit button on the edit garden form
    Then The garden details have been updated
    And I am taken back to the garden details page