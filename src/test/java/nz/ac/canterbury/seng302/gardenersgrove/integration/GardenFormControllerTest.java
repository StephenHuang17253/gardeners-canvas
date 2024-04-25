package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.ProfileController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
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

    @MockBean
    private GardenService gardenService;

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.now());


    @InjectMocks
    private static GardenFormController gardenFormController;


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
        assertNotNull(gardenFormController);
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void mvcMockIsAlive() throws Exception
    {
        mockMvc.perform(get("/create-new-garden"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postBasicNewGarden_AtLeastOneGardenAdded() throws Exception
    {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a new garden is created
        // ref (Section 4): https://www.baeldung.com/java-mockito-constructors-unit-testing
        try {
            Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
        }
        catch (Exception err)
        {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        }
        when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

        mockMvc.perform(post("/create-new-garden").with(csrf())
                        .param("gardenName","Hi")
                        .param("gardenLocation","Hi")
                        .param("gardenSize","123"))
                .andExpect(status().is3xxRedirection()).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.atLeastOnce()).addGarden(Mockito.any());
    }



}
