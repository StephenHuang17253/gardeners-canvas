package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.GardenFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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

    private Garden expectedGarden;

    private MvcResult createGardenResult;

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);

        GardenFormController gardenFormController = new GardenFormController(gardenService, locationService, securityService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup(gardenFormController).build();

    }

    // AC1
    @Given("I specify a valid full address with {string}, {string}, {string}, {string}, and {string}")
    public void i_specify_a_valid_full_address(String street, String suburb, String city, String postcode, String country) {
        gardenName = "UC Garden";
        gardenStreet = street;
        gardenSuburb = suburb;
        gardenCity = city;
        gardenPostcode = postcode;
        gardenCountry = country;
        gardenSize = "15";
    }

    // AC1
    @When("I click the Submit button on the create garden form")
    public void i_click_the_submit_button_on_the_create_garden_form() throws Exception {
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
        String createdGardenId = createGardenResult.getResponse().getRedirectedUrl().substring("/my-gardens/".length());
        expectedGarden = gardenService.getGardenById(Long.parseLong(createdGardenId)).orElse(null);
    }

    // AC1
    @Then("The garden is created successfully with that location")
    public void the_garden_is_created_successfully_with_that_location() {
        Optional<Garden> optionalGarden = gardenService.getGardenById(expectedGarden.getGardenId());
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
}
