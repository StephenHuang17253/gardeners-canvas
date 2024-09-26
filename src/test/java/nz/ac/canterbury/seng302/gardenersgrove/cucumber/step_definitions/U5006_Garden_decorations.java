package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden2DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U5006_Garden_decorations {
    public static MockMvc mockMVC;
    private MvcResult mvcResult;
    private Map<String, Object> model;
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
    public GridItemLocationService gridItemLocationService;
    @Autowired
    public GridItemLocationRepository gridItemLocationRepository;

    public static GardenService gardenService;

    public static UserService userService;
    public static DecorationService decorationService;

    @Autowired
    public static GardenTileService tileService;

    @Autowired
    public GardenTileRepository gardenTileRepository;

    @Autowired
    public DecorationRepository decorationRepository;

    private static final int numOfTiles = 49;

    @Before
    public void before_or_after_all() {
        gardenService = new GardenService(gardenRepository, userService);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);
        fileService = new FileService();
        plantService = new PlantService(plantRepository, gardenService, fileService);
        decorationService = new DecorationService(decorationRepository);
        tileService = new GardenTileService(gardenTileRepository);


        Garden2DController garden2DController = new Garden2DController(gardenService, securityService,
                gridItemLocationService, plantService, decorationService, tileService);
        mockMVC = MockMvcBuilders.standaloneSetup(garden2DController).build();
    }
    @Then("As user {string} I see a palette window with a tab labelled for decorations")
    public void asUserISeeAPaletteWindowWithATabLabelledForDecorations(String email) throws Exception {
        user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        mvcResult = mockMVC.perform(
                        MockMvcRequestBuilders
                                .get("/2D-garden/{gardenId}", garden.getGardenId()))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        model = modelAndView.getModel();

        Assertions.assertTrue(model.containsKey("displayableItemsList"));
    }

    @When("I As user {string} place a decoration on my grid and press save")
    public void iAsUserPlaceADecorationOnMyGridAndPressSave(String email) throws Exception {
        gridItemLocationRepository.deleteAll();
        user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        Long gardenId = garden.getGardenId();
        Decoration decoration = decorationService.getDecorationsByGardenAndCategory(garden, DecorationCategory.ROCK).get(0);

        List<String> idList = new ArrayList<>();
        idList.add(decoration.getId().toString());

        List<String> typeList = new ArrayList<>();
        typeList.add("DECORATION");

        List<Double> xCoordList = new ArrayList<>();
        xCoordList.add(2.0);

        List<Double> yCoordList = new ArrayList<>();
        yCoordList.add(3.0);

        String[] grassArray = new String[numOfTiles];
        Arrays.fill(grassArray, "GRASS");
        List<String> tileTextureList = Arrays.asList(grassArray);

        mockMVC.perform(MockMvcRequestBuilders.post("/2D-garden/" + gardenId + "/save").with(csrf())
                        .param("idList", JSONArray.toJSONString(idList))
                        .param("typeList", JSONArray.toJSONString(typeList))
                        .param("xCoordList", JSONArray.toJSONString(xCoordList))
                        .param("yCoordList", JSONArray.toJSONString(yCoordList))
                        .param("tileTextureList", JSONArray.toJSONString(tileTextureList)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection()).andReturn();
    }

    @Then("I see my placed decoration")
    public void iSeeMyPlacedDecoration() {
        Decoration decoration = decorationService.getDecorationsByGardenAndCategory(garden, DecorationCategory.ROCK).get(0);
        List<GridItemLocation> gridItems = gridItemLocationService.getGridItemLocationByGarden(garden);
        Assertions.assertFalse(gridItems.isEmpty());
        Assertions.assertEquals(gridItems.getLast().getObjectId(), decoration.getId());
    }
}
