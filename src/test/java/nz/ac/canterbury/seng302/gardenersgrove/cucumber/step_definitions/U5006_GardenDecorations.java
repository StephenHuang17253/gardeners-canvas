package nz.ac.canterbury.seng302.gardenersgrove.cucumber.step_definitions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nz.ac.canterbury.seng302.gardenersgrove.controller.Garden3DController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Decoration;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GridItemLocation;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.DecorationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GridItemLocationService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.DecorationCategory;
import nz.ac.canterbury.seng302.gardenersgrove.util.GridItemType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
public class U5006_GardenDecorations {

    @Autowired
    private UserService userService;

    @Autowired
    private DecorationService decorationService;

    @Autowired
    private GridItemLocationService gridItemLocationService;

    @Autowired
    private Garden3DController garden3DController;

    MockMvc mockMvc;

    MvcResult mvcResult;

    @Autowired
    public ObjectMapper objectMapper;


    @Before
    public void beforeOrAfterAll() {
        mockMvc = MockMvcBuilders.standaloneSetup(garden3DController).build();
    }

    @Given("There is a garden decoration {string} saved in my {string} garden {string}")
    public void there_is_a_garden_decoration_saved_in_my_garden(String decorationCategory, String userEmail, String gardenName) {
        User user = userService.getUserByEmail(userEmail);
        Optional<Garden> matchingGarden = user.getGardens().stream()
                .filter(garden -> garden.getGardenName().equals(gardenName))
                .findFirst();
        Garden garden = matchingGarden.get();
        Decoration decoration1 = decorationService.addDecoration(new Decoration(garden, DecorationCategory.valueOf(decorationCategory.toUpperCase())));
        gridItemLocationService.addGridItemLocation(new GridItemLocation(decoration1.getId(), GridItemType.DECORATION, garden, 6, 6));
    }

    @When("I {string} open the 3D view of my garden {string}")
    public void i_open_the_3d_view_of_my_garden(String userEmail, String gardenName) throws Exception {

        User user = userService.getUserByEmail(userEmail);
        Optional<Garden> matchingGarden = user.getGardens().stream()
                .filter(garden -> garden.getGardenName().equals(gardenName))
                .findFirst();
        Garden garden = matchingGarden.get();
        mvcResult = mockMvc
                .perform(get("/3D-garden-layout/{gardenId}", garden.getGardenId()))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("I can see the garden decoration {string} in my {string} garden {string}")
    public void i_can_see_the_garden_decoration_in_my_garden(String decorationCategory, String userEmail, String gardenName) throws UnsupportedEncodingException, JsonProcessingException {
        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        JsonNode item = jsonNode.get(0);
        //should match decoration1 in before_all
        assertEquals(item.get("xcoordinate").asInt(), 6);
        assertEquals(item.get("ycoordinate").asInt(), 6);
        assertEquals(item.get("category").asText(), decorationCategory.substring(0, 1).toUpperCase() + decorationCategory.substring(1));
        assertEquals(item.get("type").asText(), "DECORATION");
    }


}
