package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

public class BrowsePublicGardens {

    @Given("{string} has {int} plants.")
    public void has_plants(String gardenName, Integer plantNo) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("I am on the browse gardens page")
    public void i_am_on_the_browse_gardens_page() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("I enter the search string {string}")
    public void i_enter_the_search_string(String input) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("I hit the search button")
    public void i_hit_the_search_button() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("I am shown only gardens whose names or plants include my search string {string}")
    public void i_am_shown_only_gardens_whose_names_or_plants_include_my_search_string(String input) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("A message tells me “No gardens match your search”.")
    public void a_message_tells_me_no_gardens_match_your_search() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


}
