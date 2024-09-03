package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@SpringBootTest
public class U5005_PlantCategories {
    public static MockMvc mockMvc;

    private MvcResult mvcResult;

    public static GardenService gardenService;

    public static PlantService plantService;

    public static UserService userService;

    public static FileService fileService;

    public static SecurityService securityService;

    private Garden garden;


    @Autowired
    public GardenRepository gardenRepo;

    @Autowired
    public PlantRepository plantRepo;

    @Autowired
    public UserRepository userRepo;

    @Before
    public void before() {
        gardenService = new GardenService(gardenRepo, userService);
        plantService = new PlantService(plantRepo,gardenService,fileService);

        PlantFormController plantFormController = new PlantFormController(plantService,gardenService,fileService,securityService);
        mockMvc = MockMvcBuilders.standaloneSetup(plantFormController).build();
    }
//    AC4.1, AC4.3
    @Given("I am creating a new plant with the name {string}")
    public void i_am_creating_a_new_plant_with_the_name(String plantName) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.1, AC4.2
    @Given("I can see a list of categories")
    public void i_can_see_a_list_of_categories() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.1, AC4.2
    @When("I select a category {string}")
    public void i_select_a_category(String selectedPlantCategory) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.1, AC4.2, AC.4.3
    @When("I submit my plant form")
    public void i_submit_my_plant_form() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.1
    @Then("My plant {string} is created with the plant category {string}")
    public void my_plant_is_created_with_the_plant_category(String plantName, String plantCategory) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
// AC4.2
    @Given("I have a plant {string} with a plant category {string}")
    public void i_have_a_plant_with_a_plant_category(String plantName, String plantCategory) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.2
    @Given("I select edit plant")
    public void i_select_edit_plant() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.2
    @Then("My plant {string} is updated with the plant category {string}")
    public void my_plant_is_updated_with_the_plant_category(String plantName, String newPlantCategory) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.3
    @When("I do not select a plant category")
    public void i_do_not_select_a_plant_category() {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }
//    AC4.3
    @Then("I receive an error message reading {string}")
    public void i_receive_an_error_message_reading(String errorMessage) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }


}
