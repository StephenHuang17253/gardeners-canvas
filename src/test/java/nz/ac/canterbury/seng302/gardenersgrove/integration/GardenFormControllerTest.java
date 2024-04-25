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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class GardenFormControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @MockBean
    private UserService userServiceMock;

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.now());


    @InjectMocks
    private static ProfileController profileController;


    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
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
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void mvcMockIsAlive() throws Exception
    {
        Mockito.when(userServiceMock.getUserByEmail("profile.user.test@ProfileController.com")).thenReturn(mockUser);
        mockMvc.perform(get("/create-new-garden"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void postProfilePage_LoggedIn_FilledPage() throws Exception
    {
        mockMvc.perform(post("/create-new-garden").with(csrf())
                        .flashAttr("gardenName","Hi")
                        .flashAttr("gardenLocation","Hi")
                        .flashAttr("gardenSize","123"))
                .andExpect(status().is3xxRedirection()).andDo(print());
    }



}
