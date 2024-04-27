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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;
@SpringBootTest
public class UpdateUserPassword {

    @Autowired
    public AuthenticationManager authenticationManager;
    public static MockMvc MOCK_MVC;
    @Autowired
    public UserRepository userRepository;
    public static UserService userService;
    @Autowired
    public PasswordEncoder passwordEncoder;
    public FileService fileService;

    String currentPassword;
    String newPassword;
    String retypePassword;
    String userEmail;
    //
    //
    //New Tests:
    //
    //

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        ProfileController profileController = new ProfileController(userService, authenticationManager, fileService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(profileController).build();

        userEmail = "jamesdoe@email.com";
        // Prep for user authentication that will be added later on
        User user = new User("Admin", "Test", userEmail, null);
        if (!userService.emailInUse(userEmail))
        {
            userService.addUser(user, "AlphabetSoup10!");
        }
    }

    @When("I enter two different new passwords: {string} and {string}")
    public void iEnterTwoDifferentPasswordsInNew(String newPass,String retypePass) {
        currentPassword = "AlphabetSoup10!";
        newPassword = newPass;
        retypePassword = retypePass;
    }
    @Then("The password does not get updated")
    public void thePasswordDoesNotGetUpdated() {
        Assertions.assertNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",newPassword));
        Assertions.assertNotNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",currentPassword));
    }
    @Then("The password is updated")
    public void thePasswordIsUpdated() {
        System.out.println(userService.getUserByEmailAndPassword("jamesdoe@email.com",currentPassword));
        Assertions.assertNotNull(userService.getUserByEmailAndPassword("jamesdoe@email.com",newPassword));
    }

    @When("I enter the weak password: {string}")
    public void iEnterTheWeakPasswordWeakPassword(String weakPassword) {
        currentPassword = "AlphabetSoup10!";
        newPassword = weakPassword;
        retypePassword = weakPassword;
    }

    @When("I enter fully compliant password: {string}")
    public void iEnterFullyCompliantDetails(String compliantPassword) {
        currentPassword = "AlphabetSoup10!";
        newPassword = compliantPassword;
        retypePassword = compliantPassword;
    }

    @And("I click the “Submit” button")
    public void iClickTheSubmitButton() throws Exception {
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/editPassword")
                        .param("currentPassword", currentPassword)
                        .param("newPassword", newPassword)
                        .param("repeatPassword", retypePassword)
        );
    }

    @When("I enter an old password {string} that does not match the current password")
    public void iEnterAnOldPasswordThatDoesNotMatchTheCurrentPassword(String oldPassword) {
        currentPassword = oldPassword;
        newPassword = "TestPass10!";
        retypePassword = "TestPass10!";
    }

}
