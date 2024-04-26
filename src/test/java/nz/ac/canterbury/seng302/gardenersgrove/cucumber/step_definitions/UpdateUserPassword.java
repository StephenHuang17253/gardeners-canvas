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

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        ProfileController profileController = new ProfileController(userService, authenticationManager, fileService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(profileController).build();
    }

    //AC1
    @Given("I am on the edit profile form")
    public void iAmOnTheEditProfileForm() throws Exception {
    //    MOCK_MVC.perform(MockMvcRequestBuilders.get("/edit-profile")).andExpect(MockMvcResultMatchers.view().name("edit-profile-form"));
    }
    //AC1
    @When("I hit the change password button")
    public void iHitTheChangePasswordButton() throws Exception {
        MOCK_MVC.perform(MockMvcRequestBuilders.get("/editPassword"));
    }
    @Given("I am on the change password form")
    public void IAmOnTheChangePasswordForm() throws Exception {
        MOCK_MVC.perform(MockMvcRequestBuilders.get("/editPassword"))
        .andExpect(MockMvcResultMatchers.view().name("/editPassword"));
    }
    //AC1
    @Then("a dedicated form is shown with three text fields: “old password”, “new password”, and “retype password”")
    public void aDedicatedFormIsShownWithThreeTextFieldsOldPasswordNewPasswordAndRetypePassword() throws Exception {
        MOCK_MVC.perform(MockMvcRequestBuilders.get("/profile/editPassword"));
        String responseContent = MOCK_MVC.perform(MockMvcRequestBuilders.get("/profile/editPassword")).andReturn().getResponse().getContentAsString();

        Assert.hasText(responseContent, "<input type=\"password\" id=\"currentPassword\" name=\"currentPassword\" />");

    }
    //AC2
    @When("I enter an old password <oldPassword> that does not match the password <passwordInFile> in file")
    public void iEnterAnOldPasswordThatDoesNotMatchThePasswordInFile() throws Exception {
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post("/editPassword")
                        .param("currentPassword",currentPassword)
                        .param("newPassword", newPassword)
                        .param("retypePassword", retypePassword)
        );
    }

    //
    //
    //New Tests:
    //
    //

    @io.cucumber.java.en.When("I enter a weak password")
    public void IEnterAWeakPassword(String weakPassword) {

    }
    @When("I enter two different new passwords: <newPassword> and <retypePassword>")
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
}
