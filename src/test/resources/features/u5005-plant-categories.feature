Feature: As Oliver, I want to be able to categorise my plants for my 3D viewer.

Background:
  Given I "Oliver" "Doe", 18 am a user with email "oliverdoe@email.com" and password "TestPassword10!"
  And I as user "oliverdoe@email.com" am logged in with "TestPassword10!"
  And User "oliverdoe@email.com" has a garden "Oliver's Little Garden" located in "Christchurch", "New Zealand"

Scenario: AC4.1 - I can select a plant category when created a new plant
    Given I am creating a new plant with the name 'Ferns'
    And I can see a list of categories
    When I select a category 'Bush'
    And I submit my plant form
    Then My plant 'Ferns' is created with the plant category 'Bush'

Scenario: AC4.2 - I can select a plant category when editing an existing plant
    Given I have a plant 'Lily' with a plant category 'Flower'
    And I select edit plant
    And I can see a list of categories
    When I select a category 'Bush'
    And I submit my plant form
    Then My plant 'Lily' is updated with the plant category 'Bush'

Scenario: AC4.3 - I see an error message if I do not select a plant category when creating a new plant
    Given I am creating a new plant with name 'Lemons'
    When I do not select a plant category
    And I submit my plant form
    Then I receive an error message reading 'A plant category must be selected'