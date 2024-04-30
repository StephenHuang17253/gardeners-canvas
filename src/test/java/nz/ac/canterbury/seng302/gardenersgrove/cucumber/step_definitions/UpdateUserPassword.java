package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
    @Autowired
    private WebApplicationContext context;


    public static MockMvc MOCK_MVC;
    public static UserService userService;


    String currentPassword;
    String newPassword;
    String retypePassword;
    String userEmail;
    String originalPassword;
    //
    //
    //New Tests:
    //
    //

    @Given ("Given i am on the edit password page")
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        ProfileController profileController = new ProfileController(userService, authenticationManager, fileService);


        userEmail = "jamesdoe@email.com";
        originalPassword = "AlphabetSoup10!";
        // Prep for user authentication that will be added later on
        User user = new User("Admin", "Test", userEmail, null);
        if (!userService.emailInUse(userEmail))
        {
            userService.addUser(user, originalPassword);
        }

        MOCK_MVC = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(userEmail, originalPassword);
        Authentication authentication = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @When("I enter two different new passwords: {string} and {string}")
    public void i_enter_two_different_passwords_in_new(String newPass,String retypePass) {
        currentPassword = originalPassword;
        newPassword = newPass;
        retypePassword = retypePass;
    }
    @Then("The password does not get updated")
    public void the_password_does_not_get_updated() {
        Assertions.assertNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",newPassword));
        Assertions.assertNotNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",originalPassword));
    }
    @Then("The password is updated")
    public void the_password_is_updated() {
        Assertions.assertNotNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",newPassword));
    }

    @When("I enter the weak password: {string}")
    public void i_enter_the_weak_password_weak_password(String weakPassword) {
        currentPassword = originalPassword;
        newPassword = weakPassword;
        retypePassword = weakPassword;
    }

    @When("I enter fully compliant password: {string}")
    public void i_enter_fully_compliant_details(String compliantPassword) {
        currentPassword = originalPassword;
        newPassword = compliantPassword;
        retypePassword = compliantPassword;
    }

    @And("I click the “Submit” button to edit password")
    public void i_click_the_submit_button() throws Exception {
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/profile/editPassword")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", newPassword)
                        .param("retypePassword", retypePassword)
                        .with(csrf())
        );
    }

    @When("I enter an old password {string} that does not match the current password")
    public void i_enter_an_old_password_that_does_not_match_the_current_password(String oldPassword) {
        currentPassword = oldPassword;
        newPassword = "TestPass10!";
        retypePassword = "TestPass10!";
    }

}
