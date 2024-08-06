Feature: Browsing gardens by tag
  As Inaya,
  I want to be able to browse gardens by different tags
  So that I can browse for gardens that match my interests.

  Background:
    Given I am browsing gardens
    And I added "Inaya" "Singh", 24 is a user with email "inaya123@email.com" and password "TestPassword10!"
    And I added "Paul" "Mccartney", 82 is a user with email "pol@email.com" and password "Blackbird1!"
    And I added "Ringo" "Star", 84 is a user with email "rango@email.com" and password "PeaceAndLove1!"


  Scenario: AC6 - Search for gardens by tag
    Given I am on the browse garden page
    And The user "pol@email.com" has a public garden called "Pauls garden" that has the tag "Fruit"
    And I apply the tag "Fruit"
    When I submit the search with tag
    Then The search results contain the garden called "Pauls garden"

  Scenario: AC6.2 - Search for gardens by tag and search field
    Given I am on the browse garden page
    And The user "rango@email.com" has a public garden called "Ringos garden" that has the tag "Banana"
    And I input search value "Ringos garden"
    And I apply the tag "Banana"
    When I submit the search with both search and tag
    Then The search results contain the garden called "Ringos garden"
