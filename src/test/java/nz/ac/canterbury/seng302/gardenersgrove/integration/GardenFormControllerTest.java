package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class GardenFormControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @MockBean
    private GardenService gardenService;

    @Autowired
    UserService userService;

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.now());

    @InjectMocks
    private static GardenFormController gardenFormController;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        if (!userService.emailInUse(mockUser.getEmailAddress())) {
            userService.addUser(mockUser, "password");
        }

        Garden test_garden = new Garden(
                "test",
                "test",
                "test",
                "test",
                "test",
                "80",
                "test",
                10.0,
                false,
                mockUser

        );
        Optional<Garden> gardenOptional = Mockito.mock(Optional.class);
        Mockito.when(gardenOptional.get()).thenReturn(test_garden);
        when(gardenService.getGardenById(Mockito.anyLong())).thenReturn(gardenOptional);

    }

    @AfterAll
    public static void cleanup() {
        // Clear the context tests
        SecurityContextHolder.clearContext();
    }

    @Test
    public void controllerLoads() {
        assertNotNull(gardenFormController);
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void mvcMockIsAlive() throws Exception {
        mockMvc.perform(get("/create-new-garden"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postBasicNewGarden_AtLeastOneGardenAdded() throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "Hi")
                    .param("suburb", "Hi")
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postBasicGardenEdit_gardenEdited() throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "Hi")
                .param("suburb", "Hi")
                .param("city", "Hi")
                .param("country", "Hi")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    // Note on tests bellow, the tests bellow are paremeterised tests aiming to test
    // every edge case for each field
    // when creating or editing a garden. They are there in lots of 4 (per field)
    // and are in order:
    // 1. valid create 2. valid edit 3. invalid create 4. invalid edit
    // these tests check weather the controller will try to create/update a garden
    // with the given params.
    // if it doesn't the expectation is that some validation failed and the user is
    // re queried for innput.

    //
    // -------------------------------------------- Garden Name Parametrisation
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_gardenName(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", input)
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "Hi")
                    .param("suburb", "Hi")
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_gardenName(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", input)
                .param("gardenDescription", "Hi")
                .param("streetAddress", "Hi")
                .param("suburb", "Hi")
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_gardenName(String input) throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", input)
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "Hi")
                    .param("suburb", "Hi")
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_gardenName(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", input)
                .param("gardenDescription", "Hi")
                .param("streetAddress", "Hi")
                .param("suburb", "Hi")
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    //
    // -------------------------------------------- Description Parametrisation
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { " ", "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_description(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", input)
                    .param("streetAddress", "Hi")
                    .param("suburb", "Hi")
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_description(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", input)
                .param("streetAddress", "Hi")
                .param("suburb", "Hi")
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = {
        "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd",
        ";",
        "94385938754",
        "::",
        " ! "
})
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_description(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "input")
                    .param("gardenDescription", input)
                    .param("streetAddress", "Hi")
                    .param("suburb", "Hi")
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "dddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddd",
            ";",
            "94385938754",
            "::",
            " ! "
    })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_description(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "input")
                .param("gardenDescription", input)
                .param("streetAddress", "Hi")
                .param("suburb", "Hi")
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    //
    // -------------------------------------------- Street Address Parametrisation
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { " ", "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_streetAddress(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", input)
                    .param("suburb", "Hi")
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_StreetAddress(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", "Hi")
                .param("streetAddress", input)
                .param("suburb", "Hi")
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_streetAddress(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "input")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", input)
                    .param("suburb", "Hi")
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_streetAddress(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "input")
                .param("gardenDescription", "Hi")
                .param("streetAddress", input)
                .param("suburb", "Hi")
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    //
    // -------------------------------------------- Suburb Parametrisation
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { " ", "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_suburb(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", input)
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_suburb(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", input)
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_suburb(String input) throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "input")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", input)
                    .param("city", "Hi")
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_suburb(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "input")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", input)
                .param("city", "Hi")
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    //
    // -------------------------------------------- city Parameterization
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_city(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", input)
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_city(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", input)
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_city(String input) throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "input")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", input)
                    .param("country", "Hi")
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_city(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "input")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", input)
                .param("country", "Hi")
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    //
    // -------------------------------------------- country Parameterization
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_country(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", "input")
                    .param("country", input)
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "son", "basic input", "More name", "some-puntiuation ", "commas,",
            "full stops.", "Numbers ok 123", "apostrophee's", "some-mix's " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_country(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", "input")
                .param("country", input)
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_country(String input) throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "input")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", "input")
                    .param("country", input)
                    .param("gardenLocation", "My Home")
                    .param("postcode", "123")
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { " ", "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! " })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_country(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "input")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", "input")
                .param("country", input)
                .param("gardenLocation", "My Home")
                .param("postcode", "123")
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());
    }

    // Note: there is no parameterization on location,
    // this is because any user passed values for location are ignored
    //
    // -------------------------------------------- postCode Parameterization
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "son", " input", "More name", "123", "123 123", "12S 34E", "12345321" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_postCode(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", "input")
                    .param("country", "input")
                    .param("gardenLocation", "input")
                    .param("postcode", input)
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "", " ", "son", " input", "More name", "123", "123 123", "12S 34E", "12345321" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_postCode(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", "input")
                .param("country", "input")
                .param("gardenLocation", "input")
                .param("postcode", input)
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! ",
            "any.", "puntuation,", "is bad:", "{bracket?}", "(no)" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_postCode(String input) throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "input")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", "input")
                    .param("country", "input")
                    .param("gardenLocation", "input")
                    .param("postcode", input)
                    .param("gardenSize", "123"))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! ",
            "any.", "puntuation,", "is bad:", "{bracket?}", "(no)" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_postCode(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "input")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", "input")
                .param("country", "input")
                .param("gardenLocation", "input")
                .param("postcode", input)
                .param("gardenSize", "123")).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());
    }

    //
    // -------------------------------------------- gardenSize Parameterization
    // ---------------------------------------
    //

    @ParameterizedTest
    @ValueSource(strings = { "12345", "1.0", "1,0", "0.1", "123123.2", "8000000.00", "0.01" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_AtLeastOneGardenAdded_parameterisedOn_gardenSize(String input)
            throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "Hi")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", "input")
                    .param("country", "input")
                    .param("gardenLocation", "input")
                    .param("postcode", "123")
                    .param("gardenSize", input))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.times(1)).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "12345", "1.0", "1,0", "0.1", "123123.2", "0.01", "8000000,00" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_gardenEdited_parameterisedOn_gardenSize(String input)
            throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "Hi")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", "input")
                .param("country", "input")
                .param("gardenLocation", "input")
                .param("postcode", "123")
                .param("gardenSize", input)).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.times(1)).updateGarden(Mockito.anyLong(), Mockito.any());

    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! ",
            "son", "basic input", "More name", "123 123", "12S 34E",
            "1234531222222222212212312321331211222222222222222222222222222222222222222222222222222222222222222222222222",
            "1.2.3", "1,2.3", "-123.2", "-0.1", "-2.0",
            "any.", "puntuation,", "is bad:", "{bracket?}", "(no)", "0.009", "0", "8000000,01" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postNewGarden_NotAdded_parameterisedOn_gardenSize(String input) throws Exception {
        Garden mockGarden = Mockito.spy(Garden.class);
        when(mockGarden.getGardenId()).thenReturn(1L);

        // Below implementation (3 lines) is to mock the Garden class constructor when a
        // new garden is created
        // ref (Section 4):
        // https://www.baeldung.com/java-mockito-constructors-unit-testing
        MockedConstruction<Garden> mockGardenConstruction = null;
        try {
            mockGardenConstruction = Mockito.mockConstruction(Garden.class, (mock, context) -> {
                when(mock.getGardenId()).thenReturn(1L);
            });
            when(gardenService.getGardens()).thenReturn(new ArrayList<Garden>());

            mockMvc.perform(post("/create-new-garden").with(csrf())
                    .param("gardenName", "input")
                    .param("gardenDescription", "Hi")
                    .param("streetAddress", "input")
                    .param("suburb", "input")
                    .param("city", "input")
                    .param("country", "input")
                    .param("gardenLocation", "input")
                    .param("postcode", "123")
                    .param("gardenSize", input))
                    .andDo(MockMvcResultHandlers.print());
            Mockito.verify(gardenService, Mockito.never()).addGarden(Mockito.any());

        } catch (Exception err) {
            Assertions.fail("Constructor Mock failed: " + err.getMessage());
        } finally {
            // need to always kill the mock, otherwise the Garden Service tests will fail
            if (!(mockGardenConstruction == null)) {
                mockGardenConstruction.close();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "!", ";", "Surely•this", "{Null}", "@Value()", "::", " ! ",
            "son", "basic input", "More name", "123 123", "12S 34E",
            "1234531222222222212212312321331211222222222222222222222222222222222222222222222222222222222222222222222222",
            "1.2.3", "1,2.3", "-123.2", "-0.1", "-2.0",
            "any.", "puntuation,", "is bad:", "{bracket?}", "(no)", "0", "8000000,01" })
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void gardenFormController_postGardenEdit_NotAdded_parameterisedOn_gardenSize(String input) throws Exception {

        mockMvc.perform(post("/my-gardens/123/edit").with(csrf())
                .param("gardenName", "input")
                .param("gardenDescription", "Hi")
                .param("streetAddress", "input")
                .param("suburb", "input")
                .param("city", "input")
                .param("country", "input")
                .param("gardenLocation", "input")
                .param("postcode", "123")
                .param("gardenSize", input)).andDo(MockMvcResultHandlers.print());
        Mockito.verify(gardenService, Mockito.never()).updateGarden(Mockito.anyLong(), Mockito.any());
    }

}
