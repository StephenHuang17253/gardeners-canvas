package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden3DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class U50010_View_Public_3D_Garden {
    public static MockMvc mockMVC;

    private MvcResult mvcResult;

    public static GardenService gardenService;

    public static UserService userService;

    private Garden garden;

    private User user;

    @Autowired
    public GardenRepository gardenRepository;

    @Autowired
    public PlantRepository plantRepository;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    public SecurityService securityService;

    @Autowired
    public PlantService plantService;

    @Autowired
    public FileService fileService;

    @Autowired
    public FriendshipService friendshipService;

    @Autowired
    public GridItemLocationService gridItemLocationService;

    @Autowired
    public GardenTileService gardenTileService;

    @Autowired
    public GridItemLocationRepository gridItemLocationRepository;

    @Autowired
    public FriendshipRepository friendshipRepository;

    @Autowired
    public GardenTileRepository gardenTileRepository;

    @Autowired
    public WeatherService weatherService;

    @Autowired
    public DecorationService decorationService;

    @Autowired
    public DecorationRepository decorationRepository;

    private Map<String, Object> model;

    @Before
    public void beforeOrAfterAll() {
        gardenService = new GardenService(gardenRepository, userService);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);
        fileService = new FileService();
        plantService = new PlantService(plantRepository, gardenService, fileService);
        friendshipService = new FriendshipService(friendshipRepository, userService);
        weatherService = new WeatherService();
        gardenTileService = new GardenTileService(gardenTileRepository);

        decorationService = new DecorationService(decorationRepository);
        Garden3DController garden3DController = new Garden3DController(gardenService, securityService,
                friendshipService, gridItemLocationService, weatherService, plantService, decorationService,gardenTileService);
        mockMVC = MockMvcBuilders.standaloneSetup(garden3DController).build();
    }

    @Given("I try to navigate to {string}'s 3d garden {string}")
    public void iTryToNavigateTo3DGarden(String email, String gardenName) throws Exception {
        user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        assertEquals(gardenName, garden.getGardenName());
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get("/3D-garden/{gardenId}", garden.getGardenId()))
                .andReturn();
    }

    @Then("I am able to view the 3D garden")
    public void iAmAbleToViewThe3DGarden() {
        assertEquals(200, mvcResult.getResponse().getStatus());

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        model = modelAndView.getModel();

        Assertions.assertTrue(model.containsKey("garden"));
    }
}
