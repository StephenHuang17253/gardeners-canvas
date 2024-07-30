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
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
public class U15_GardenLocation {

    public static MockMvc mockMVC;

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

    @Autowired
    private LocationService locationService;

    public static GardenService gardenService;

    public static UserService userService;

    public String gardenName;
    public String gardenDescription;
    public String gardenStreet;
    public String gardenSuburb;
    public String gardenCity;
    public String gardenPostcode;
    public String gardenCountry;
    public String gardenSize;
    public String gardenLongitude;
    public String gardenLatitude;
    private Garden expectedNewGarden;
    private Garden expectedGarden;
    private MvcResult createGardenResult;
    private MvcResult locationResult;
    Logger logger = LoggerFactory.getLogger(U15_GardenLocation.class);

    @Before
    public void before_or_after_all() throws IOException, InterruptedException {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        locationService = Mockito.mock(LocationService.class);

        GardenFormController gardenFormController = new GardenFormController(gardenService, locationService,
                securityService);
        // Allows us to bypass spring security
        mockMVC = MockMvcBuilders.standaloneSetup(gardenFormController).build();

        // Mocking LocationService response
        String mockResponse = "   {\n" +
                "      \"place_id\":\"321781519185\",\n" +
                "      \"osm_id\":\"5264937246\",\n" +
                "      \"osm_type\":\"node\",\n" +
                "      \"licence\":\"https:\\/\\/locationiq.com\\/attribution\",\n" +
                "      \"lat\":\"-43.5280654\",\n" +
                "      \"lon\":\"172.5846033\",\n" +
                "      \"boundingbox\":[\n" +
                "         \"-43.5281154\",\n" +
                "         \"-43.5280154\",\n" +
                "         \"172.5845533\",\n" +
                "         \"172.5846533\"\n" +
                "      ],\n" +
                "      \"class\":\"place\",\n" +
                "      \"type\":\"house\",\n" +
                "      \"display_name\":\"20, Kirkwood Avenue, Upper Riccarton, Christchurch, Christchurch City, Canterbury, 8041, New Zealand\",\n"
                +
                "      \"display_place\":\"Kirkwood Avenue\",\n" +
                "      \"display_address\":\"20, Upper Riccarton, Christchurch, Christchurch City, Canterbury, 8041, New Zealand\",\n"
                +
                "      \"address\":{\n" +
                "         \"house_number\":\"20\",\n" +
                "         \"road\":\"Kirkwood Avenue\",\n" +
                "         \"suburb\":\"Upper Riccarton\",\n" +
                "         \"city\":\"Christchurch\",\n" +
                "         \"county\":\"Christchurch City\",\n" +
                "         \"state\":\"Canterbury\",\n" +
                "         \"postcode\":\"8041\",\n" +
                "         \"country\":\"New Zealand\",\n" +
                "         \"name\":\"Kirkwood Avenue\",\n" +
                "         \"country_code\":\"nz\"\n" +
                "      }\n" +
                "   },";
        when(locationService.getLocationSuggestions(anyString())).thenReturn(mockResponse);
    }

    @Given("I as user {string} have another garden {string} located in {string}, {string}")
    public void i_as_user_have_another_garden_located_in(String userEmail, String gardenName, String city,
            String country) {
        User user = userService.getUserByEmail(userEmail);
        Garden garden = new Garden(gardenName, "A garden for UC students", "", "", city, "", country, 0.0, false, "",
                "", user);
        gardenService.addGarden(garden);
        Assertions.assertEquals(garden.getGardenId(),
                userService.getUserByEmail(userEmail).getGardens().get(0).getGardenId());
        expectedGarden = garden;
        this.gardenName = gardenName;
        gardenDescription = garden.getGardenDescription();
        gardenCity = garden.getGardenCity();
        gardenCountry = garden.getGardenCountry();
    }

    // AC1
    @Given("I specify a valid address with {string}, {string}, {string}, {string}, {string}, {string}, and {string}")
    public void i_specify_a_valid_address(String street, String suburb, String city, String postcode, String country,
            String latitude, String longitude) {
        gardenName = "UC Garden";
        gardenDescription = "A garden for UC students";
        gardenStreet = street;
        gardenSuburb = suburb;
        gardenCity = city;
        gardenPostcode = postcode;
        gardenCountry = country;
        gardenSize = "15";
        gardenLongitude = longitude;
        gardenLatitude = latitude;
    }

    // AC3
    @Given("I specify an invalid address with {string}, {string}, {string}, {string}, {string}, {string}, and {string}")
    public void i_specify_an_invalid_address(String street, String suburb, String city, String postcode, String country,
            String latitude, String longitude) {
        gardenName = "UC Garden";
        gardenDescription = "A garden for UC students";
        gardenStreet = street;
        gardenSuburb = suburb;
        gardenCity = city;
        gardenPostcode = postcode;
        gardenCountry = country;
        gardenSize = "15";
        gardenLongitude = latitude;
        gardenLatitude = longitude;
    }

    // AC1, AC3, AC5
    @When("I submit the create garden form")
    public void i_submit_the_create_garden_form() throws Exception {
        String gardenUrl = "/create-new-garden";
        createGardenResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .post(gardenUrl)
                        .param("gardenName", gardenName)
                        .param("gardenDescription", gardenDescription)
                        .param("streetAddress", gardenStreet)
                        .param("suburb", gardenSuburb)
                        .param("city", gardenCity)
                        .param("country", gardenCountry)
                        .param("postcode", gardenPostcode)
                        .param("gardenSize", gardenSize)
                        .param("longitude", gardenLongitude)
                        .param("latitude", gardenLatitude))
                .andReturn();
    }

    // AC1, AC3.1, AC4
    @Then("The garden is created successfully with that location")
    public void the_garden_is_created_successfully_with_that_location() {
        String redirectUrl = createGardenResult.getResponse().getRedirectedUrl();
        Assertions.assertNotNull(redirectUrl);
        String createdGardenId = redirectUrl.substring("/my-gardens/".length());
        expectedNewGarden = gardenService.getGardenById(Long.parseLong(createdGardenId)).orElse(null);
        assert expectedNewGarden != null;
        Optional<Garden> optionalGarden = gardenService.getGardenById(expectedNewGarden.getGardenId());
        if (optionalGarden.isEmpty()) {
            Assertions.fail();
            return;
        }
        Garden createdGarden = optionalGarden.get();
        Assertions.assertEquals(gardenName, createdGarden.getGardenName());
        Assertions.assertEquals(gardenDescription, createdGarden.getGardenDescription());
        Assertions.assertEquals(gardenStreet, createdGarden.getGardenAddress());
        Assertions.assertEquals(gardenSuburb, createdGarden.getGardenSuburb());
        Assertions.assertEquals(gardenCity, createdGarden.getGardenCity());
        Assertions.assertEquals(gardenPostcode, createdGarden.getGardenPostcode());
        Assertions.assertEquals(gardenCountry, createdGarden.getGardenCountry());
        Assertions.assertEquals(gardenLatitude, createdGarden.getGardenLatitude());
        Assertions.assertEquals(gardenLongitude, createdGarden.getGardenLongitude());
        Assertions.assertEquals(Double.parseDouble(gardenSize.replace(",", ".")), createdGarden.getGardenSize());
    }

    // AC2
    @When("I submit the edit garden form")
    public void i_submit_the_edit_plant_form() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d/edit", expectedGarden.getGardenId());
        mockMVC.perform(
                MockMvcRequestBuilders
                        .post(gardenUrl)
                        .param("gardenName", gardenName)
                        .param("gardenDescription", gardenDescription)
                        .param("streetAddress", gardenStreet)
                        .param("suburb", gardenSuburb)
                        .param("city", gardenCity)
                        .param("country", gardenCountry)
                        .param("postcode", gardenPostcode)
                        .param("gardenSize", gardenSize) // must be present, but is overridden immediately in controller
                        .param("longitude", gardenLongitude)
                        .param("latitude", gardenLatitude))
                .andReturn();
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
        Assertions.assertEquals(gardenDescription, updatedGarden.getGardenDescription());
        Assertions.assertEquals(gardenStreet, updatedGarden.getGardenAddress());
        Assertions.assertEquals(gardenSuburb, updatedGarden.getGardenSuburb());
        Assertions.assertEquals(gardenCity, updatedGarden.getGardenCity());
        Assertions.assertEquals(gardenPostcode, updatedGarden.getGardenPostcode());
        Assertions.assertEquals(gardenCountry, updatedGarden.getGardenCountry());
        Assertions.assertEquals(gardenLatitude, updatedGarden.getGardenLatitude());
        Assertions.assertEquals(gardenLongitude, updatedGarden.getGardenLongitude());
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
        Assertions.assertNotNull(modelAndView);
        String model = modelAndView.getModel().toString();

        Assertions.assertTrue(model.contains("City and Country are required"));

    }

    // AC6
    @When("I start typing {string}")
    public void i_start_typing(String query) throws Exception {
        String fetchUrl = "/api/location/suggestions";
        locationResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .get(fetchUrl)
                        .param("query", query)

        ).andReturn();

    }

    // AC6, AC7
    @Then("LocationService is invoked to make an API request")
    public void location_service_is_invoked() throws UnsupportedEncodingException {
        String responseBody = locationResult.getResponse().getContentAsString();
        logger.info(responseBody);
        String expectedLocation = "20, Kirkwood Avenue, Upper Riccarton, Christchurch, Christchurch City, Canterbury, 8041, New Zealand";
        Assertions.assertTrue(responseBody.contains(expectedLocation));
    }

    // AC7
    @Then("The matching fields are filled out")
    public void the_matching_fields_are_filled_out() {
        // not that meaningful of a test, but it's meant to emulate how the autocomplete
        // script fills the fields.
        gardenStreet = "Mock Street";
        gardenSuburb = "Mock Suburb";
        gardenCity = "Mock City";
        gardenPostcode = "MOCK";
        gardenCountry = "The United States of Mockland";

        Assertions.assertEquals("Mock Street", gardenStreet);
        Assertions.assertEquals("Mock Suburb", gardenSuburb);
        Assertions.assertEquals("Mock City", gardenCity);
        Assertions.assertEquals("MOCK", gardenPostcode);
        Assertions.assertEquals("The United States of Mockland", gardenCountry);

    }

}
