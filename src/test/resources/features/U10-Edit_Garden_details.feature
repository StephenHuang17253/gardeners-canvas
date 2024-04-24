Feature: U10 Edit garden details

Background:
  Given There is a user
  And the user owns a garden "Test Garden"
  And I am on the garden edit form


  Scenario Outline: AC2 - valid values
    When  I enter valid values for the "<name>", "<location>", and "<size>"
    And I click the edit plant form Submit button
    And The garden details have been updated
    Examples:
      | name         | location        | size  |
      | gard         |   here          | 7     |
      | ward         |   there         | 15    |
      | Everywhere   |   13 A street   | 7,9   |
      | ward         |   14 b STREET   | 7.9   |
      | ward-connect |   14 b STREET   | 7.9   |
      | ward-cÁÕăect |   14 b STÁÕăT   | 0.1   |

  Scenario Outline: AC3
    Given I enter valid values for the "name", "Location", and "0.0"
    When I enter an invalid name value "<Name>"
    And I click the edit plant form Submit button
    Then The garden details are not updated
    Examples:
      | Name            |
      | 13@fifty_laner  |
      | sgsha!asdksad   |
      | nowhereçua§il   |
      | louois_hobson!  |
      | [Alexandra]     |
      | Clone #12       |
      |        %        |



  Scenario Outline: AC4
    Given I enter valid values for the "name", "Location", and "0.0"
    When I enter an invalid size value "<size>"
    And I click the edit plant form Submit button
    Then The garden details are not updated
    Examples:
      | size            |
      | 13@fifty_laner  |
      | sgsha!asdksad   |
      | nowhereçua§il   |
      | 12.6.5          |
      | six             |
      | 12,4.3          |
      | Null            |


  Scenario Outline: AC5
    Given I enter valid values for the "name", "Location", and "0.0"
    When I enter an invalid location value "<inv_location>"
    And I click the edit plant form Submit button
    Then The garden details are not updated
    Examples:
      | inv_location   |
      | 13@fifty_laner |
      | sgsha!asdksad  |
      | nowhereçua§il   |


  Scenario: AC6
    Given I enter valid values for the "name", "Location", and "0.0"
    When I enter a size using a comma
    And I click the edit plant form Submit button
    Then The garden details have been updated