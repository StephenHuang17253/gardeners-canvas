package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
public class UpdateUserPassword {

    @Autowired
    public AuthenticationManager authenticationManager;
    @Autowired
    public PasswordEncoder passwordEncoder;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public FileService fileService;

    public static MockMvc MOCK_MVC;
    public static UserService userService;
    private MvcResult editPasswordResult;

    User loggedInUser;

    String currentPassword;
    String origHash;
    String userEmail;

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        ProfileController profileController = new ProfileController(userService, authenticationManager, fileService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(profileController).build();

    }
    @Given("I as user {string} with password {string} am on the edit password page")
    public void iAsUserWithPasswordAmOnTheEditPasswordPage(String userEmail, String currentPassword) throws Exception {
        String url = "/profile/editPassword";
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(url)

        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        loggedInUser = userService.getUserByEmail(userEmail);
        this.currentPassword = currentPassword;
        origHash = loggedInUser.getEncodedPassword();
        this.userEmail = loggedInUser.getEmailAddress();
    }



    @When("I enter two different new passwords: {string} and {string}")
    public void i_enter_two_different_passwords_in_new(String newPass,String retypePass) throws Exception {
        editPasswordResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/editPassword")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", newPass)
                        .param("retypePassword", retypePass)
        ).andReturn();
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
        editPasswordResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/editPassword")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", weakPassword)
                        .param("retypePassword", weakPassword)
        ).andReturn();
    }

    @When("I enter fully compliant password: {string}")
    public void i_enter_fully_compliant_details(String compliantPassword) throws Exception {
        editPasswordResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/editPassword")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", compliantPassword)
                        .param("retypePassword", compliantPassword)
        ).andReturn();
    }

    @When("I enter an old password {string} that does not match the current password")
    public void i_enter_an_old_password_that_does_not_match_the_current_password(String incorrectOldPassword) throws Exception {
        editPasswordResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/editPassword")
                        .param("currentPassword", incorrectOldPassword)
                        .param("newPassword", "NewPassword10!")
                        .param("retypePassword", "NewPassword10!")
        ).andReturn();
    }

}
