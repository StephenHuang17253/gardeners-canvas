package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nz.ac.canterbury.seng302.gardenersgrove.controller.AccountController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ResetPasswordController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
public class ResetUserPassword {

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

    @Autowired
    public GardenService gardenService;

    @Autowired
    private TokenService tokenService;

    public static MockMvc mockMVC;

    @Autowired
    public UserService userService;

    @Autowired
    public SecurityService securityService;

    private MvcResult resetPasswordResult;

    private Token token;

    User loggedInUser;

    String currentPassword;
    String userEmail;
    String origHash;

    private String resetLink;

    private EmailService emailService;

    private TemplateEngine templateEngine;

    private JavaMailSender mailSender;

    @Before
    public void before_or_after_all() throws MessagingException {
        token = new Token(loggedInUser, null);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);

        emailService = mock(EmailService.class);

        ProfileController profileController = new ProfileController(authenticationManager, userService,
                fileService,
                emailService, securityService);
        AccountController accountController = new AccountController(userService, authenticationManager,
                emailService,
                tokenService, gardenService, securityService);
        ResetPasswordController resetPasswordController = new ResetPasswordController(userService, tokenService,
                emailService);
        mockMVC = MockMvcBuilders.standaloneSetup(accountController, resetPasswordController, profileController)
                .build();
        tokenService.addToken(token);

        mailSender = mock(JavaMailSender.class);
        doNothing().when(mailSender).send(Mockito.any(MimeMessage.class));

        templateEngine = mock(TemplateEngine.class);
        when(templateEngine.process(Mockito.anyString(), Mockito.any(Context.class))).thenReturn("Test Body");

        emailService = new EmailService(mailSender, templateEngine);
        emailService = spy(emailService);
        doNothing().when(emailService).sendHTMLEmail(anyString(), anyString(), anyString(), any(Context.class));
        when(emailService.getBaseURL()).thenReturn("");
    }

    @Given("I am on the login page")
    public void i_am_on_the_login_page() throws Exception {
        SecurityContextHolder.clearContext();
        String url = "/login";
        mockMVC.perform(
                MockMvcRequestBuilders
                        .get(url))
                .andExpect(status().isOk()).andReturn();
    }

    @When("I hit the “Forgot your password?” link")
    public void i_hit_the_forgot_your_password_link() throws Exception {
        String url = "/lost-password";
        mockMVC.perform(
                MockMvcRequestBuilders
                        .get(url))
                .andExpect(status().isOk()).andReturn();
    }

    @Then("I see a form asking me for my email address")
    public void i_see_a_form_asking_me_for_my_email_address() throws Exception {
        String url = "/lost-password";
        resetPasswordResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .get(url))
                .andExpect(status().isOk()).andReturn();
        ModelAndView model = resetPasswordResult.getModelAndView();
        Assertions.assertNotNull(model);
        ModelMap modelMap = model.getModelMap();
        assertNotNull("emailAddress attribute exists", modelMap.get("emailAddress"));
    }

    @Given("I am on the lost password form")
    public void i_am_on_the_lost_password_form() throws Exception {
        String url = "/lost-password";
        mockMVC.perform(
                MockMvcRequestBuilders
                        .get(url))
                .andExpect(status().isOk()).andReturn();
    }

    @When("I enter an empty or malformed email address {string}")
    public void i_enter_an_empty_or_malformed_email_address(String email) {
        userEmail = email;
    }

    @And("I click the submit button")
    public void i_click_the_submit_button() throws Exception {
        String url = "/lost-password";
        resetPasswordResult = mockMVC.perform(
                MockMvcRequestBuilders.post(url)
                        .param("emailAddress", userEmail))
                .andExpect(status().isOk())
                .andReturn();

        Mockito.verify(emailService, times(0)).sendHTMLEmail(anyString(), anyString(), anyString(),
                any(Context.class));
    }

    @Then("an error message tells me {string}")
    public void an_error_message_tells_me(String errorMessage) {
        ModelAndView model = resetPasswordResult.getModelAndView();
        Assertions.assertNotNull(model);
        ModelMap modelMap = model.getModelMap();
        Object errorObject = modelMap.get("emailError");
        String givenErrorMessage = errorObject.toString();
        Assertions.assertEquals(errorMessage, givenErrorMessage, "Error message match");
    }

    @When("I enter a valid email {string} that is not known to the system")
    public void i_enter_a_valid_email_that_is_not_known_to_the_system(String email) {
        userEmail = email;
    }

    @Then("a confirmation message tells me {string}")
    public void a_confirmation_message_tells_me(String message) {
        ModelAndView model = resetPasswordResult.getModelAndView();
        Assertions.assertNotNull(model);
        ModelMap modelMap = model.getModelMap();
        Object messageObject = modelMap.get("message");
        String givenMessage = messageObject.toString();
        Assertions.assertEquals(message, givenMessage, "Message match");
    }

    @When("I enter an email {string} that is known to the system")
    public void i_enter_an_email_that_is_known_to_the_system(String email) {
        userEmail = email;
    }

    @And("an email is sent with a link containing a unique reset token")
    public void an_email_is_sent_with_a_link_containing_a_unique_reset_token() {
        ModelAndView model = resetPasswordResult.getModelAndView();
        Assertions.assertNotNull(model);
        ModelMap modelMap = model.getModelMap();
        assertNotNull("emailSent attribute exists", modelMap.get("emailSent"));
    }

    @Given("I go to the received email in the email {string}")
    public void i_go_to_the_received_email_in_my_email(String email) throws Exception {
        loggedInUser = userService.getUserByEmail(email);
        token = new Token(loggedInUser, null);
        tokenService.addToken(token);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        doNothing().when(emailService).sendHTMLEmail(anyString(), anyString(), anyString(),
                contextCaptor.capture());

        emailService.sendResetPasswordEmail(token);

        Context context = contextCaptor.getValue();
        // Sets the resetLink to the url variable captured
        resetLink = (String) context.getVariable("url");
    }

    @When("I click the clickable link in the email")
    public void i_click_the_clickable_link_in_the_email() throws Exception {
        // Simulates clicking because it parses the link from the sent email directly
        // into the request
        resetPasswordResult = mockMVC.perform(MockMvcRequestBuilders.get(resetLink))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I am taken to the reset password form")
    public void i_am_taken_to_the_reset_password_form() throws Exception {
        // Verifies the page the resetLink takes you to is the reset password form
        resetPasswordResult = mockMVC.perform(MockMvcRequestBuilders.get(resetLink))
                .andExpect(status().isOk())
                .andExpect(view().name("resetPasswordPage"))
                .andReturn();
    }

    @And("I as user {string} with password {string} am on the reset password form")
    public void i_as_user_with_password_am_on_the_reset_password_form(String email, String password)
            throws Exception {
        loggedInUser = userService.getUserByEmail(email);
        currentPassword = password;
        origHash = loggedInUser.getEncodedPassword();
        userEmail = loggedInUser.getEmailAddress();
        token = new Token(loggedInUser, null);
        tokenService.addToken(token);

        String url = "/reset-password/" + token.getTokenString();
        mockMVC.perform(
                MockMvcRequestBuilders
                        .get(url))
                .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @When("I enter two different passwords {string}, {string}")
    public void i_enter_two_different_passwords(String newPassword, String retypePassword) throws Exception {
        String url = "/reset-password/" + token.getTokenString();
        resetPasswordResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .post(url)
                        .param("password", newPassword)
                        .param("retypePassword", retypePassword))
                .andReturn();
    }

    @When("I enter a weak password {string}")
    public void i_enter_a_weak_password(String weakPassword) throws Exception {
        String url = "/reset-password/" + token.getTokenString();
        resetPasswordResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .post(url)
                        .param("password", weakPassword)
                        .param("retypePassword", weakPassword))
                .andReturn();
    }

    @Then("My password does not get updated")
    public void my_password_does_not_get_updated() {
        Assertions.assertEquals(origHash, userService.getUserByEmail(userEmail).getEncodedPassword());
    }

    @When("I enter {string} in both new and retype fields and hit the save button")
    public void i_enter_in_both_new_and_retype_fields_and_hit_the_save_button(String password) throws Exception {
        String url = "/reset-password/" + token.getTokenString();
        resetPasswordResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .post(url)
                        .param("password", password)
                        .param("retypePassword", password))
                .andReturn();
    }

    @Then("my password is updated")
    public void my_password_is_updated() {
        Assertions.assertNotEquals(origHash, userService.getUserByEmail(userEmail).getEncodedPassword());
    }

    @And("I am redirected to the login page")
    public void i_am_redirected_to_the_login_page() {
        String url = "/login";
        String redirectedUrl = resetPasswordResult.getResponse().getRedirectedUrl();
        Assertions.assertEquals(String.format(url), redirectedUrl);
    }
}
