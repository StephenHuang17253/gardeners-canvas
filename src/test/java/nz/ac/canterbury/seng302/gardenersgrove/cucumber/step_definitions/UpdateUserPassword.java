package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

@SpringBootTest
public class UpdateUserPassword {

    @Autowired
    public AuthenticationManager authenticationManager;
    @Autowired
    public PasswordEncoder passwordEncoder;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;
    @Autowired
    public FileService fileService;
    @Mock
    public EmailService emailService;
    @Autowired
    public SecurityService securityService;

    public static MockMvc mockMVC;
    public static UserService userService;

    User loggedInUser;

    String currentPassword;
    String origHash;
    String userEmail;

    @Before
    public void before_or_after_all() {
        emailService = Mockito.mock(EmailService.class);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        ProfileController profileController = new ProfileController(authenticationManager, userService, fileService,
                emailService, securityService);
        mockMVC = MockMvcBuilders.standaloneSetup(profileController).build();

    }

    @Given("I as user {string} with password {string} am on the change password page")
    public void iAsUserWithPasswordAmOnTheChangePasswordPage(String userEmail, String currentPassword)
            throws Exception {
        String url = "/profile/change-password";
        mockMVC.perform(
                MockMvcRequestBuilders
                        .get(url))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        loggedInUser = userService.getUserByEmail(userEmail);
        this.currentPassword = currentPassword;
        origHash = loggedInUser.getEncodedPassword();
        this.userEmail = loggedInUser.getEmailAddress();
    }

    @When("I enter two different new passwords: {string} and {string}")
    public void i_enter_two_different_passwords_in_new(String newPass, String retypePass) throws Exception {
        mockMVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/change-password")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", newPass)
                        .param("retypePassword", retypePass))
                .andReturn();
    }

    @Then("The password does not get updated")
    public void the_password_does_not_get_updated() {
        Assertions.assertEquals(origHash, userService.getUserByEmail(userEmail).getEncodedPassword());
    }

    @Then("The password is updated")
    public void the_password_is_updated() {
        Assertions.assertNotEquals(origHash, userService.getUserByEmail(userEmail).getEncodedPassword());
    }

    @When("I enter the weak password: {string}")
    public void i_enter_the_weak_password_weak_password(String weakPassword) throws Exception {
        mockMVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/change-password")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", weakPassword)
                        .param("retypePassword", weakPassword))
                .andReturn();
    }

    @When("I enter fully compliant password: {string}")
    public void i_enter_fully_compliant_details(String compliantPassword) throws Exception {
        mockMVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/change-password")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", compliantPassword)
                        .param("retypePassword", compliantPassword))
                .andReturn();
    }

    @When("I enter an old password {string} that does not match the current password")
    public void i_enter_an_old_password_that_does_not_match_the_current_password(String incorrectOldPassword)
            throws Exception {
        mockMVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/change-password")
                        .param("currentPassword", incorrectOldPassword)
                        .param("newPassword", "NewPassword10!")
                        .param("retypePassword", "NewPassword10!"))
                .andReturn();
    }

}
