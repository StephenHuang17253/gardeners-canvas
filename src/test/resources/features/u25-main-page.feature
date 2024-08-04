Feature: U25 (Main Page): As Liam, I want to have a main page for the application so that I can easily access other functionality and customize the information I am shown.

Background:
  Given "Liam" "Smith", 67 is a user with email "liam@email.com" and password "Password1!"
  And "Inaya" "Singh", 24 is a user with email "inaya123@email.com" and password "TestPassword10!"
  And "Jerry" "Thomas", 60 is a user with email "jerry@email.com" and password "Cheese123!"
  And "Thomas" "Jerry", 50 is a user with email "thomas@email.com" and password "TestPassword123!"
  And "Sarah" "Thompson", 36 is a user with email "sarah@email.com" and password "TestPassword10!"

Scenario: I Have No Friends to show
  Given I am a user with email "liam@email.com" and no recently added friends
  When I look at the recent friends list on the home page
  Then There are no recently accessed friends

Scenario: I Have three recent Friends, they show up in order
  Given I am a user with email "liam@email.com" and no recently added friends
  And I "liam@email.com" have a pending invite from "inaya123@email.com"
  And I "liam@email.com" have a pending invite from "jerry@email.com"
  And I "liam@email.com" have a pending invite from "thomas@email.com"
  When I accept the pending invite from "jerry@email.com"
  And I accept the pending invite from "thomas@email.com"
  And I look at the recent friends list on the home page
  Then I see that my friends with emails "jerry@email.com" and "thomas@email.com" are listed in order

Scenario: (AC6) I see plants that need watering notifications
  Given I as user "liam@email.com" am logged in with "Password1!"
  And I am on the home page
  When I have garden called "Thirsty Garden" that needs watering for user "liam@email.com"
  Then I can see that "Thirsty Garden" need watering in the watering notifications for "liam@email.com"


Scenario: (AC7) I see a notification for friend requests
  Given I as user "sarah@email.com" am logged in with "TestPassword10!"
  And I "sarah@email.com" have a pending invite from "liam@email.com"
  When I am on the home page
  Then I can see a button and message that says "You have friend requests"
