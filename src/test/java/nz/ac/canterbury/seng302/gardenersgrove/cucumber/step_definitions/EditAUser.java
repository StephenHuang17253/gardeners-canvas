package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
public class EditAUser {

    public MockMvc mockMVC;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public TokenRepository tokenRepository;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public FileService fileService;

    @Autowired
    public GridItemLocationRepository gridItemLocationRepository;

    public UserService userService;

    @Autowired
    public DecorationRepository decorationRepository;

    @Autowired
    public GardenTileRepository gardenTileRepository;

    String firstName = "John";
    String lastName = "Doe";
    Boolean noLastName = false;
    String emailAddress = "JohnDoe22@email.com";
    LocalDate dateOfBirth = LocalDate.of(2001, 2, 2);

    @Before
    public void before_or_after_all(Scenario scenario) {
        if (scenario.getSourceTagNames().contains("@NotRequiresSetup")) {
            return;
        }
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        tokenRepository.deleteAll();
        friendshipRepository.deleteAll();
        decorationRepository.deleteAll();
        gardenTileRepository.deleteAll();
        gridItemLocationRepository.deleteAll();
        userRepository.deleteAll();
        userService.addUser(new User(firstName,
                lastName,
                emailAddress,
                dateOfBirth), "1es1P@ssword");

        // Allows us to bypass spring security
        mockMVC = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(emailAddress,
                "1es1P@ssword");
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Given("There exists an old user with email {string}")
    public void there_exists_an_old_user_with_email(String email) {
        User user = new User("Admin", "Test", email, null);
        userService.addUser(user, "AlphabetSoup10!");
        Assertions.assertNotNull(userService.getUserByEmail(email));

    }

    @When("I click the \"Submit\" button")
    public void i_click_the_submit_button() throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = dateOfBirth.format(formatter);
        if (noLastName) {
            lastName = "";
        }

        mockMVC.perform(
                MockMvcRequestBuilders
                        .multipart("/profile/edit")
                        .file("profilePictureInput", null)
                        .param("firstName", firstName)
                        .param("lastName", lastName)
                        .param("noLastName", String.valueOf(noLastName))
                        .param("dateOfBirth", formattedDate)
                        .param("emailAddress", emailAddress)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()));
    }

    @When("I enter valid values for first name {string}, last name {string}, email address {string}, and date of birth {string}")
    public void i_enter_valid_values(String fname, String lname, String email, String date) {
        firstName = fname;
        lastName = lname;
        emailAddress = email;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dateOfBirth = LocalDate.parse(date, formatter);

    }

    @Then("No details are changed")
    public void no_details_are_changed() {
        Assertions.assertNotNull(userService.getUserByEmail(emailAddress));
        User testUser = userService.getUserByEmail(emailAddress);
        Assertions.assertEquals(firstName, testUser.getFirstName());
        Assertions.assertEquals(lastName, testUser.getLastName());
        Assertions.assertEquals(emailAddress, testUser.getEmailAddress());
        Assertions.assertEquals(dateOfBirth, testUser.getDateOfBirth());
    }

    @Then("My surname will become {string}")
    public void my_surname_will_become(String surname) {
        Assertions.assertNotNull(userService.getUserByEmail(emailAddress));
        User testUser = userService.getUserByEmail(emailAddress);
        Assertions.assertEquals(surname, testUser.getLastName());
    }

    @Then("I will be a user with first name {string}, last name {string}, email address {string}, and date of birth {string}")
    public void i_will_be_a_user_with_new_details(String fname, String lname, String email, String date) {
        Assertions.assertNotNull(userService.getUserByEmail(email));
        User testUser = userService.getUserByEmail(email);
        Assertions.assertEquals(fname, testUser.getFirstName());
        Assertions.assertEquals(lname, testUser.getLastName());
        Assertions.assertEquals(email, testUser.getEmailAddress());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate newDateOfBirth = LocalDate.parse(date, formatter);
        Assertions.assertEquals(newDateOfBirth, testUser.getDateOfBirth());
    }

    @When("I check the check box marked \"I have no surname\"")
    public void i_click_the_check_box_marked_I_have_no_surname() {
        noLastName = true;
    }

}
