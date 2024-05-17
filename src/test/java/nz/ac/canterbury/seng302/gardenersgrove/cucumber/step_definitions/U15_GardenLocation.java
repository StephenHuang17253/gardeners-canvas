package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.Optional;

@SpringBootTest
public class U15_GardenLocation {

    public static MockMvc MOCK_MVC;

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

    public String gardenName;
    public String gardenStreet;
    public String gardenSuburb;
    public String gardenCity;
    public String gardenPostcode;
    public String gardenCountry;
    public String gardenSize;
    private Garden expectedNewGarden;
    private Garden expectedGarden;
    private MvcResult createGardenResult;
    private MvcResult editGardenResult;
    Logger logger = LoggerFactory.getLogger(U15_GardenLocation.class);


    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);

        GardenFormController gardenFormController = new GardenFormController(gardenService, locationService, securityService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup(gardenFormController).build();

    }

    @Given("I as user {string} have another garden {string} located in {string}, {string}")
    public void i_as_user_have_another_garden_located_in(String userEmail, String gardenName, String city, String country) {
        User user = userService.getUserByEmail(userEmail);
        Garden garden = new Garden(gardenName, "", "", city, "", country, 0.0, user);
        gardenService.addGarden(garden);
        Assertions.assertEquals(garden.getGardenId(), userService.getUserByEmail(userEmail).getGardens().get(0).getGardenId());
        expectedGarden = garden;
        this.gardenName = gardenName;
        gardenCity = garden.getGardenCity();
        gardenCountry = garden.getGardenCountry();
    }

    // AC1
    @Given("I specify a valid address with {string}, {string}, {string}, {string}, and {string}")
    public void i_specify_a_valid_address(String street, String suburb, String city, String postcode, String country) {
        gardenName = "UC Garden";
        gardenStreet = street;
        gardenSuburb = suburb;
        gardenCity = city;
        gardenPostcode = postcode;
        gardenCountry = country;
        gardenSize = "15";
    }

    // AC3
    @Given("I specify an invalid address with {string}, {string}, {string}, {string}, and {string}")
    public void i_specify_an_invalid_address(String street, String suburb, String city, String postcode, String country) {
        gardenName = "UC Garden";
        gardenStreet = street;
        gardenSuburb = suburb;
        gardenCity = city;
        gardenPostcode = postcode;
        gardenCountry = country;
        gardenSize = "15";
    }


    // AC1, AC3, AC5
    @When("I submit the create garden form")
    public void i_submit_the_create_garden_form() throws Exception {
        String gardenUrl = "/create-new-garden";
        createGardenResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post(gardenUrl)
                        .param("gardenName", gardenName)
                        .param("streetAddress", gardenStreet)
                        .param("suburb", gardenSuburb)
                        .param("city", gardenCity)
                        .param("country", gardenCountry)
                        .param("postcode", gardenPostcode)
                        .param("gardenSize", gardenSize)

        ).andReturn();
    }

    // AC1, AC3.1, AC4
    @Then("The garden is created successfully with that location")
    public void the_garden_is_created_successfully_with_that_location() {
        String createdGardenId = createGardenResult.getResponse().getRedirectedUrl().substring("/my-gardens/".length());
        expectedNewGarden = gardenService.getGardenById(Long.parseLong(createdGardenId)).orElse(null);
        Optional<Garden> optionalGarden = gardenService.getGardenById(expectedNewGarden.getGardenId());
        if (optionalGarden.isEmpty()) {
            Assertions.fail();
            return;
        }
        Garden createdGarden = optionalGarden.get();
        Assertions.assertEquals(gardenName, createdGarden.getGardenName());
        Assertions.assertEquals(gardenStreet, createdGarden.getGardenAddress());
        Assertions.assertEquals(gardenSuburb, createdGarden.getGardenSuburb());
        Assertions.assertEquals(gardenCity, createdGarden.getGardenCity());
        Assertions.assertEquals(gardenPostcode, createdGarden.getGardenPostcode());
        Assertions.assertEquals(gardenCountry, createdGarden.getGardenCountry());
        Assertions.assertEquals(Double.parseDouble(gardenSize.replace(",", ".")), createdGarden.getGardenSize());
    }

    // AC2
    @When("I submit the edit garden form")
    public void i_submit_the_edit_plant_form() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d/edit", expectedGarden.getGardenId());
        editGardenResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post(gardenUrl)
                        .param("gardenName", gardenName)
                        .param("streetAddress", gardenStreet)
                        .param("suburb", gardenSuburb)
                        .param("city", gardenCity)
                        .param("country", gardenCountry)
                        .param("postcode", gardenPostcode)
                        .param("gardenSize", gardenSize) // must be present, but is overridden immediately in controller

        ).andReturn();
    }

    // AC2
    @Then("The garden details are successfully updated")
    public void the_garden_details_are_successfully_updated() {
        Optional<Garden> optionalUpdatedGarden = gardenService.getGardenById(expectedGarden.getGardenId());
        if (optionalUpdatedGarden.isEmpty()) {
            Assertions.fail();
            return;
        }
        Garden updatedGarden = optionalUpdatedGarden.get();
        Assertions.assertEquals(gardenName, updatedGarden.getGardenName());
        Assertions.assertEquals(gardenStreet, updatedGarden.getGardenAddress());
        Assertions.assertEquals(gardenSuburb, updatedGarden.getGardenSuburb());
        Assertions.assertEquals(gardenCity, updatedGarden.getGardenCity());
        Assertions.assertEquals(gardenPostcode, updatedGarden.getGardenPostcode());
        Assertions.assertEquals(gardenCountry, updatedGarden.getGardenCountry());
        Assertions.assertEquals(Double.parseDouble(gardenSize.replace(",", ".")), updatedGarden.getGardenSize());

    }

    // AC3.2, AC5
    @Then("The garden is not created")
    public void the_garden_is_not_created() {
        Assertions.assertNull(expectedNewGarden);

    }

    // AC5
    @Then("An error message tells me 'City and Country are required'")
    public void city_and_country_error_message() {
        logger.info(String.valueOf(createGardenResult.getModelAndView()));
        ModelAndView modelAndView = createGardenResult.getModelAndView();
        String model = modelAndView.getModel().toString();

        // This error message is currently wrong, it is a placeholder until the fix is merged in from another branch.
        Assertions.assertTrue(model.contains("City and Country are required"));



    }


}
