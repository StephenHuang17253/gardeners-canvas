 Feature: U9 As Kaia, I want to see information about my garden so that I can keep the details accurate
   Background:
     Given "Kaia" "Pene", 67 is a user with email "kaia@email.com" and password "TestPassword10!"
     And User "kaia@email.com" has a garden "Kaia's Garden" located in "Christchurch", "New Zealand"
     And "jeff" "bezos", 60 is a user with email "jeffbezos@email.com" and password "Money123!"
     And User "jeffbezos@email.com" has a garden "Jeff's Money Trees" located in "Albuquerque", "America"

   Scenario: AC1 - A logged in user can view gardens which they own
     Given I as user "kaia@email.com" am logged in with "TestPassword10!"
     When I try to visit user "kaia@email.com"'s garden, "Kaia's Garden" 
     Then I am able to visit the page
     And The garden's name "Kaia's Garden" and location "Christchurch", "New Zealand" are visible

   Scenario: AC4 - A logged in user cannot view gardens which they don't own
     Given I as user "kaia@email.com" am logged in with "TestPassword10!"
     When I try to visit user "jeffbezos@email.com"'s garden, "Jeff's Money Trees" 
     Then I am unable to visit the page

   Scenario: AC5 - A non-logged in user cannot view any gardens
     Given I am not logged in
     When I try to visit user "jeffbezos@email.com"'s garden, "Jeff's Money Trees" 
     Then I am unable to visit the page
