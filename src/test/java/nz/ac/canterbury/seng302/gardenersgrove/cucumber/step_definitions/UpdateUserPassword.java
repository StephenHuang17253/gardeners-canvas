package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
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

    @Before
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
    public void iEnterTwoDifferentPasswordsInNew(String newPass,String retypePass) {
        currentPassword = originalPassword;
        newPassword = newPass;
        retypePassword = retypePass;
    }
    @Then("The password does not get updated")
    public void thePasswordDoesNotGetUpdated() {
        Assertions.assertNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",newPassword));
        Assertions.assertNotNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",originalPassword));
    }
    @Then("The password is updated")
    public void thePasswordIsUpdated() {
        Assertions.assertNotNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",newPassword));
    }

    @When("I enter the weak password: {string}")
    public void iEnterTheWeakPasswordWeakPassword(String weakPassword) {
        currentPassword = originalPassword;
        newPassword = weakPassword;
        retypePassword = weakPassword;
    }

    @When("I enter fully compliant password: {string}")
    public void iEnterFullyCompliantDetails(String compliantPassword) {
        currentPassword = originalPassword;
        newPassword = compliantPassword;
        retypePassword = compliantPassword;
    }

    @And("I click the “Submit” button")
    public void iClickTheSubmitButton() throws Exception {
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
    public void iEnterAnOldPasswordThatDoesNotMatchTheCurrentPassword(String oldPassword) {
        currentPassword = oldPassword;
        newPassword = "TestPass10!";
        retypePassword = "TestPass10!";
    }

}
