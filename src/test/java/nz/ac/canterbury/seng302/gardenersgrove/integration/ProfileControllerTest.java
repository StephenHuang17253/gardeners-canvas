package nz.ac.canterbury.seng302.gardenersgrove.integration;

import java.time.LocalDate;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ProfileControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @MockBean
    private UserService userServiceMock;

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.of(2003, 5, 2));
    @InjectMocks
    private static ProfileController profileController;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

    }

    @AfterAll
    static void cleanup() {
        // Clear the context tests
        SecurityContextHolder.clearContext();
    }

    @Test
    void controllerLoads() {
        assertNotNull(profileController);
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    void mvcMockIsAlive() throws Exception {
        Mockito.when(userServiceMock.getUserByEmail("profile.user.test@ProfileController.com")).thenReturn(mockUser);
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Test
    void getProfilePage_notAuthenticated_Forbidden() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    void getProfilePage_LoggedIn_FilledPage() throws Exception {
        Mockito.when(userServiceMock.getUserByEmail("profile.user.test@ProfileController.com")).thenReturn(mockUser);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userName", "John Test"))
                .andExpect(model().attribute("dateOfBirth", "02/05/2003"))
                .andExpect(model().attribute("emailAddress", "profile.user.test@ProfileController.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "a", "lowercase", "UPPERCASE", "1Comma,",
            "UsersNameIsJohnSoThisShouldntWork!1", "UsersLastNameIsTest1!",
            "UsersEmailIsprofile.user.test@ProfileController.com1",
            "UsersDOBis2003-05-02!", "1backslash/" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    void testUpdatePasswordFailCases(String input) throws Exception {
        String currentPassword = "currentPassword1!";
        mockUser.setPassword(currentPassword);
        mockUser.setId(1L);
        Mockito.when(userServiceMock.getUserByEmail("profile.user.test@ProfileController.com")).thenReturn(mockUser);
        Mockito.when(userServiceMock.checkPassword(1L, currentPassword)).thenReturn(true);
        mockMvc.perform(post("/profile/change-password").with(csrf())
                .param("currentPassword", currentPassword)
                .param("newPassword", input)
                .param("retypePassword", input))
                .andDo(MockMvcResultHandlers.print());
        Mockito.verify(userServiceMock, Mockito.never()).updatePassword(1L, input);
    }

    @ParameterizedTest
    @ValueSource(strings = { "Password1!", "ThisPasswordShouldWork!1", "123456789Aa!", "JonTes123!", "2002-02-03!aA",
            "1Exclamationmark!",
            "1Fullstop.", "1Apostrophe'", "1Question?", "1Atsymbol@", "1Hashtag#", "1Moneysign$",
            "1Percent%", "1Uparrow^", "1Andsymbol&", "1Starsymbol*", "1Bracket(", "1Hyphen-", "1Underscore_" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    void testUpdatePasswordPassCases(String input) throws Exception {
        String currentPassword = "currentPassword1!";
        mockUser.setPassword(currentPassword);
        mockUser.setId(1L);
        Mockito.when(userServiceMock.getUserByEmail("profile.user.test@ProfileController.com")).thenReturn(mockUser);
        Mockito.when(userServiceMock.checkPassword(1L, currentPassword)).thenReturn(true);
        mockMvc.perform(post("/profile/change-password").with(csrf())
                .param("currentPassword", currentPassword)
                .param("newPassword", input)
                .param("retypePassword", input));
        Mockito.verify(userServiceMock, Mockito.times(1)).updatePassword(1L, input);
    }

}
