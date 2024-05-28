package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PublicGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class BrowsePublicGardens {

    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public PlantRepository plantRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;


    @Autowired
    public SecurityService securityService;


    public static GardenService gardenService;

    public static UserService userService;

    public static PlantService plantService;

    public static FileService fileService;

    private String searchValue;

    private MvcResult mvcResult;


    @Given("I am browsing gardens")
    public void before_or_after_all() throws IOException, InterruptedException {

        fileService = new FileService();
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        plantService = new PlantService(plantRepository, gardenService, fileService);

        PublicGardensController publicGardensController = new PublicGardensController(gardenService);

        // Allows us to bypass spring security
        MOCK_MVC = MockMvcBuilders
                .standaloneSetup(publicGardensController)
                .build();
    }

    @Given("I enter the search string {string}")
    public void i_enter_the_search_string(String input) {
        searchValue = input;
    }

    @Given("I added {string} {string}, {int} is a user with email {string} and password {string}")
    public void i_added_is_a_user_with_email_and_password(String firstName, String LastName, Integer age, String userEmail, String userPassword) {
        if (userService.getUserByEmail(userEmail) == null) {
            int birthYear = 2024 - age;
            String dob = "01/01/" + birthYear;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.ENGLISH);
            LocalDate dateOfBirth = LocalDate.parse(dob, formatter);

            User user = new User(firstName, LastName, userEmail, dateOfBirth);
            user.setVerified(true);
            userService.addUser(user, userPassword);
            Assertions.assertNotNull(userService.getUserByEmail(userEmail));
        }

    }

    @When("I hit the search button with page {int}")
    public void i_hit_the_search_button_with_page(Integer page) throws Exception {
        mvcResult = MOCK_MVC.perform(
                        MockMvcRequestBuilders
                                .get("/public-gardens/search/" + page)
                                .param("searchInput", searchValue))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I am shown only gardens whose names or plants include my search string {string}")
    public void i_am_shown_only_gardens_whose_names_or_plants_include_my_search_string(String input) {
        List<Garden> searchResults = (List<Garden>) mvcResult.getModelAndView().getModelMap().getAttribute("publicGardens");
        assert searchResults != null;
        for (Garden result : searchResults) {
            Assertions.assertTrue(result.getGardenName().contains(input) || result.getPlants().stream().anyMatch(plant -> plant.getPlantName().contains(input)));
        }

    }

    @Then("A message tells me {string}")
    public void a_message_tells_me(String error) {
        String searchError = (String) mvcResult.getModelAndView().getModelMap().getAttribute("SearchErrorText");
        Assertions.assertEquals(error, searchError);
    }

    @And("User {string} has a garden {string} located in {string}, {string} with {int} plants.")
    public void user_has_a_garden_located_in_with_plants(String email, String gardenName, String city, String country, int plantNo) {
        User user = userService.getUserByEmail(email);
        if (user.getGardens().stream().noneMatch(garden -> Objects.equals(garden.getGardenName(), gardenName))) {
            Garden garden = new Garden(gardenName, "", "", city, "", country, 3.0, user);
            garden = gardenService.addGarden(garden);
            for (int i = 0; i < plantNo; i++) {
                String plantName = gardenName + " " + (i + 1);
                plantService.addPlant(plantName, 2, gardenName, LocalDate.now(), garden.getGardenId());
            }
        }

    }
}
