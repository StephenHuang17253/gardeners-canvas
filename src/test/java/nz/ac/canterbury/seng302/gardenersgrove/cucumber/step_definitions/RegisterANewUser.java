package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AccountController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
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
public class RegisterANewUser {

    public static MockMvc mockMVC;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public GardenService gardenService;

    public static UserService userService;

    public EmailService emailService;

    public TokenService tokenService;

    @Autowired
    public SecurityService securityService;

    String firstName;
    String lastName;
    Boolean noLastName = false;
    String emailAddress;
    String password;
    String repeatPassword;
    LocalDate dateOfBirth;

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        emailService = Mockito.mock(EmailService.class);
        tokenService = Mockito.mock(TokenService.class);
        gardenService = Mockito.mock(GardenService.class);

        AccountController accountController = new AccountController(userService, authenticationManager, emailService,
                tokenService, gardenService, securityService);
        mockMVC = MockMvcBuilders.standaloneSetup(accountController).build();
    }

    @Given("There exists a user with email {string}")
    public void there_exists_a_user_with_email(String email) {
        User user = new User("Admin", "Test", email, null);
        userService.addUser(user, "AlphabetSoup10!");
        Assertions.assertNotNull(userService.getUserByEmail(email));

    }

    @When("I enter valid values for my first name {string} and last name {string}, email address {string}, password {string}, repeat password {string} and optionally date of birth {string}")
    public void i_enter_valid_values_for_my_first_name_and_last_name_email_address_password_repeat_password_and_date_of_birth(
            String fname, String lname, String email, String pass, String repeatPass, String dob) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        dateOfBirth = LocalDate.parse(dob, formatter);
        firstName = fname;
        lastName = lname;
        emailAddress = email;
        password = pass;
        repeatPassword = repeatPass;
    }

    @When("I click the check box marked \"I have no surname\"")
    public void i_click_the_check_box_marked_I_have_no_surname() {
        noLastName = true;
    }

    @When("I enter invalid values for my first name {string} and last name {string}")
    public void i_enter_invalid_values_for_my_first_name_and_last_name(String fname, String lname) {
        String dob = "10/10/2001";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        dateOfBirth = LocalDate.parse(dob, formatter);
        firstName = fname;
        lastName = lname;
        emailAddress = "johndoe@email.com";
        password = "TestPass10!";
        repeatPassword = "TestPass10!";
    }

    @When("I enter invalid value for my email {string}")
    public void i_enter_invalid_value_for_my_email(String email) {
        String dob = "10/10/2001";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        dateOfBirth = LocalDate.parse(dob, formatter);
        firstName = "James";
        lastName = "Smith";
        emailAddress = email;
        password = "TestPass10!";
        repeatPassword = "TestPass10!";
    }

    @When("I enter an invalid value for date of birth {string}")
    public void i_enter_an_invalid_value_for_date_of_birth(String dob) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        dateOfBirth = LocalDate.parse(dob, formatter);
        firstName = "James";
        lastName = "Smith";
        emailAddress = "jamessmith@email.com";
        password = "TestPass10!";
        repeatPassword = "TestPass10!";
    }

    @When("I enter invalid passwords for password {string} and repeat password {string}")
    public void i_enter_invalid_passwords_for_password_and_repeat_password(String pass, String repeatPass) {
        String dob = "10/10/2001";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        dateOfBirth = LocalDate.parse(dob, formatter);
        firstName = "James";
        lastName = "Smith";
        emailAddress = "jamessmith@email.com";
        password = pass;
        repeatPassword = repeatPass;
    }

    @And("I click the \"Sign Up\" button")
    public void i_click_sign_up_button() throws Exception {
        mockMVC.perform(
                MockMvcRequestBuilders
                        .post("/register")
                        .param("firstName", firstName)
                        .param("lastName", lastName)
                        .param("noLastName", String.valueOf(noLastName))
                        .param("dateOfBirth", String.valueOf(dateOfBirth))
                        .param("emailAddress", emailAddress)
                        .param("password", password)
                        .param("repeatPassword", repeatPassword));
    }

    @Then("A new user is added to database")
    public void a_new_user_is_added_to_database() {
        Assertions.assertNotNull(userService.getUserByEmail(emailAddress));
    }

    @Then("No account is created")
    public void no_account_is_created() {
        Assertions.assertNull(userService.getUserByEmailAndPassword(emailAddress, password));
    }
}
