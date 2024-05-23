Feature: U17 (Send friend request): As Liam, I want to connect with my friends on Gardener’s Grove
  so that we can build a community on the app.

  Background:
    Given I "Liam" "Müller", 47 am a user with email "liam@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Kaia" "Pene", 67, a user with email "kaia@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Inaya" "Singh", 24, a user with email "inaya@email.com" and password "TestPassword10!"


  Scenario: AC1 - nav bar takes me to manage friends page
    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    When I click on the 'Manage Friends' button
    Then I am shown a 'Manage Friends' page

  Scenario: AC2 - Given I am on the manage friends page, then I see the list of my friends with their names, their
  profile pictures, and a link to their gardens list including private and public gardens.

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    When I am on the 'Manage Friends' page
    Then I see a list of my friends with their names, profile pictures, and link to their gardens list

  Scenario Outline: AC4 - Given I am on the manage friends page and I have opened the search bar, when I enter a full
  name (first and last name, if any) and I hit the search button, then I can see a list of users of the app exactly matching the name I provided.

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And A user with first name <fname>, last name <lname>, and email <email> exists
    When I am on the 'Manage Friends' page
    And I have opened the search bar
    And I enter in <fname>, <lname>, <email>
    And I hit the search button
    Then I can see a list of users of the app exactly matching <fname> <lname> <email>
    Examples:
    | fname   | lname   | email           |
    | "Amy"   | "Doe"   | "doe@gmail.com" |
    | "Andy"  | ""      | "andy@gmail.com"|

  Scenario Outline: AC5 - Given I am on the manage friends page and I have opened the search bar, when I enter an email
  address and I hit the search button, then I can see a list of users of the app exactly matching the email provided.

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And A user with first name <fname>, last name <lname>, and email <email> exists
    When I am on the 'Manage Friends' page
    And I have opened the search bar
    And I enter in <fname>, <lname>, <email>
    And I hit the search button
    Then I can see a list of users of the app exactly matching <fname> <lname> <email>
    Examples:
      | fname   | lname   | email           |
      | "Amy"   | "Doe"   | "doe@gmail.com" |
      | "Andy"  | ""      | "andy@gmail.com"|

  Scenario Outline: AC6 - Given I am on the manage friends page and I have opened the search bar, when I enter a search
  string and I press the search button and there are no perfect matches, then I see a message saying “There is nobody with that name or email in Gardener’s Grove”.

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And A user with first name <fname>, last name <lname>, and email <email> exists
    When I am on the 'Manage Friends' page
    And I have opened the search bar
    And I enter in <differentFName>, <differentLName>, <differentEmail>
    And I hit the search button
    Then I can see a list of users of the app not matching <fname> <lname> <email>
    Examples:
      | fname   | lname   | email           | differentFName | differentLName | differentEmail |
      | "Amy"   | "Doe"   | "doe@gmail.com" | "Andy"         | ""             | ""             |
      | "Andy"  | ""      | "andy@gmail.com"| "Andy"         | "Doe"          | ""             |



