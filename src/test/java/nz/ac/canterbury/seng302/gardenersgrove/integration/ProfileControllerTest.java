package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ProfileControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @MockBean
    private UserService userServiceMock;

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.of(2003,5,2));
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
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk());
    }

    @Test
    public void getProfilePage_notAuthenticated_Forbidden() throws Exception
    {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void getProfilePage_LoggedIn_FilledPage() throws Exception
    {
        Mockito.when(userServiceMock.getUserByEmail("profile.user.test@ProfileController.com")).thenReturn(mockUser);
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("userName","John Test"))
                .andExpect(model().attribute("dateOfBirth", "02/05/2003"))
                .andExpect(model().attribute("emailAddress","profile.user.test@profilecontroller.com"));
    }


    // Add tests for posting picture method
    // useful reference: https://stackoverflow.com/questions/38571716/how-to-put-multipart-form-data-using-spring-mockmvc


}
