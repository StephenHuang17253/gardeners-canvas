Feature: U11 (Record plants in garden): As Kaia, I want to record the different plants in my garden so I can keep track of the plants I have

  Background:
    Given "Kaia" "Pene", 67 is a user with email "kaia@email.com" and password "TestPassword10!"
    And User "kaia@email.com" has a garden "Kaia's Garden" located in "Christchurch", "New Zealand"
    And I as user "kaia@email.com" am logged in with "TestPassword10!"

    Scenario: AC1 -  “Add new plant” button takes you to a new "add plant form"
      Given I as user "kaia@email.com" is on my garden details page for "Kaia's Garden"
      When I click the add new plant button
      Then I see an add plant form.

    Scenario Outline: AC2 - Can submit valid values for plant
      Given I am on the add plant form
      And  I enter valid plant values for the <name>, <count>, <description>, and <date>
      When I click the Submit button on the add plant form
      Then a new plant record is added to the garden
      And I am taken back to the garden details page
      Examples:
        | name          | description | count     | date          |
        | "plant"       | "Info"      | "1"       | "11/11/2000"  |
        | "Plant"       | "Lä Info"   | "3"       | "07/06/2023"  |
        | "Cool Plant"  | "Rivèndel"  | "100"     | "31/06/2020"  |
        | "Yes"         | "Hí there"  | "2000"    | "01/01/2001"  |


    Scenario Outline: AC3 - Can't submit invalid names
      Given I am on the add plant form
      And  I enter invalid plant value for the <name>
      When I click the Submit button on the add plant form
      Then a new plant record is not added to the garden
      Examples:
        | name              |
        | "13@fifty_laner"  |
        | "sgsha!asdksad"   |
        | "nowhereçua§il"   |
        | "louois_hobson!"  |
        | "[Alexandra]"     |
        | "Clone #12"       |
        | " "               |
        | ""                |


    Scenario Outline: AC4 - Can't submit long description
      Given I am on the add plant form
      And  I enter invalid plant value for the <description>
      When I click the Submit button on the add plant form
      Then a new plant record is not added to the garden
      Examples:
        | description |
        | a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant description that exceeds the maximum length a very long plant description that a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant name that exceeds the maximum length a very long plant description that exceeds the maximum length a very long plant description that |


    Scenario Outline: AC5 - Can't submit invalid count
      Given I am on the add plant form
      And  I enter invalid plant value for the <count>
      When I click the Submit button on the add plant form
      Then a new plant record is not added to the garden
      Examples:
        | count            |
        | "13@fifty_laner" |
        | "sgsha!asdksad"  |
        | "nowhereçua§il"  |
        | "12.6.5"         |
        | "six"            |
        | "12,4.3"         |
        | "Null"           |
        | "0.009"          |
        | "8000000.01"     |
        | "0.00"           |

    Scenario Outline: AC6 - Can't submit invalid date format
      Given I am on the add plant form
      And  I enter invalid plant value for the <date>
      When I click the Submit button on the add plant form
      Then a new plant record is not added to the garden
      Examples:
        | date             |
        | "13@fifty_laner" |
        | "sgsha!asdksad"  |
        | "nowhereçua§il"  |
        | "12.6.5"         |
        | "six"            |
        | "12,4.3"         |
        | "Null"           |
        | "0.009"          |
        | "8000000.01"     |
        | "0.00"           |

