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
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
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
import org.springframework.ui.ModelMap;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;

@SpringBootTest
public class U10_Acceptance_Testing {

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

    @Autowired
    private LocationService locationService;


    public static GardenService gardenService;

    public static UserService userService;

    String gardenName;
    String gardenDescription;
    String gardenCity;
    String gardenCountry;
    String gardenSize;

    String gardenLongitude = "";
    String gardenLatitude = "";

    private Garden expectedGarden;

    private MvcResult editGardenResult;

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

    @Given("{string} {string}, {int} is a user with email {string} and password {string}")
    public void iAmAUserWithEmailAndPassword(String firstName, String LastName, Integer age, String userEmail, String userPassword) {
        int birthYear = 2024 - age;
        String dob = "01/01/" + birthYear;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
        LocalDate dateOfBirth = LocalDate.parse(dob, formatter);

        User user = new User(firstName, LastName, userEmail, dateOfBirth);
        user.setVerified(true);
        userService.addUser(user, userPassword);
        Assertions.assertNotNull(userService.getUserByEmail(userEmail));
    }

    @Given("User {string} has a garden {string} located in {string}, {string}")
    public void iAsUserHaveAGardenLocatedIn(String userEmail, String gardenName, String city, String country) {
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

    @When("I click the edit garden button")
    public void iClickTheEditGardenButton() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d/edit", expectedGarden.getGardenId());
        editGardenResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(gardenUrl)

        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();

    }

    @Then("I see the edit garden form where all the details are prepopulated")
    public void iSeeTheEditGardenPageWhereAllTheDetailsArePrepopulated() {
        ModelMap modelMap = editGardenResult.getModelAndView().getModelMap();

        Assertions.assertEquals(modelMap.getAttribute("gardenName"), expectedGarden.getGardenName());
        Assertions.assertEquals(modelMap.getAttribute("city"), expectedGarden.getGardenCity());
        Assertions.assertEquals(modelMap.getAttribute("country"), expectedGarden.getGardenCountry());
    }

    @Given("I am on the garden edit form")
    public void i_am_on_the_garden_edit_form() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d/edit", expectedGarden.getGardenId());
        editGardenResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .get(gardenUrl)

        ).andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
        ModelMap modelMap = editGardenResult.getModelAndView().getModelMap();

        Assertions.assertEquals(modelMap.getAttribute("gardenName"), expectedGarden.getGardenName());
        Assertions.assertEquals(modelMap.getAttribute("gardenDescription"), expectedGarden.getGardenDescription());
        Assertions.assertEquals(modelMap.getAttribute("city"), expectedGarden.getGardenCity());
        Assertions.assertEquals(modelMap.getAttribute("country"), expectedGarden.getGardenCountry());
        gardenName = expectedGarden.getGardenName();
        gardenDescription = expectedGarden.getGardenDescription();
        gardenCity = expectedGarden.getGardenCity();
        gardenCountry = expectedGarden.getGardenCountry();
        gardenSize = String.valueOf(expectedGarden.getGardenSize());
    }

    @Given("I enter valid garden values for the {string}, {string}, {string}, {string} and {string}")
    public void i_enter_valid_garden_values_for_the_and_optionally(String name, String description, String city, String country,
                                                                   String size) {
        gardenName = name;
        gardenCity = city;
        gardenCountry = country;
        gardenSize = size;
        gardenDescription = description;
    }

    @When("I click the Submit button on the edit garden form")
    public void i_click_the_submit_button_on_the_edit_plant_form() throws Exception {
        String gardenUrl = String.format("/my-gardens/%d/edit", expectedGarden.getGardenId());
        editGardenResult = MOCK_MVC.perform(
                MockMvcRequestBuilders
                        .post(gardenUrl)
                        .param("gardenName", gardenName)
                        .param("gardenDescription", gardenDescription)
                        .param("streetAddress", "")
                        .param("suburb", "")
                        .param("city", gardenCity)
                        .param("country", gardenCountry)
                        .param("postcode", "")
                        .param("gardenSize", gardenSize)
                        .param("latitude", gardenLatitude)
                        .param("longitude", gardenLongitude)

        ).andReturn();
    }

    @Then("The garden details have been updated")
    public void the_garden_details_have_been_updated() {
        Optional<Garden> optionalUpdatedGarden = gardenService.getGardenById(expectedGarden.getGardenId());
        if (optionalUpdatedGarden.isEmpty()) {
            Assertions.fail();
            return;
        }
        Garden updatedGarden = optionalUpdatedGarden.get();
        Assertions.assertEquals(gardenName, updatedGarden.getGardenName());
        Assertions.assertEquals(gardenDescription, updatedGarden.getGardenDescription());
        Assertions.assertEquals(gardenCity, updatedGarden.getGardenCity());
        Assertions.assertEquals(gardenCountry, updatedGarden.getGardenCountry());
        Assertions.assertEquals(Double.parseDouble(gardenSize.replace(",", ".")), updatedGarden.getGardenSize());
    }

    @Then("I am taken back to the garden details page")
    public void i_am_taken_back_to_the_garden_details_page() {
        String redirectedUrl = editGardenResult.getResponse().getRedirectedUrl();
        Assertions.assertEquals(String.format("/my-gardens/%d", expectedGarden.getGardenId()), redirectedUrl);

    }

    @When("I enter an invalid garden name value {string}")
    public void i_enter_an_invalid_garden_name_value(String string) {
        gardenName = string;
    }

    @Then("The garden details are not updated")
    public void the_garden_details_are_not_updated() {
        Optional<Garden> optionalUpdatedGarden = gardenService.getGardenById(expectedGarden.getGardenId());
        if (optionalUpdatedGarden.isEmpty()) {
            Assertions.fail();
            return;
        }
        Garden updatedGarden = optionalUpdatedGarden.get();
        Assertions.assertEquals(expectedGarden.getGardenName(), updatedGarden.getGardenName());
        Assertions.assertEquals(expectedGarden.getGardenCity(), updatedGarden.getGardenCity());
        Assertions.assertEquals(expectedGarden.getGardenCountry(), updatedGarden.getGardenCountry());
        Assertions.assertEquals(expectedGarden.getGardenSize(), updatedGarden.getGardenSize());
    }

    @Given("I enter invalid garden location values {string}, {string}")
    public void i_enter_invalid_garden_location_values(String city, String country) {
        gardenCity = city;
        gardenCountry = country;
    }

    @When("I enter an invalid garden size value {string}")
    public void i_enter_an_invalid_garden_size_value_for_the(String string) {
        gardenSize = string;
    }

    @When("I enter {double} as a size")
    public void i_enter_a_size_using_a_comma(double size) {
        gardenSize = String.valueOf(size);
    }

}
