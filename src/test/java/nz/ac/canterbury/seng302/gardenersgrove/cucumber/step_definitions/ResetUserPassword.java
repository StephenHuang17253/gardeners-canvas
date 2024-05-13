package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AccountController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ResetPasswordController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class ResetUserPassword {

    @Autowired
    public AuthenticationManager authenticationManager;
    @Autowired
    public PasswordEncoder passwordEncoder;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public FileService fileService;
    @Autowired
    public EmailService emailService;
    @Autowired
    public GardenService gardenService;
    @Autowired
    private TokenService tokenService;

    public static MockMvc MOCK_MVC;
    @Autowired
    public UserService userService;
    private MvcResult resetPasswordResult;

    private Token token;

    User loggedInUser;

    String currentPassword;
    String origHash;
    String userEmail;

    @Before
    public void before_or_after_all() {
        token = new Token(loggedInUser, null);
        userService = new UserService(passwordEncoder, userRepository);
        ProfileController profileController = new ProfileController(authenticationManager, userService, fileService, emailService);
        AccountController accountController = new AccountController(userService, authenticationManager, emailService, tokenService,gardenService);
        ResetPasswordController resetPasswordController = new ResetPasswordController(userService, tokenService, emailService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(accountController, resetPasswordController, profileController).build();
        tokenService.addToken(token);
    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() throws Exception {
        String url = "/login";
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(url)
        ).andExpect(status().isOk()).andReturn();

    }

    @When("I hit the “Forgot your password?” link")
    public void iHitTheForgotYourPasswordLink() throws Exception {
        String url = "/lost-password";
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(url)
        ).andExpect(status().isOk()).andReturn();
    }

    @Then("I see a form asking me for my email address")
    public void iSeeAFormAskingMeForMyEmailAddress() throws Exception {
        String url = "/lost-password";
        resetPasswordResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(url)

        ).andExpect(status().isOk()).andReturn();
        ModelMap modelMap = resetPasswordResult.getModelAndView().getModelMap();

        assertNotNull("emailAddress attribute exists", modelMap.get("emailAddress"));
    }


    @Given("I am on the lost password form")
    public void iAmOnTheLostPasswordForm() throws Exception {
        String url = "/lost-password";
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(url)
        ).andExpect(status().isOk()).andReturn();
    }

    @When("I enter an empty or malformed email address {string}")
    public void iEnterAnEmptyOrMalformedEmailAddress(String email) {
        userEmail = email;
    }

    @And("I click the submit button")
    public void iClickTheSubmitButton() throws Exception {
        String url = "/lost-password";
        resetPasswordResult = MOCK_MVC.perform(
                MockMvcRequestBuilders.post(url)
                        .param("email", userEmail))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("an error message tells me {string}")
    public void anErrorMessageTellsMe(String errorMessage) throws Exception {
        ModelMap modelMap = resetPasswordResult.getModelAndView().getModelMap();
        Object errorObject = modelMap.get("emailError");
        String givenErrorMessage = errorObject.toString();
        Assertions.assertEquals(errorMessage, givenErrorMessage, "Error message match");
    }

    @When("I enter a valid email {string} that is not known to the system")
    public void iEnterAValidEmailThatIsNotKnownToTheSystem(String email) {
        userEmail = email;
    }

    @Then("a confirmation message tells me {string}")
    public void aConfirmationMessageTellsMe(String message) {
        ModelMap modelMap = resetPasswordResult.getModelAndView().getModelMap();
        Object messageObject = modelMap.get("message");
        String givenMessage = messageObject.toString();
        Assertions.assertEquals(message, givenMessage, "Message match");
    }

    @When("I enter an email {string} that is known to the system")
    public void iEnterAnEmailThatIsKnownToTheSystem(String email) {
        userEmail = email;
    }

    @And("an email is sent with a link containing a unique reset token")
    public void anEmailIsSentToTheEmailAddressWithALinkContainingAUniqueResetToken() {
        ModelMap modelMap = resetPasswordResult.getModelAndView().getModelMap();
        assertNotNull("emailSent attribute exists", modelMap.get("emailSent"));
    }

    @Given("I received an email to reset my password")
    public void iReceivedAnEmailToResetMyPassword() throws Exception {
        String url = "/reset-password/{token}";
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(url)
        ).andExpect(status().isOk()).andReturn();
    }

    @When("I go to the given URL passed in the email")
    public void iGoToTheGivenURLPassedInTheEmail() {
    }

    @Then("I am taken to the reset password form")
    public void iAmTakenToTheResetPasswordForm() {
    }

    @Given("I am on the reset password form")
    public void iAmOnTheResetPasswordForm() {
    }

    @When("I enter two different passwords in {string} and {string} fields and hit the save button")
    public void iEnterTwoDifferentPasswordsInAndFieldsAndHitTheSaveButton(String arg0, String arg1) {
    }

    @When("I enter a weak password and hit the save button")
    public void iEnterAWeakPasswordAndHitTheSaveButton() {
    }

    @When("I enter fully compliant details and hit the save button")
    public void iEnterFullyCompliantDetailsAndHitTheSaveButton() {
    }

    @Then("my password is updated")
    public void myPasswordIsUpdated() {
    }

    @And("I am redirected to the login page")
    public void iAmRedirectedToTheLoginPage() {
    }

}
