package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden2DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GridItemLocationRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U5002_2D_Garden_Edit {
    public static MockMvc mockMVC;

    private MvcResult mvcResult;

    public static GardenService gardenService;

    public static UserService userService;

    private Garden garden;

    private User user;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public UserRepository userRepository;
    @Autowired
    public SecurityService securityService;
    @Autowired
    public GridItemLocationService gridItemLocationService;
    @Autowired
    public GridItemLocationRepository gridItemLocationRepository;
    private Map<String, Object> model;

    @Before
    public void before_or_after_all() {
        gardenService = new GardenService(gardenRepository, userService);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);

        Garden2DController garden2DController = new Garden2DController(gardenService, securityService, gridItemLocationService);
        mockMVC = MockMvcBuilders.standaloneSetup(garden2DController).build();
    }

    @Given("I as user {string} is on my two-D garden page for {string}")
    public void iAsUserIsOnMyDGardenPageFor(String email, String gardenName) throws Exception {
        user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get("/2D-garden/{gardenId}", garden.getGardenId()))
                .andExpect(status().isOk()).andReturn();
    }

    @Then("I see a palette window containing all my plants with relevant information as well as a save garden and clear all button")
    public void iSeeAPaletteWindowContainingAllMyPlantsWithRelevantInformationAsWellAsASaveGardenAndClearAllButton() {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        model = modelAndView.getModel();

        //palette window page
        Assertions.assertTrue(model.containsKey("currentPage"));

        //plants
        Assertions.assertTrue(model.containsKey("plants"));
    }
}
