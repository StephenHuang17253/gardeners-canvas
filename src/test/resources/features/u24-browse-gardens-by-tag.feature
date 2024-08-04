Feature: Browsing gardens by tag
  As Inaya,
  I want to be able to browse gardens by different tags
  So that I can browse for gardens that match my interests.

  Background:
    Given I am browsing gardens
    And I added "Inaya" "Singh", 24 is a user with email "inaya123@email.com" and password "TestPassword10!"
    And I added "John" "Lennon", 40 is a user with email "johnlennon@email.com" and password "Imagine1!"
    And I added "Paul" "Mccartney", 82 is a user with email "pol@email.com" and password "Blackbird1!"
    And I added "Ringo" "Star", 84 is a user with email "rango@email.com" and password "PeaceAndLove1!"
    And I added "George" "Harrison", 58 is a user with email "layla@email.com" and password "Brackets1!"
    And The user "pol@email.com" has a public garden called "Pauls garden" that has the tag "Fruit"

  Scenario: AC6 - Browse gardens by tag
    Given I am on the browse garden page
    And I input search value "Pauls garden"
    And I apply the tag "Fruit"
    When I submit the search with both search and tag
    Then The search results contain the garden called "Pauls garden"
