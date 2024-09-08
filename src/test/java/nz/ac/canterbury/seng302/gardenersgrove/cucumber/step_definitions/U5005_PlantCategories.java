package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.HomePageLayoutRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.ModelAndView;
import java.time.LocalDate;
import java.util.Map;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U5005_PlantCategories {
    public static MockMvc mockMvc;
    private MvcResult mvcResult;

    public static GardenService gardenService;

    public static PlantService plantService;

    public static UserService userService;

    public static FileService fileService;

    @Autowired
    public SecurityService securityService;

    private Garden garden;

    private PlantCategory newPlantCategory;

    private String newPlantName;

    private Plant existingPlant;

    @Autowired
    public GardenRepository gardenRepo;

    @Autowired
    public PlantRepository plantRepo;

    @Autowired
    public HomePageLayoutRepository homePageLayoutRepository;

    @Autowired
    public PasswordEncoder passwordEncoder;

    @Autowired
    public UserRepository userRepository;

    @Before
    public void before() {
        fileService = new FileService();
        gardenService = new GardenService(gardenRepo, userService);
        plantService = new PlantService(plantRepo, gardenService, fileService);
        userService = new UserService(passwordEncoder, userRepository, homePageLayoutRepository);

        PlantFormController plantFormController = new PlantFormController(plantService,gardenService,fileService,securityService);
        mockMvc = MockMvcBuilders.standaloneSetup(plantFormController).build();
    }
//    AC4.1, AC4.3
    @Given("I {string} am creating a new plant with the name {string}")
    public void i_am_creating_a_new_plant_with_the_name(String email, String plantName) throws Exception {
        User user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        newPlantName = plantName;
        mvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/my-gardens/{gardenId}/create-new-plant", garden.getGardenId()))
                        .andExpect(status().isOk()).andReturn();
    }
//    AC4.1, AC4.2
    @Given("I can see a list of categories")
    public void i_can_see_a_list_of_categories() {
        ModelAndView modelAndView = mvcResult.getModelAndView();
        Assertions.assertNotNull(modelAndView);
        Map<String, Object> model = modelAndView.getModel();

        Assertions.assertTrue(model.containsKey("categories"));
        Assertions.assertEquals(plantService.getPlantCategories(), model.get("categories"));
    }
//    AC4.1, AC4.2
    @When("I select a category {string}")
    public void i_select_a_category(String selectedPlantCategory) {
        newPlantCategory = PlantCategory.valueOf(selectedPlantCategory.toUpperCase());
    }
//    AC4.1, AC.4.3
    @When("I submit create new plant form")
    public void i_submit_create_new_plant_form() throws Exception {

        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
    mvcResult = mockMvc.perform(MockMvcRequestBuilders
            .multipart("/my-gardens/{gardenId}/create-new-plant", garden.getGardenId())
            .file(mockFile) // Attach the file to the request
            .param("plantName", newPlantName)
            .param("plantCount", "3")
            .param("plantCategory",  String.valueOf(newPlantCategory))
            .param("plantDescription", "hello")
            .param("plantDate", LocalDate.now().toString())).andReturn();
    }
//    AC4.2
    @When("I submit edit plant form")
    public void i_submit_edit_plant_form() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mvcResult = mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", garden.getGardenId(), existingPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", existingPlant.getPlantName())
                .param("plantCount", "3")
                .param("plantCategory",  String.valueOf(newPlantCategory))
                .param("plantDescription", "hello")
                .param("plantDate", LocalDate.now().toString())).andReturn();

    }
//    AC4.1
    @Then("My plant {string} is created with the plant category {string}")
    public void my_plant_is_created_with_the_plant_category(String plantName, String plantCategory) {
        Assertions.assertEquals(302, mvcResult.getResponse().getStatus());
        Plant createdPlant = plantService.getPlants().get(0);
        Assertions.assertEquals(plantName, createdPlant.getPlantName());
        Assertions.assertEquals(plantCategory, createdPlant.getPlantCategory().toString());
    }
// AC4.2
    @Given("I {string} have a plant {string} with a plant category {string}")
    public void i_have_a_plant_with_a_plant_category(String email, String plantName, String plantCategory) {
        User user = userService.getUserByEmail(email);
        garden = user.getGardens().get(0);
        PlantCategory category = PlantCategory.valueOf(plantCategory.toUpperCase());
        existingPlant = new Plant(plantName, 1, "", LocalDate.now(), garden, category);
        plantRepo.save(existingPlant);

    }
//    AC4.2
    @Given("I select edit plant")
    public void i_select_edit_plant() throws Exception {
        mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/my-gardens/{gardenId}/{plantId}/edit", garden.getGardenId(), existingPlant.getPlantId())
                                .param("plantName", newPlantName)
                                .param("plantCategory", String.valueOf(newPlantCategory)))
                .andReturn();
    }
//    AC4.2
    @Then("My plant {string} is updated with the plant category {string}")
    public void my_plant_is_updated_with_the_plant_category(String plantName, String plantCategory) {
        Assertions.assertEquals(302, mvcResult.getResponse().getStatus());
        Plant updatedPlant = plantService.getPlants().get(0);
        Assertions.assertEquals(plantName, updatedPlant.getPlantName());
        Assertions.assertEquals(plantCategory, updatedPlant.getPlantCategory().toString());
    }
}
