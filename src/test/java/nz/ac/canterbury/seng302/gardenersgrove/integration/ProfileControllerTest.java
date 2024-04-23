package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.multipart.MultipartFile;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userServiceMock;

    @MockBean
    private FileService fileServiceMock;


    @InjectMocks
    private static ProfileController profileController;

    // Method to create a RequestPostProcessor that adds the security context to the session
    private RequestPostProcessor userAuthenticated() {
        return mockRequest -> {
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken("sally@email.com", "Trial@10!", Collections.emptyList());

            // Create and set a SecurityContext for the mock request
            SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
            securityContext.setAuthentication(auth);
            mockRequest.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

            return mockRequest;
        };
    }

    @BeforeEach
    public void setup() {
        //Setup an mock user
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
        LocalDate date = LocalDate.parse("12/12/2001", formatter);
        User mockUser = new User("Sally", "Doe", "sally@email.com", date);
        mockUser.setPassword("sally@email.com");

        //return mock user when certain methods are called
        Mockito.when(userServiceMock.getUserByEmail("sally@email.com")).thenReturn(mockUser);
        Mockito.when(userServiceMock.getUserByEmailAndPassword("sally@email.com", "Trial@10!")).thenReturn(mockUser);
        Mockito.when(userServiceMock.getUserByEmailAndPassword("sally@email.com", mockUser.getEncodedPassword())).thenReturn(mockUser);

    }

    @AfterAll
    public static void cleanup() {
        // Clear the context tests
        SecurityContextHolder.clearContext();
    }

    @Test
    public void controllerLoads()
    {
        assertNotNull(profileController);
    }

    @Test
    public void mvcMockIsAlive() throws Exception
    {
        mockMvc.perform(get("/profile").with(userAuthenticated()))
                .andExpect(status().isOk());
    }

    @Test
    public void getProfilePage_notAuthenticated_Forbidden() throws Exception
    {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getProfilePage_LoggedIn_FilledPage() throws Exception
    {
        // Todo: Fix date format on site such that this test passes
        // test is currently correct but fails because format on site is 2001-12-12 (YYYYY-MM-DD)
        mockMvc.perform(get("/profile").with(userAuthenticated()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userName","Sally Doe"))
                .andExpect(model().attribute("dateOfBirth","12/12/2001"))
                .andExpect(model().attribute("emailAddress","sally@email.com"));
    }

    // Todo Currently the two test below here do not pass because the server response is forbidden
    @Test
    public void postProfilePage_LoggedIn_FilledPage() throws Exception
    {
        MultipartFile newProfilePhotoMock = Mockito.mock(MultipartFile.class);

        mockMvc.perform(post("/profile").with(userAuthenticated()).flashAttr("profilePictureInput", newProfilePhotoMock))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    public void postProfileEditPage_Empty_Ok() throws Exception
    {
        mockMvc.perform(post("/profile/edit").with(userAuthenticated()))
                .andExpect(status().is3xxRedirection());
    }


}
