package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.repository.UserRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
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

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.Matchers.is;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BrowsePublicGardensControllerTest {
    @Autowired
    private GardenService gardenService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private PlantService plantService;
    private final MockMvc mockMvc;

    private MvcResult mockMvcResult;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy").withLocale(Locale.ENGLISH);
    LocalDate date = LocalDate.parse("01/01/2001", formatter);

    List<Garden> mostRecent10TestGardens = new ArrayList<>();

    @Autowired
    public BrowsePublicGardensControllerTest(MockMvc mockMvc){
        this.mockMvc = mockMvc;
    }

    @BeforeEach
    void ClearRepository_AddUsersAndGardens() {

        User user1 = new User("John","Doe","johnDoe@email.com", date);
        userService.addUser(user1,"1es1P@ssword");
        for (int i = 1; i < 15; i++)
        {
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
            if (i > 4)
            {
                mostRecent10TestGardens.add(0,newGarden);
            }
            
        }
    }

    @Test
    @WithMockUser(username = "johnDoe@email.com")
    public void GetPublicGardens_GardensAreReturnedInReversCreationDateOrder_Return200() throws Exception {
        mockMvcResult = mockMvc
                .perform(MockMvcRequestBuilders.get("/public-gardens/page/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        List<Garden> publicGardens = (List<Garden>) mockMvcResult.getModelAndView().getModelMap().getAttribute("publicGardens");
        Assertions.assertEquals(publicGardens.toString(), mostRecent10TestGardens.toString());

    }

}
