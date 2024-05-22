package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.MyGardensController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import org.junit.jupiter.api.Assertions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.ModelMap;

import java.util.List;

@SpringBootTest
public class U9_ViewGarden {
    public static MockMvc MOCK_MVC;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationManager authenticationManager;

    public SecurityService securityService;

    private static GardenService gardenService;

    private static UserService userService;

    private static PlantService plantService;

    private static FileService fileService;

    private ResultActions resultActions;

    private MvcResult mvcResult;

    @Before
    public void before_or_after_all() {
        userService = new UserService(passwordEncoder, userRepository);
        gardenService = new GardenService(gardenRepository, userService);
        securityService = new SecurityService(userService, authenticationManager);

        MyGardensController myGardensController = new MyGardensController(gardenService, securityService, plantService,
                fileService);
        MOCK_MVC = MockMvcBuilders.standaloneSetup(myGardensController).build();
    }

    @Given("I as user {string} is on my garden details page for {string}")
    public void iAsUserIsOnMyGardenDetailsPageFor(String userEmail, String gardenName) throws Exception {
        User user = userService.getUserByEmail(userEmail);
        String gardenId = String.valueOf(user.getGardens().get(0).getGardenId());
        MOCK_MVC.perform(
                get("/my-gardens/{gardenId}", gardenId)).andExpect(MockMvcResultMatchers.status().isOk());

    }


    @When("I try to visit user {string}'s garden, {string}")
    public void iTryToVisitGarden(String userEmail, String gardenName) throws Exception {
        User user = userService.getUserByEmail(userEmail);
        resultActions = MOCK_MVC.perform(
                get("/my-gardens/{gardenId}", user.getGardens().get(0).getGardenId()));
    }

    @Then("I am unable to visit the page")
    public void iAmUnableToVisitThePage() throws Exception {
        resultActions.andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Then("I am able to visit the page")
    public void iAmAbleToVisitThePage() throws Exception {
        mvcResult = resultActions.andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }

    @And("The garden's name {string} and location {string}, {string} are visible")
    public void theNameLocationAndOptionallySizeAreVisible(String gardenName, String gardenCity, String gardenCountry)
            throws Exception {
        ModelMap modelMap = mvcResult.getModelAndView().getModelMap();
        Assertions.assertEquals(modelMap.getAttribute("gardenName"), gardenName);
        Assertions.assertEquals(modelMap.getAttribute("gardenLocation"), gardenCity + ", " + gardenCountry);

    }
}
