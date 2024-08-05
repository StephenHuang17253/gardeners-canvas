Feature: U27 (Import Plant): As Lei, I want to be able to import a plant into one of my gardens so that I don’t have
  to type all the information for it.

Background:
  Given "Kaia" "Pene", 67 is a user with email "kaia@email.com" and password "TestPassword10!"
  And "Lei" "Pene", 47 is a user with email "lei@email.com" and password "TestPassword10!"
  And User "kaia@email.com" has a garden "Kaia's Garden" located in "Dunedin", "New Zealand" with 13 plants.
  And User "lei@email.com" has a garden "Lei's Garden" located in "Christchurch", "New Zealand" with 10 plants.
  And I as user "kaia@email.com" am logged in with "TestPassword10!"


  Scenario:
  Given User "lei@email.com" has a plant with
