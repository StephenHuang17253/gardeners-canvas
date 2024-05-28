Feature: U20 (Browsing public gardens): As Inaya, I want to be able to browse gardens that other users
  have made public so that I can learn from what other gardeners are doing.

  Background:
    Given I am browsing gardens
    And I added "Inaya" "Singh", 24 is a user with email "inaya123@email.com" and password "TestPassword10!"
    And I added "Jerry" "Thomas", 60 is a user with email "jerry@email.com" and password "Cheese123!"
    And I added "Thomas" "Jerry", 50 is a user with email "thomas@email.com" and password "TestPassword123!"
    And User "inaya123@email.com" has a garden "Inaya's Garden" located in "Dunedin", "New Zealand" with 13 plants.
    And User "jerry@email.com" has a garden "Jerry's Garden" located in "Christchurch", "New Zealand" with 10 plants.
    And User "thomas@email.com" has a garden "Tom's Garden" located in "Auckland", "New Zealand" with 2 plants.
    And I as user "inaya123@email.com" am logged in with "TestPassword10!"

  @NotRequiresSetup
  Scenario Outline: AC3 - Given I enter a search string and click a search button either labelled “search” or with a
  magnifying glass icon, then I am shown only gardens whose names or plants include my search value.

    Given I enter the search string <input>
    When I hit the search button with page <page>
    Then I am shown only gardens whose names or plants include my search string <input>
    Examples:
      | input         | page |
      | "1"           | 1    |
      | "Jerry"       | 1    |
      | "Garden"      | 1    |
      | "jerry@email" | 1    |

  @NotRequiresSetup
  Scenario Outline: AC5 - Given I enter a search string that has no matches, then a message tells me
  “No gardens match your search”.

    Given I enter the search string <input>
    When I hit the search button with page <page>
    Then A message tells me "No gardens match your search"
    Examples:
      | input   | page |
      | "Emily" | 1    |



