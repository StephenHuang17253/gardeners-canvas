package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        ProfileController profileController = new ProfileController(authenticationManager, userService, fileService, emailService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(profileController).build();

    }

    @Given("I am on the login page")
    public void iAmOnTheLoginPage() {
    }

    @When("I hit the “Forgot your password?” link")
    public void iHitTheForgotYourPasswordLink() {
    }

    @Then("I see a form asking me for my email address")
    public void iSeeAFormAskingMeForMyEmailAddress() {
    }

    @Given("I am on the lost password form")
    public void iAmOnTheLostPasswordForm() {
    }

    @When("I enter an empty or malformed email address and click {string}")
    public void iEnterAnEmptyOrMalformedEmailAddressAndClick(String arg0) {
    }

    @Then("an error message tells me {string}")
    public void anErrorMessageTellsMe(String arg0) {
    }

    @When("I enter a valid email that is not known to the system and click {string}")
    public void iEnterAValidEmailThatIsNotKnownToTheSystemAndClick(String arg0) {
    }

    @Then("a confirmation message tells me {string}")
    public void aConfirmationMessageTellsMe(String arg0) {
    }

    @When("I enter an email that is known to the system and click {string}")
    public void iEnterAnEmailThatIsKnownToTheSystemAndClick(String arg0) {
    }

    @And("an email is sent to the email address with a link containing a unique reset token")
    public void anEmailIsSentToTheEmailAddressWithALinkContainingAUniqueResetToken() {
    }

    @Given("I received an email to reset my password")
    public void iReceivedAnEmailToResetMyPassword() {
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
