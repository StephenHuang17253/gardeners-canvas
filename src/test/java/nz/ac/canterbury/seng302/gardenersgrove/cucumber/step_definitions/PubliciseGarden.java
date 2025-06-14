package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRelationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenTagRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
public class PubliciseGarden {

    public static MockMvc mockMVCMyGarden;

    public static MockMvc mockMVCGardenForm;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public GardenTagRepository gardenTagRepository;

    @Autowired
    public GardenTagRelationRepository gardenTagRelationRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public SecurityService securityService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private GardenTagService gardenTagService;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private WeatherService weatherService;

    private static GardenService gardenService;

    private static UserService userService;

    private static PlantService plantService;

    private ProfanityService profanityService;

    private User user;

    private Garden userGarden;

    private Garden newGarden;

    private String description;

    private MvcResult createGardenResult;

    @Before
    public void before_or_after_all() {
        profanityService = Mockito.mock(ProfanityService.class);
        weatherService = Mockito.mock(WeatherService.class);

        Mockito.when(profanityService.containsProfanity(Mockito.anyString(), Mockito.any())).thenReturn(false);

        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gardenService = new GardenService(gardenRepository, userService);
        GardensController myGardensController = new GardensController(gardenService, securityService, plantService,
                weatherService, objectMapper, gardenTagService, profanityService);
        GardenFormController gardenFormController = new GardenFormController(gardenService, locationService,
                securityService);

        mockMVCMyGarden = MockMvcBuilders.standaloneSetup(myGardensController).build();
        mockMVCGardenForm = MockMvcBuilders.standaloneSetup(gardenFormController).build();
    }

    @Given("User {string} is on my garden details page for {string}")
    public void userIsOnMyGardenDetailsPageFor(String userEmail, String garden) {
        user = userService.getUserByEmail(userEmail);
        userGarden = user.getGardens().get(0);
        Assertions.assertEquals(garden, userGarden.getGardenName());
    }

    @When("I mark a checkbox labelled \"Make my garden public\"")
    public void iMarkACheckboxLabelledMakeMyGardenPublic() throws Exception {
        String myGardenUrl = String.format("/my-gardens/%d/public", userGarden.getGardenId());
        mockMVCMyGarden.perform(
                MockMvcRequestBuilders
                        .post(myGardenUrl)
                        .param("makeGardenPublic", String.valueOf(true))
                        .with(csrf()))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Then("My garden will be visible in search results")
    public void myGardenWillBeVisibleInSearchResults() {
        Optional<Garden> testGarden = gardenService.getGardenById(userGarden.getGardenId());
        Assertions.assertTrue(testGarden.get().getIsPublic());
    }

    @When("I add an optional description")
    public void iAddAnOptionalDescription() {
        description = "is a vegetable patch";
    }

    @When("I remove the description of the garden")
    public void iRemoveTheDescriptionOfTheGarden() {
        description = "";
    }

    @And("I am creating a new garden {string}")
    public void iAmCreatingANewGarden(String gardenName) throws Exception {
        createGardenResult = mockMVCGardenForm.perform(
                MockMvcRequestBuilders
                        .post("/create-new-garden")
                        .param("gardenName", gardenName)
                        .param("gardenDescription", description)
                        .param("streetAddress", "")
                        .param("suburb", "")
                        .param("city", "Christchurch")
                        .param("country", "New Zealand")
                        .param("postcode", "")
                        .param("gardenSize", "")
                        .param("latitude", "-43.5214643")
                        .param("longitude", "172.5796159")
                        .with(csrf()))
                .andReturn();
    }

    @And("I am editing an existing garden")
    public void iAmEditingAnExistingGarden() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d/edit", userGarden.getGardenId());
        mockMVCGardenForm.perform(
                MockMvcRequestBuilders
                        .post(gardenUrl)
                        .param("gardenName", userGarden.getGardenName())
                        .param("gardenDescription", description)
                        .param("streetAddress", userGarden.getGardenAddress())
                        .param("suburb", userGarden.getGardenSuburb())
                        .param("city", userGarden.getGardenCity())
                        .param("country", userGarden.getGardenCountry())
                        .param("postcode", userGarden.getGardenPostcode())
                        .param("gardenSize", String.valueOf(1))
                        .param("latitude", "-43.5214643")
                        .param("longitude", "172.5796159"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection());
    }

    @Then("the new description is persisted")
    public void theNewDescriptionIsPersisted() {
        newGarden = gardenService.getAllUsersGardens(user.getId())
                .get(gardenService.getAllUsersGardens(user.getId()).size() - 1);
        Assertions.assertEquals(description, newGarden.getGardenDescription());
    }

    @Then("the new description is deleted")
    public void theNewDescriptionIsDeleted() {
        newGarden = gardenService.getAllUsersGardens(user.getId())
                .get(gardenService.getAllUsersGardens(user.getId()).size() - 1);
        Assertions.assertEquals("", newGarden.getGardenDescription());
    }

    @Then("the edited description is persisted")
    public void theEditedDescriptionIsPersisted() {
        Optional<Garden> testGarden = gardenService.getGardenById(userGarden.getGardenId());
        Assertions.assertEquals(description, testGarden.get().getGardenDescription());
    }

    @Then("the edited description is deleted")
    public void theEditedDescriptionIsDeleted() {
        Optional<Garden> testGarden = gardenService.getGardenById(userGarden.getGardenId());
        Assertions.assertEquals("", testGarden.get().getGardenDescription());
    }

    @When("I enter an invalid description {string}")
    public void iEnterAnInvalidDescription(String invalidDescription) {
        description = invalidDescription;
    }

    @Then("an error message appears")
    public void anErrorMessageAppears() {
        ModelAndView modelAndView = createGardenResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        String model = modelAndView.getModel().toString();
        Assertions.assertTrue(model.contains("Description must be 512 characters or less and contain some letters"));
    }

}
