package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantFormController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.RegistrationFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.h2.table.Plan;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@SpringBootTest
public class U10_Acceptance_Testing {

    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    public static GardenService gardenService;

    UserService userService;

    String firstName;
    String lastName;
    Boolean noLastName = false;
    String emailAddress;
    String password;
    String repeatPassword;
    LocalDate dateOfBirth;

    String gardenName;
    String gardenLocation;
    String gardenSize;

    @Before
    public void before_or_after_all() {
        gardenService = new GardenService(gardenRepository);
        GardenFormController gardenFormController = new GardenFormController(gardenService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup().build();
    }

    @Given("There exists a garden {string}")
    public void there_exists_a_garden(String gardenName) {
        Garden newGarden = new Garden(gardenName, "University Testing Lab", 0.0f);
        gardenService.addGarden(newGarden);
    }

    @Given("I am on the garden edit form")
    public void i_am_on_the_garden_edit_form() {
        gardenName = "My garden";
        gardenLocation = "My Location";
        gardenSize = "0.0";
    }
    @Given("I enter valid values for the {string}, {string}, and {string}")
    public void i_enter_valid_values_for_the_and_optionally(String name, String location, String size) {
        gardenName = name;
        gardenLocation = location;
        gardenSize = size;
    }
    @When("I enter a size using a comma")
    public void i_enter_a_size_using_a_comma() {
        gardenSize = "3,9";
    }
    @When("I enter an invalid name value {string}")
    public void i_enter_an_invalid_name_value_for_the(String string) {
        gardenName = string;
    }
    @When("I enter an invalid name location {string}")
    public void i_enter_an_invalid_location_value_for_the(String string) {
        gardenLocation = string;
    }
    @When("I enter an invalid size value {string}")
    public void i_enter_an_invalid_size_value_for_the(String string) {
        gardenSize = string;
    }
    @When("I click the edit plant form Submit button")
    public void i_click_the_edit_plant_form_submit_button() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
    @Then("The garden details have been updated")
    public void the_garden_details_have_been_updated() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("The garden details are not updated")
    public void the_garden_details_are_not_updated() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Then("I am taken back to the Garden Page")
    public void i_am_taken_back_to_the_garden_page() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }










// Implementation From Emma used for reference
//
//    @Given("There exists a user with email {string}")
//    public void there_exists_a_user_with_email(String email) {
//        User user = new User("Admin","Test",email,null);
//        userService.addUser(user,"AlphabetSoup10!");
//        Assertions.assertNotNull(userService.getUserByEmail(email));
//
//    }
//
//    @When("I enter valid values for my first name {string} and last name {string}, email address {string}, password {string}, repeat password {string} and optionally date of birth {string}")
//    public void i_enter_valid_values_for_my_first_name_and_last_name_email_address_password_repeat_password_and_date_of_birth(String fname, String lname, String email, String pass, String repeatPass, String dob){
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
//        dateOfBirth = LocalDate.parse(dob, formatter);
//        firstName = fname;
//        lastName = lname;
//        emailAddress = email;
//        password = pass;
//        repeatPassword = repeatPass;
//    }
//
//    @When("I click the check box marked \"I have no surname\"")
//    public void i_click_the_check_box_marked_I_have_no_surname(){
//        noLastName = true;
//    }
//
//    @When("I enter invalid values for my first name {string} and last name {string}")
//    public void i_enter_invalid_values_for_my_first_name_and_last_name(String fname, String lname){
//        String dob = "10/10/2001";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
//        dateOfBirth = LocalDate.parse(dob, formatter);
//        firstName = fname;
//        lastName = lname;
//        emailAddress = "johndoe@email.com";
//        password = "TestPass10!";
//        repeatPassword = "TestPass10!";
//    }
//
//    @When("I enter invalid value for my email {string}")
//    public void i_enter_invalid_value_for_my_email(String email){
//        String dob = "10/10/2001";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
//        dateOfBirth = LocalDate.parse(dob, formatter);
//        firstName = "James";
//        lastName = "Smith";
//        emailAddress = email;
//        password = "TestPass10!";
//        repeatPassword = "TestPass10!";
//    }
//
//    @When("I enter an invalid value for date of birth {string}")
//    public void i_enter_an_invalid_value_for_date_of_birth(String dob){
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
//        dateOfBirth = LocalDate.parse(dob, formatter);
//        firstName = "James";
//        lastName = "Smith";
//        emailAddress = "jamessmith@email.com";
//        password = "TestPass10!";
//        repeatPassword = "TestPass10!";
//    }
//
//    @When("I enter invalid passwords for password {string} and repeat password {string}")
//    public void i_enter_invalid_passwords_for_password_and_repeat_password(String pass, String repeatPass) {
//        String dob = "10/10/2001";
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
//        dateOfBirth = LocalDate.parse(dob, formatter);
//        firstName = "James";
//        lastName = "Smith";
//        emailAddress = "jamessmith@email.com";
//        password = pass;
//        repeatPassword = repeatPass;
//    }
//
//
//    @And("I click the \"Sign Up\" button")
//    public void i_click_sign_up_button() throws Exception {
//        MOCK_MVC.perform(
//                MockMvcRequestBuilders
//                        .post("/register")
//                        .param("firstName", firstName)
//                        .param("lastName", lastName)
//                        .param("noLastName", String.valueOf(noLastName))
//                        .param("dateOfBirth", String.valueOf(dateOfBirth))
//                        .param("emailAddress",emailAddress)
//                        .param("password", password)
//                        .param("repeatPassword", repeatPassword)
//        );
//
//    }
//
//    @Then("A new user is added to database")
//    public void a_new_user_is_added_to_database() {
//        Assertions.assertNotNull(userService.getUserByEmail(emailAddress));
//    }
//
//    @Then("No account is created")
//    public void no_account_is_created() {
//        Assertions.assertNull(userService.getUserByEmailAndPassword(emailAddress,password));
//    }

}
