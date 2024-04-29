package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

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
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

@SpringBootTest
public class U10_Acceptance_Testing {

    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    @MockBean
    private LocationService locationService;

    public static GardenService gardenService;

    public static UserService userService;

    Long gardenId;
    String gardenName;
    String gardenLocation;
    String gardenSize;
    String gardenPostCode;

    String initialGardenName;
    String initialGardenLocation;
    String initialGardenSize;
    String initialGardenPostCode;

    String expectedNewLocation;

    String userEmail;

    @Given("I am on the edit garden page")
    public void before_or_after_all() {
        gardenService = new GardenService(gardenRepository);
        userService = new UserService(passwordEncoder, userRepository);


        userEmail = "testear@something.com";
        // Prep for user authentication that will be added later on
        User user = new User("Admin", "Test", userEmail, null);
        if (!userService.emailInUse(userEmail))
        {
            userService.addUser(user, "AlphabetSoup10!");
        }



        GardenFormController gardenFormController = new GardenFormController(gardenService, locationService);
        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders.standaloneSetup(gardenFormController).build();
    }

    @Given("There is a user")
    public void there_is_a_user_called() {
        Assertions.assertNotNull(userService.getUserByEmail(userEmail));
    }

    @Given("the user owns a garden {string}")
    public void owns_a_garden(String gardenName) {
        initialGardenLocation = "University Testing Lab";
        initialGardenSize = "0.0";
        Garden newGarden = new Garden(gardenName, initialGardenLocation
                ,initialGardenLocation,initialGardenLocation,gardenPostCode
                ,initialGardenLocation,initialGardenLocation, 0.0f);
        initialGardenName = gardenName;
        gardenService.addGarden(newGarden);
        gardenId = newGarden.getGardenId();
        initialGardenPostCode = newGarden.getGardenPostcode();
    }

    @Given("I am on the garden edit form")
    public void i_am_on_the_garden_edit_form() {
        gardenName = "My garden";
        gardenLocation = "My Location";
        gardenSize = "0.0";
    }

    @Given("I enter valid values for the {string}, {string}, {string} and {string}")
    public void i_enter_valid_values_for_the_and_optionally(String name, String location, String size, String postcode) {
        gardenName = name;
        gardenLocation = location;
        gardenSize = size;
        gardenPostCode = postcode;
    }

    @When("I enter a size using a comma")
    public void i_enter_a_size_using_a_comma() {
        gardenSize = "3,9";
    }

    @When("I enter an invalid name value {string}")
    public void i_enter_an_invalid_name_value_for_the(String string) {
        gardenName = string;
    }

    @When("I enter an invalid location value {string}")
    public void i_enter_an_invalid_location_value_for_the(String string) {
        gardenLocation = string;
    }

    @When("I enter an invalid size value {string}")
    public void i_enter_an_invalid_size_value_for_the(String string) {
        gardenSize = string;
    }

    @When("I click the edit plant form Submit button")
    public void i_click_the_edit_plant_form_submit_button() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d=%s/edit", gardenId, gardenName);
        MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post(gardenUrl)
                        .param("gardenName", gardenName)
                        .param("gardenSize", gardenSize)
                        .param("streetAddress",gardenLocation)
                        .param("suburb",gardenLocation)
                        .param("city", gardenLocation)
                        .param("country",gardenLocation)
                        .param("postcode",gardenPostCode)
                        .param("gardenLocation", "") // must be present, but is overridden immediately in controller

        );
    }

    @Then("The garden details have been updated")
    public void the_garden_details_have_been_updated() {
        Optional<Garden> optionalUpdatedGarden = gardenService.findById(gardenId);
        if (optionalUpdatedGarden.isEmpty()) {
            Assertions.fail();
            return;
        }
        Garden updatedGarden = optionalUpdatedGarden.get();
        Assertions.assertEquals(gardenName, updatedGarden.getGardenName());
        Assertions.assertTrue( updatedGarden.getGardenLocation().contains(gardenLocation));
        Assertions.assertEquals(Float.parseFloat(gardenSize.replace(",", ".")), updatedGarden.getGardenSize());
        Assertions.assertEquals(gardenPostCode, updatedGarden.getGardenPostcode());
    }

    @Then("The garden details are not updated")
    public void the_garden_details_are_not_updated() {
        Optional<Garden> optionalUpdatedGarden = gardenService.findById(gardenId);
        if (optionalUpdatedGarden.isEmpty()) {
            Assertions.fail();
            return;
        }
        Garden updatedGarden = optionalUpdatedGarden.get();
        Assertions.assertEquals(initialGardenName, updatedGarden.getGardenName());
        Assertions.assertEquals(initialGardenLocation, updatedGarden.getGardenLocation());
        Assertions.assertEquals(Float.parseFloat(initialGardenSize.replace(",", ".")), updatedGarden.getGardenSize());
        Assertions.assertEquals(initialGardenPostCode, updatedGarden.getGardenPostcode());
    }

}




