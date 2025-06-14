Feature: As Oliver, I want to be able to categorise my plants for my 3D viewer.

Background:
  Given I "Oliver" "Doe", 18 am a user with email "oliverdoe@email.com" and password "TestPassword10!"
  And I as user "oliverdoe@email.com" am logged in with "TestPassword10!"
  And User "oliverdoe@email.com" has a garden "Oliver's Little Garden" located in "Christchurch", "New Zealand"

Scenario: AC4.1 - I can select a plant category when created a new plant
    Given I "oliverdoe@email.com" am creating a new plant with the name 'Ferns'
    And I can see a list of categories
    When I select a category 'Bush'
    And I submit create new plant form
    Then My plant 'Ferns' is created with the plant category 'Bush'

Scenario: AC4.2 - I can select a plant category when editing an existing plant
    Given I "oliverdoe@email.com" have a plant 'Lily' with a plant category 'Flower'
    And I select edit plant
    And I can see a list of categories
    When I select a category 'Bush'
    And I submit edit plant form
    Then My plant 'Lily' is updated with the plant category 'Bush'

