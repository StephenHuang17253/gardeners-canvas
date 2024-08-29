package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.GardenTag;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.ModelAndView;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BrowsePublicGardensControllerTest {
    @Autowired
    private GardenService gardenService;
    @Autowired
    private GardenTagService gardenTagService;
    @Autowired
    private UserService userService;
    private final MockMvc mockMvc;

    private MvcResult mockMvcResult;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    List<Garden> mostRecent9TestGardens = new ArrayList<>();

    @Autowired
    BrowsePublicGardensControllerTest(MockMvc mockMvc) {
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void ClearRepository_AddUsersAndGardens() {

        User user1 = new User("John", "Doe", "johnDoe@email.com", date);
        userService.addUser(user1, "1es1P@ssword");
        for (int i = 1; i < 15; i++) {
            Garden newGarden = new Garden(
                    "Garden" + i,
                    "",
                    "114 Ilam Road",
                    "Ilam",
                    "Christchurch",
                    "8041",
                    "New Zealand",
                    10.0,
                    true,
                    "-43.5214643",
                    "172.5796159",
                    user1);
            gardenService.addGarden(newGarden);
            if (i > 5) {
                mostRecent9TestGardens.add(0, newGarden);
            }

        }

        GardenTag tag1 = new GardenTag("garden");
        GardenTag tag2 = new GardenTag("plants");

        gardenTagService.addGardenTag(tag1);
        gardenTagService.addGardenTag(tag2);

    }

    @Test
    @WithMockUser(username = "johnDoe@email.com")
    void GetPublicGardens_GardensAreReturnedInReversCreationDateOrder_Return200() throws Exception {
        mockMvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/public-gardens/search/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ModelAndView model = mockMvcResult.getModelAndView();
        Assertions.assertNotNull(model);
        List<Garden> publicGardens = (List<Garden>) model.getModelMap()
                .getAttribute("publicGardens");
        Assertions.assertEquals(publicGardens.toString(), mostRecent9TestGardens.toString());

    }

    @Test
    @WithMockUser(username = "johnDoe@email.com")
    void EnterTagName_TagNameExists_ReturnTrue() throws Exception {
        String fetchUrl = "/tag/exists";
        mockMvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get(fetchUrl)
                        .param("tagName", "garden")

        ).andReturn();

        String tagListResponse = mockMvcResult.getResponse().getContentAsString();

        Assertions.assertEquals("true", tagListResponse);

    }

    @Test
    @WithMockUser(username = "johnDoe@email.com")
    void EnterTagName_TagNameDoesNotExists_ReturnFalse() throws Exception {
        String fetchUrl = "/tag/exists";
        mockMvcResult = mockMvc.perform(
                MockMvcRequestBuilders
                        .get(fetchUrl)
                        .param("tagName", "blue")

        ).andReturn();

        String tagListResponse = mockMvcResult.getResponse().getContentAsString();

        Assertions.assertEquals("false", tagListResponse);

    }

}
