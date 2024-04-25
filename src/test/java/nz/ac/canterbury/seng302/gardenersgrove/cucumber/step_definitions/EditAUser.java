package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class EditAUser {

    @Given("I have a surname {string}")
    public void i_have_a_surname(String surname) {
    }
    @When("I click the {string} button")
    public void i_click_the_button(String submit) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @When("I enter valid values for first name {string}, last name {string}, email address {string}, and date of birth {string}")
    public void i_enter_valid_values(String fname, String lname, String email, String date) {

    }
    @Then("No details are changed")
    public void no_details_are_changed() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("My surname will become {string}")
    public void my_surname_will_become(String surname) {
    }

    @Then("I will be a user with first name {string}, last name {string}, email address {string}, and date of birth {string}")
    public void i_will_be_a_user_with_new_details(String fname, String lname, String email, String date) {
        
    }





}
