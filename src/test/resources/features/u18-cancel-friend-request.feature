Feature: U18 (Cancel friend request): As Liam, I want to cancel friends on Gardener's Grove so that we can manage my
  friends list with people I trust.

  Background:
    Given I "Liam" "Müller", 47 am a user with email "liam@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Kaia" "Pene", 67, a user with email "kaia@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Inaya" "Singh", 24, a user with email "inaya@email.com" and password "TestPassword10!"
    And I "Sarah" "Thompson", 36 am a user with email "sarah@email.com" and password "TestPassword10!"
    And I "sarah@email.com" am friends with "Fabian" "Gilson", 34, a user with email "fabian@email.com" and password "TestPassword10!"
    And I "Morgan" "English", 24 am a user with email "morgan@email.com" and password "TestPassword10!"


    Scenario: AC1 -  Given I am on my “manage friends” page, when I have pending request that I have sent, then I
    can cancel my friend request, and the other user cannot see the friend request, and the other user
    cannot accept the request anymore.
      Given I "liam@email.com" have sent an invite to "sarah@email.com"
      When I cancel my friend request to "sarah@email.com"
      And I as user "sarah@email.com" am logged in with "TestPassword10!"
      And I "sarah@email.com" have a pending invite from "morgan@email.com"
      Then I cannot see or accept the friend request from "liam@email.com"



    Scenario: AC2 - Given I am on my “manage friends” page, when I hit a UI element to remove a friend from my
    list, and I have confirmed that I want to remove that friend, then that friend is removed from my list
    of friends, and I cannot see this friend’s gardens, and this friend cannot see my gardens, and I am
    removed from the list of friends of that user.
      Given I as user "liam@email.com" am logged in with "TestPassword10!"
      When I hit the 'Remove Friend' button for user "kaia@email.com"
      Then That friend "kaia@email.com" is removed from my friends list
      And I as user "sarah@email.com" am logged in with "TestPassword10!"
      And That friend "liam@email.com" is removed from my friends list
