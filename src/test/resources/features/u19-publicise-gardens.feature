Feature: U19 As Inaya, I want to be able to make my garden public so that others can see what I’m growing.

  Background:
    Given I "Kaia" "Pene", 67 am a user with email "kaia@email.com" and password "TestPassword10!"
    And I as user "kaia@email.com" have a garden "Kaia's Garden" located in "Christchurch", "New Zealand"
    And I as user "kaia@email.com" am logged in with "TestPassword10!"

  Scenario: AC1 - When I mark my garden as public, my garden is visible in search results
    Given User "kaia@email.com" is on my garden details page for "Kaia's Garden"
    When I mark a checkbox labelled "Make my garden public"
    Then My garden will be visible in search results