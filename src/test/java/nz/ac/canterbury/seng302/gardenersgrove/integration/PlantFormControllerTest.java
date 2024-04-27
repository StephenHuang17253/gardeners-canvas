package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PlantFormControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @MockBean
    private UserService userServiceMock;

    @MockBean
    private PlantService plantService;

    User mockUser = new User("John", "Test", "profile.user.test@ProfileController.com", LocalDate.now());


    @InjectMocks
    private static PlantFormController plantFormController;

    @BeforeEach
    public void initial() {
        LocalDate date = LocalDate.of(2024, 3, 27);
        Garden test_garden = new Garden(
                "name",
                "address",
                "suburb",
                "city",
                "90210",
                "country",
                "location",
                1.0f
        );
        Plant test_plant = new Plant(
                "test",
                1,
                "test",
                date,
                test_garden
        );

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
    }

    @AfterAll
    public static void bin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void controllerLoadsTest()
    {
        assertNotNull(plantFormController);
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void heartbeat() throws Exception {
        String gardenId = "1";
        String gardenName = "test";
        String plantData = "{ \"name\": \"Rose\", \"plantingDate\": \"2024-04-27\", \"type\": \"Flower\" }";
        mockMvc.perform(get("/my-gardens/{gardenId}={gardenName}/create-new-plant", gardenId, gardenName))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void plantFormController_OnePlantAdded() throws Exception
    {
        Plant mockPlant = Mockito.spy(Plant.class);
        when(mockPlant.getPlantId()).thenReturn(1L);
        LocalDate date = LocalDate.of(2024, 3, 27);
        String gardenId = "1";
        String gardenName = "test";

        mockMvc.perform(post("/my-gardens/{gardenId}={gardenName}/create-new-plant",  gardenId, gardenName).with(csrf())
                        .param("plantName","test")
                        .param("plantCount", String.valueOf(1))
                        .param("plantDescription","test")
                        .param("plantDate", String.valueOf(date))
                        .param("gardenId", String.valueOf(1L)))
                .andDo(MockMvcResultHandlers.print());
        Mockito.verify(plantService, Mockito.times(1)).addPlant("test", 1, "test", date, 1L);
    }

    @Test
    @WithMockUser(username = "profile.user.test@ProfileController.com")
    public void plantFormController_plantEdited() throws Exception
    {
        String gardenId = "1";
        String gardenName = "test";
        String plantId = "1";
        String plantName = "test";
        LocalDate date = LocalDate.of(2024, 3, 27);
        mockMvc.perform(post("/my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit",  gardenId, gardenName, plantId, plantName).with(csrf())
                        .param("plantName","test")
                        .param("plantCount", String.valueOf(1))
                        .param("plantDescription","test")
                        .param("plantDate", String.valueOf(date))
                        .param("gardenId", String.valueOf(1L)))
                .andDo(MockMvcResultHandlers.print());
        Mockito.verify(plantService, Mockito.times(1)).updatePlant(1L, "test", 1.0F, "test", date);

    }



}
