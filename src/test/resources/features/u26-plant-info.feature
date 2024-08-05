Feature: As Lei, I want to be able to look up information about various plants so that I can learn more about them and whether they are suitable for my garden.

Background:
  Given I "Daemon" "targaryen", 41 am a user with email "daemon@email.com" and password "Password1!"
  And I as user "daemon@email.com" am logged in with "Password1!"

Scenario: AC2-  Given I am on the plant wiki page when I enter a search term
then I see a list of plant cards made up of plant names containing my search term and an image that corresponds to that plant if available
  Given I am on the Plant Wiki Page
  When I enter the search term "apple"
  Then I see a list of plant cards
  And they display names that contain my search term
  And an Image
