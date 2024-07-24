Feature: U17 (Send friend request): As Liam, I want to connect with my friends on Gardener's Grove
  so that we can build a community on the app.

  Background:
    Given I "Liam" "Müller", 47 am a user with email "liam@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Kaia" "Pene", 67, a user with email "kaia@email.com" and password "TestPassword10!"
    And I "liam@email.com" am friends with "Inaya" "Singh", 24, a user with email "inaya@email.com" and password "TestPassword10!"
    And I "Sarah" "Thompson", 36 am a user with email "sarah@email.com" and password "TestPassword10!"


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
    And I enter in <input>
    And I hit the search button
    Then I can see a list of users of the app exactly matching <fname> <lname> <email>
    Examples:
      | fname  | lname | email            | input     |
      | "Amy"  | "Doe" | "doe@gmail.com"  | "Amy Doe" |
      | "Andy" | ""    | "andy@gmail.com" | "Andy"    |

  Scenario Outline: AC5 - Given I am on the manage friends page and I have opened the search bar, when I enter an email
  address and I hit the search button, then I can see a list of users of the app exactly matching the email provided.

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And A user with first name <fname>, last name <lname>, and email <email> exists
    When I am on the 'Manage Friends' page
    And I enter in <input>
    And I hit the search button
    Then I can see a list of users of the app exactly matching <fname> <lname> <email>
    Examples:
      | fname  | lname | email            | input            |
      | "Amy"  | "Doe" | "doe@gmail.com"  | "doe@gmail.com"  |
      | "Andy" | ""    | "andy@gmail.com" | "andy@gmail.com" |

  Scenario Outline: AC5.1 - Given I am on the manage friends page and I have opened the search bar, when I enter an email
  address and I hit the search button, then I can see a list of users of the app exactly matching the email provided.

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And A user with first name <fname>, last name <lname>, and email <email> exists
    And I, user "liam@email.com", have declined a friend request from <email>
    When I am on the 'Manage Friends' page
    And I enter in <input>
    And I hit the search button
    Then I can see a list of users of the app exactly matching <fname> <lname> <email>
    Examples:
      | fname  | lname | email            | input            |
      | "Amy"  | "Doe" | "doe@gmail.com"  | "doe@gmail.com"  |
      | "Andy" | ""    | "andy@gmail.com" | "andy@gmail.com" |

  Scenario Outline: AC6 - Given I am on the manage friends page and I have opened the search bar, when I enter a search
  string and I press the search button and there are no perfect matches, then I see a message saying "There is nobody with that name or email in Gardener's Grove".

    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And A user with first name <fname>, last name <lname>, and email <email> exists
    When I am on the 'Manage Friends' page
    And I enter in <input>
    And I hit the search button
    Then I can see the error "There is nobody with that name or email in Gardener's Grove"
    Examples:
      | fname  | lname | email            | input              |
      | "Amy"  | "Doe" | "doe@gmail.com"  | "Andy"             |
      | "Amy"  | "Doe" | "doe@gmail.com"  | "random@gmail.com" |
      | "Amy"  | "Doe" | "doe@gmail.com"  | "liam@email.com"   |
      | "Andy" | ""    | "andy@gmail.com" | "Andy Doe"         |
      | "Andy" | ""    | "andy@gmail.com" | "sjdlkfjaljlf;"    |
      | "Andy" | ""    | "andy@gmail.com" | ""                 |

  Scenario: AC7 - Given I see a matching person for the search I made, when I hit the “invite as friend ” button,
  then the other user receives an invite that will be shown in their “manage friends” page.
    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And I am on the 'Manage Friends' page
    And I search for a user "sarah@email.com"
    When I hit the 'invite as friend' button for user with "sarah@email.com"
    And I as user "sarah@email.com" am logged in with "TestPassword10!"
    Then user "sarah@email.com" sees the invite from "liam@email.com"

  Scenario: AC8 - Given I am on the manage friends page, and I have pending invites, when I accept an invite, then
  that person is added to my list of friends, and I can see their profile, and I am added to that person's friends list, and that person can see my profile.
    Given I as user "sarah@email.com" am logged in with "TestPassword10!"
    And I am on the 'Manage Friends' page
    And I "sarah@email.com" have a pending invite from "liam@email.com"
    When I accept the pending invite from "liam@email.com"
    Then "liam@email.com" is added to my friends list

  Scenario: AC9 - Given I am on the manage friends page, and I have pending invites, when I decline an invite, then
  that person is not added to my list of friends, and they cannot invite me anymore.
    Given I as user "sarah@email.com" am logged in with "TestPassword10!"
    And I am on the 'Manage Friends' page
    And I "sarah@email.com" have a pending invite from "liam@email.com"
    When I decline the pending invite from "liam@email.com"
    Then "liam@email.com" are not added to friends list
    And "liam@email.com" can not add me "sarah@email.com"

  Scenario: AC10.1 - Given I have sent an invite, when it has been declined then I can see the status of the invite as “declined”.
    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And I am on the 'Manage Friends' page
    And I "liam@email.com" have sent an invite to "sarah@email.com"
    When "sarah@email.com" declines my "liam@email.com" request
    Then I see the request as declined by "sarah@email.com"

  Scenario: AC10.2 - Given I have sent an invite, when it has been left pending then I can see the status of the invite as “pending”.
    Given I as user "liam@email.com" am logged in with "TestPassword10!"
    And I am on the 'Manage Friends' page
    And I "liam@email.com" have sent an invite to "sarah@email.com"
    When "sarah@email.com" has not accepted or declined my "liam@email.com" request
    Then I see the request to "sarah@email.com" as pending


