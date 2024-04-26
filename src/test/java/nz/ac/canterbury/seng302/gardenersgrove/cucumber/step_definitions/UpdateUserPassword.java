package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.Assert;

public class UpdateUserPassword {

    public AuthenticationManager authenticationManager;
    public static MockMvc MOCK_MVC;
    public UserRepository userRepository;
    public static UserService userService;
    public PasswordEncoder passwordEncoder;
    public FileService fileService;

    String currentPassword;
    String newPassword;
    String retypePassword;

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
    }

    @When("I enter the weak password: {string}")
    public void IEnterAWeakPassword(String weakPassword) {

    }
    @When("I enter two different new passwords: {string} and {string}")
    public void iEnterTwoDifferentPasswordsInNew(String newPassword,String retypePassword) {

    }
    @Then("The password does not get updated")
    public void thePasswordDoesNotGetUpdated() {

    }
    @Then("The password is updated")
    public void thePasswordIsUpdated() {

    }

    @When("I enter the weak password: <weakPassword>")
    public void iEnterTheWeakPasswordWeakPassword() {
        
    }

    @When("I enter fully compliant details")
    public void iEnterFullyCompliantDetails() {
        
    }

    @And("I click the “Submit” button")
    public void iClickTheSubmitButton() {
    }

    @When("I enter an old password {string} that does not match the current password")
    public void iEnterAnOldPasswordThatDoesNotMatchTheCurrentPassword(String oldPassword) {

    }
}
