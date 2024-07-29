Feature: U21 (Adding tags to gardens) : As Inaya, I want to be able to browse gardens by different
  user-specified tags so that I can browse for gardens that match my interests.

  Background:
    Given I "Jacaerys" "Velaryon", 18 am a user with email "jace@email.com" and password "TestPassword10!"
    And I "jace@email.com" am friends with "Cregan" "Stark", 26, a user with email "cregan@email.com" and password "TestPassword10!"
    And I as user "jace@email.com" have a garden



  Scenario: AC2 - Given I am on the garden details page for a public garden, then I can see a list of tags that the
  garden has been marked with by its owner.

    Given I as user "jace@email.com" am logged in with "TestPassword10!"
    And I access a garden details page for a public garden owned by "cregan@email.com"
    Then I see a list of tags that the garden has been marked with by its owner

  Scenario: AC3 - Given I am typing a tag, then I should see autocomplete options for tags that already exist in the system
    Given I as user "jace@email.com" am logged in with "TestPassword10!"
    And I begin typing the tag "gar"
    Then I see autocomplete options for existing tags

  Scenario: AC5 - I add a non-existing valid tag to my garden and it shows in future auto-complete suggestions
    Given I as user "jace@email.com" am logged in with "TestPassword10!"
    When I enter a valid tag "Cabbage Patch"
    Then the tag is added to my garden
    And the tag shows up in future autocomplete suggestions

  Scenario Outline: AC6 - Given I have entered invalid text (i.e. non-alphanumeric characters except spaces, dashes,
  underscore, and apostrophes), when I click the “+” button or press enter, then 
  an error message tells me “The tag name must only contain alphanumeric characters, spaces, -, _, ', or ” , 
  and no tag is added to my garden and no tag is added to the user defined tags the system knows.

    Given I as user "jace@email.com" am logged in with "TestPassword10!"
    When I try to add an invalid tag <tag> to my garden
    Then The tag is not added to the garden
    And The following error message is displayed "The tag name must only contain alphanumeric characters, spaces, -, _, ', or \""
    Examples:
      | tag                       |
      | "!!"                      |
      | "IllegalTag!"             |
      | "IllegalTag@("            |
      | "IllegalTag#)"            |
      | "IllegalTag%*"            |
      | "IllegalTag^&-+\[];'/"    |


  Scenario Outline: AC7 - Given I have entered a tag that is more than 25 characters, when I click the “+” button or press
  enter, then an error message tells me “A tag cannot exceed 25 characters”, and no tag is added to my garden
  and no tag is added to the user defined tags the system knows.

    Given I as user "jace@email.com" am logged in with "TestPassword10!"
    When I try to add an invalid tag <tag> to my garden
    Then The following error message is displayed "A tag cannot exceed 25 characters"
    And The tag is not added to the garden

    Examples:
      | tag                                                           |
      | "This tag name is too long to be added"                       |
      | "Aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"   |
      | "Aasdfljasdfasdsdlklfskdlkflsdkflksdlfklsdkflkdsfksdlfklsk"   |