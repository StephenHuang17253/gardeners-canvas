package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;
import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden2DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.Tile2DModel;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U5008_Tile_Textures {

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
    public DecorationRepository decorationRepository;

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
    public DecorationService decorationService;

    @Autowired
    public FileService fileService;

    @Autowired
    public GridItemLocationService gridItemLocationService;

    @Autowired
    public GridItemLocationRepository gridItemLocationRepository;

    @Autowired
    public GardenTileRepository gardenTileRepository;

    @Autowired
    public GardenTileService gardenTileService;

    private Map<String, Object> model;

    @Before
    public void beforeOrAfterAll() {
        gardenService = new GardenService(gardenRepository, userService);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);
        gridItemLocationService = new GridItemLocationService(gridItemLocationRepository);
        fileService = new FileService();
        plantService = new PlantService(plantRepository, gardenService, fileService);
        decorationService = new DecorationService(decorationRepository);
        gardenTileService = new GardenTileService(gardenTileRepository);

        Garden2DController garden2DController = new Garden2DController(gardenService, securityService,
                gridItemLocationService, plantService, decorationService, gardenTileService);
        mockMVC = MockMvcBuilders.standaloneSetup(garden2DController).build();
    }

    @Given("I as user {string} am on my 2D garden page for {string}")
    public void i_user_am_on_my_2d_garden(String email, String gardenName) {
        user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        Assertions.assertEquals(gardenName, garden.getGardenName());
    }

    @When("I select a texture {string} and place it at {int}, {int} and click save")
    public void i_select_a_texture_and_place_it_at_and_click_save(String newTextureName, Integer x, Integer y)
            throws Exception {
        mvcResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .get("/2D-garden/{gardenId}", garden.getGardenId()))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        model = modelAndView.getModel();

        List<Tile2DModel> tileModels = (List<Tile2DModel>) model.get("tiles");

        Assertions.assertEquals(49, tileModels.size());

        String[] tiles = new String[49];
        Arrays.fill(tiles, null);
        List<String> tilesList = Arrays.asList(tiles);

        for (Tile2DModel tileModel : tileModels) {
            String thisTileTextureName = tileModel.getTileTexture();
            if (tileModel.getXCoordinate() == x && tileModel.getYCoordinate() == y) {
                thisTileTextureName = newTextureName;
            }

            tilesList.set(tileModel.getYCoordinate() * 7 + tileModel.getXCoordinate(), thisTileTextureName);
        }

        List<Integer> emptyList = new ArrayList<>();

        mockMVC.perform(MockMvcRequestBuilders.post("/2D-garden/{gardenId}/save", garden.getGardenId())
                .with(csrf())
                .param("idList", JSONArray.toJSONString(emptyList))
                .param("typeList", JSONArray.toJSONString(emptyList))
                .param("xCoordList", JSONArray.toJSONString(emptyList))
                .param("yCoordList", JSONArray.toJSONString(emptyList))
                .param("tileTextureList", JSONArray.toJSONString(tilesList)))
                .andExpect(status().is3xxRedirection());
    }

    @Then("my new texture {string}, is persisted at {int}, {int}")
    public void my_new_texture_is_persisted_at(String newTextureName, Integer x, Integer y) throws Exception {
        mvcResult = mockMVC.perform(
                MockMvcRequestBuilders
                        .get("/2D-garden/{gardenId}", garden.getGardenId()))
                .andExpect(status().isOk()).andReturn();

        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        model = modelAndView.getModel();

        List<Tile2DModel> tileModels = (List<Tile2DModel>) model.get("tiles");

        Assertions.assertEquals(49, tileModels.size());

        Tile2DModel tileModelAtXY = null;

        for (Tile2DModel tileModel : tileModels) {
            if (tileModel.getXCoordinate() == x && tileModel.getYCoordinate() == y) {
                tileModelAtXY = tileModel;
            }
        }

        Assertions.assertNotNull(tileModelAtXY);

        Assertions.assertEquals(newTextureName, tileModelAtXY.getTileTexture());
    }
}
