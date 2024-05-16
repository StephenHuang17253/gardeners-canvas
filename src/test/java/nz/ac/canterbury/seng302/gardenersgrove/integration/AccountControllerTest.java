package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.AccountController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.TokenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.Authentication;

import jakarta.servlet.http.HttpSession;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private static SecurityContext securityContextMock;

    @MockBean
    private static AuthenticationManager authenticationManagerMock;

    @MockBean
    private static UserService userServiceMock;

    @MockBean
    private static GardenService gardenServiceMock;

    @MockBean
    private static TokenService tokenServiceMock;

    @MockBean
    private static EmailService emailServiceMock;

    private static AccountController accountController;

    private static String verifiedEmail = "verifiedEmail@gmail.com";

    private static String unverifiedEmail = "unVerifiedEmail@gmail.com";

    private static String userPassword = "ValidPa33w0rd!";

    @BeforeAll
    public static void setup() {
        securityContextMock = spy(SecurityContext.class);
        SecurityContextHolder.setContext(securityContextMock);
        accountController = spy(new AccountController(userServiceMock, authenticationManagerMock, emailServiceMock,
                tokenServiceMock, gardenServiceMock));
        doNothing().when(accountController).setSecurityContext(any(String.class), any(String.class),
                any(HttpSession.class));
    }

    @Test
    public void controllerLoads() {
        assertNotNull(accountController);
    }

    @Test
    public void mvcMockIsAlive() throws Exception {
        this.mockMvc.perform(get("/register")).andExpect(status().isOk());
    }

    @Test
    public void getRegistrationPage_NoParams_NonFilledPage() throws Exception {
        this.mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", ""))
                .andExpect(model().attribute("noLastName", false))
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", ""))
                .andExpect(model().attribute("repeatPassword", ""));
    }

    @Test
    public void getRegistrationPage_FirstName_PageWFirstName() throws Exception {
        this.mockMvc.perform(get("/register").param("firstName", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName", "123"))
                .andExpect(model().attribute("lastName", ""))
                .andExpect(model().attribute("noLastName", false))
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", ""))
                .andExpect(model().attribute("repeatPassword", ""));
    }

    @Test

    public void getRegistrationPage_LastName_PageWLastName() throws Exception {
        this.mockMvc.perform(get("/register").param("lastName", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", "123"))
                .andExpect(model().attribute("noLastName", false))
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", ""))
                .andExpect(model().attribute("repeatPassword", ""));
    }

    @Test
    public void getRegistrationPage_NoLastName_PageWNoLastName() throws Exception {
        this.mockMvc.perform(get("/register").param("noLastName", "true"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", ""))
                .andExpect(model().attribute("noLastName", true))
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", ""))
                .andExpect(model().attribute("repeatPassword", ""));
    }

    @Test
    public void getRegistrationPage_Email_PageWEmail() throws Exception {
        this.mockMvc.perform(get("/register").param("emailAddress", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", ""))
                .andExpect(model().attribute("noLastName", false))
                .andExpect(model().attribute("emailAddress", "123"))
                .andExpect(model().attribute("password", ""))
                .andExpect(model().attribute("repeatPassword", ""));
    }

    @Test
    public void getRegistrationPage_Password_PageWPassword() throws Exception {
        this.mockMvc.perform(get("/register").param("password", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", ""))
                .andExpect(model().attribute("noLastName", false))
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", "123"))
                .andExpect(model().attribute("repeatPassword", ""));
    }

    @Test
    public void getRegistrationPage_RepeatPassword_PageWRepeatPassword() throws Exception {
        this.mockMvc.perform(get("/register").param("repeatPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("firstName", ""))
                .andExpect(model().attribute("lastName", ""))
                .andExpect(model().attribute("noLastName", false))
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", ""))
                .andExpect(model().attribute("repeatPassword", "123"));
    }

    @Test
    public void getLoginPage_NoParams_NonFilledPage() throws Exception {
        this.mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", ""));
    }

    @Test
    public void getLoginPage_EmailAddress_PageWEmailAddress() throws Exception {
        this.mockMvc.perform(get("/login").param("emailAddress", "testEmail"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emailAddress", "testEmail"))
                .andExpect(model().attribute("password", ""));
    }

    @Test
    public void getLoginPage_Password_PageWPassword() throws Exception {
        this.mockMvc.perform(get("/login").param("password", "testPassword"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emailAddress", ""))
                .andExpect(model().attribute("password", "testPassword"));
    }

    @Test
    public void postLogin_InvalidEmailAddress_AddEmailAddressErrorText() throws Exception {
        MvcResult result = this.mockMvc.perform(
                post("/login")
                        .with(csrf()).param("emailAddress", "testEmail").param("password", ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emailAddress", "testEmail"))
                .andExpect(model().attribute("password", ""))
                .andReturn();

        // Check that the email error message is correct, was not working as a
        // .andExpect so saved result and checked with string.equals(anotherString)
        ModelAndView modelAndView = result.getModelAndView();

        if (modelAndView == null)
            return;

        Map<String, Object> model = modelAndView.getModel();

        String error = model.get("emailAddressError").toString();

        assertTrue("Email must be in the form 'jane@doe.nz'".equals(error));
    }

    @Test
    public void postLogin_InvalidPassword_AddLoginErrorText() throws Exception {
        MvcResult result = this.mockMvc.perform(
                post("/login").with(csrf()).param("emailAddress", "unUsedTestEmail@gmail.com").param("password",
                        ""))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emailAddress", "unUsedTestEmail@gmail.com"))
                .andExpect(model().attribute("password", ""))
                .andReturn();

        // Check that the login error message is correct, was not working as a
        // .andExpect so saved result and checked with string.equals(anotherString)
        ModelAndView modelAndView = result.getModelAndView();

        if (modelAndView == null)
            return;

        Map<String, Object> model = modelAndView.getModel();

        String error = model.get("loginError").toString();

        assertTrue("The email address is unknown, or the password is invalid".equals(error));
    }

    @Test
    public void postLogin_UserDoesNotExist_AddLoginErrorText() throws Exception {

        MvcResult result = this.mockMvc.perform(
                post("/login").with(csrf()).param("emailAddress", "unUsedTestEmail@gmail.com").param("password",
                        "ValidPa33w0rd!"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("emailAddress", "unUsedTestEmail@gmail.com"))
                .andExpect(model().attribute("password", "ValidPa33w0rd!"))
                .andReturn();

        // Check that the login error message is correct, was not working as a
        // .andExpect so saved result and checked with string.equals(anotherString)
        ModelAndView modelAndView = result.getModelAndView();

        if (modelAndView == null)
            return;

        Map<String, Object> model = modelAndView.getModel();

        String error = model.get("loginError").toString();

        assertTrue("The email address is unknown, or the password is invalid".equals(error));
    }

    @Test
    public void postLogin_UserExistsAndIsVerified_RedirectToHome() throws Exception {

        User verifiedMockUser = spy(new User("verifiedFirstName", "verifiedLastName", verifiedEmail, null));
        verifiedMockUser.setVerified(true);
        verifiedMockUser.setPassword(userPassword);
        when(verifiedMockUser.getId()).thenReturn(1L);

        when(userServiceMock.getUserByEmailAndPassword(verifiedEmail, userPassword)).thenReturn(verifiedMockUser);
        when(userServiceMock.getUserByEmail(any(String.class))).thenReturn(verifiedMockUser);

        when(tokenServiceMock.getTokenByUser(verifiedMockUser)).thenReturn(null);

        Authentication authenticationMock = Mockito.mock(Authentication.class);

        when(authenticationMock.isAuthenticated()).thenReturn(false);

        when(authenticationManagerMock.authenticate(any())).thenReturn(authenticationMock);

        this.mockMvc.perform(
                post("/login").with(csrf()).param("emailAddress", verifiedEmail).param("password", userPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home"))
                .andReturn();
    }

    @Test
    public void postLogin_UserExistsAndIsNotVerified_RedirectToVerify() throws Exception {

        User unverifiedMockUser = spy(new User("unverifiedFirstName", "unverifiedLastName", unverifiedEmail, null));
        unverifiedMockUser.setPassword(userPassword);
        when(unverifiedMockUser.getId()).thenReturn(1L);

        when(userServiceMock.getUserByEmailAndPassword(unverifiedEmail,
                userPassword)).thenReturn(unverifiedMockUser);
        when(userServiceMock.getUserByEmail(unverifiedEmail)).thenReturn(unverifiedMockUser);

        Token token = new Token(unverifiedMockUser, null);

        when(tokenServiceMock.getTokenByUser(unverifiedMockUser)).thenReturn(token);

        Authentication authenticationMock = Mockito.mock(Authentication.class);

        when(authenticationMock.isAuthenticated()).thenReturn(false);

        when(authenticationManagerMock.authenticate(any())).thenReturn(authenticationMock);

        this.mockMvc.perform(
                post("/login").with(csrf()).param("emailAddress", unverifiedEmail).param("password", userPassword))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/verify/" + unverifiedEmail));
    }
}
