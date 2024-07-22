package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
public class U11_RecordPlants {

    public static MockMvc MOCK_MVC;

    private ProfanityService profanityService;

    private InputValidator inputValidator;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @Autowired
    public SecurityService securityService;

    @MockBean
    private LocationService locationService;


    public static GardenService gardenService;

    public static UserService userService;

    public static PlantService plantService;

    String plantName;
    String plantDescription;
    String plantCount;
    String plantDate;

    String gardenName;
    String gardenDescription;
    String gardenCity;
    String gardenCountry;
    String gardenSize;

    String gardenLongitude = "";
    String gardenLatitude = "";
    private Garden expectedGarden;
    private Plant expectedPlant;
    private MvcResult newPlantResult;

    @Before
    public void before_or_after_all() {
        profanityService = Mockito.mock(ProfanityService.class);
        inputValidator = new InputValidator(userService, profanityService);

        Mockito.when(profanityService.containsProfanity(Mockito.anyString())).thenReturn(false);

        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);

        GardenFormController gardenFormController = new GardenFormController(gardenService, locationService,
                securityService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup(gardenFormController).build();

    }

    @And("User {string} with a garden {string} located in {string}, {string}")
    public void userWithAGardenLocatedIn(String userEmail, String gardenName, String city, String country) {
        User user = userService.getUserByEmail(userEmail);
        Garden garden = new Garden(gardenName, "", "", "", city, "", country, 0.0, false, "","", user);
        gardenService.addGarden(garden);
        Assertions.assertEquals(garden.getGardenId(),
                userService.getUserByEmail(userEmail).getGardens().get(0).getGardenId());
        expectedGarden = garden;
        this.gardenName = gardenName;
        gardenCity = garden.getGardenCity();
        gardenCountry = garden.getGardenCountry();
        gardenLongitude = garden.getGardenLongitude();
        gardenLatitude = garden.getGardenLatitude();
    }
    @When("I click the add new plant button")
    public void iClickTheAddNewPlantButton() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d/create-new-plant", expectedGarden.getGardenId());
        newPlantResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(gardenUrl)

        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @Then("I see an add plant form.")
    public void iSeeAnAddPlantForm() {

    }

    @Given("I am on the add plant form")
    public void iAmOnTheAddPlantForm() {
    }

    @And("I enter valid plant values for the <name>, <count>, <description>, and <date>")
    public void iEnterValidPlantValuesForTheNameCountDescriptionAndDate() {
    }

    @When("I click the Submit button on the add plant form")
    public void iClickTheSubmitButtonOnTheAddPlantForm() {
    }

    @Then("a new plant record is added to the garden")
    public void aNewPlantRecordIsAddedToTheGarden() {
    }

    @And("I enter invalid plant value for the <name>")
    public void iEnterInvalidPlantValueForTheName() {
    }

    @Then("a new plant record is not added to the garden")
    public void aNewPlantRecordIsNotAddedToTheGarden() {
    }

    @And("I enter invalid plant value for the {string}")
    public void iEnterInvalidPlantValueForThe(String arg0) {
    }

}
