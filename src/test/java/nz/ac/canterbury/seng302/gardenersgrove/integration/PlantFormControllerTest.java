package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PlantFormControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Autowired
    private PlantService plantService;
    @Autowired
    private UserService userService;
    @Autowired
    private GardenService gardenService;

    User mockUser = new User("Test", "Test", "test@gmail.com", LocalDate.now());

    @BeforeEach
    public void initial() {
        userService.addUser(mockUser,"1es1P@ssword");
        LocalDate date1 = LocalDate.of(2024, 3, 27);
        Garden test_garden = new Garden(
                "test",
                "test",
                "test",
                "test",
                "80",
                "test",
                10.0,
                mockUser
        );
        gardenService.addGarden(test_garden);

        plantService.addPlant("test",
                1,
                "test",
                date1,
                test_garden.getGardenId());

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @AfterAll
    public static void bin() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    public void heartbeat() throws Exception {
        String gardenId = "1";
        String plantData = "{ \"name\": \"Rose\", \"plantingDate\": \"2024-04-27\", \"type\": \"Flower\" }";
        mockMvc.perform(get("/my-gardens/{gardenId}/create-new-plant", gardenId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_OnePlantAdded() throws Exception
    {
        String gardenId = "1";
        String plantId = "1";
        String plantDescription = "test";
        int plantCount = 1;
        String plantName = "test";
        LocalDate date = LocalDate.parse("2024-03-28");
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/create-new-plant", gardenId)
                        .file(mockFile)     // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertEquals(plantName, plantService.findById(Long.parseLong(plantId)).get().getPlantName());
        Assertions.assertEquals(plantDescription, plantService.findById(Long.parseLong(plantId)).get().getPlantDescription());
        Assertions.assertEquals(plantCount, plantService.findById(Long.parseLong(plantId)).get().getPlantCount());
        Assertions.assertEquals(Long.parseLong(plantId), plantService.findById(Long.parseLong(plantId)).get().getPlantId());

    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_plantEdited() throws Exception {
        String plantName = "test";
        String gardenId = "1";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // This should match the name of the form field for the file in the request
                "image.jpg",         // Filename
                "image/jpeg",        // Content type
                "image data".getBytes() // File content
        );

        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andDo(MockMvcResultHandlers.print());


        Assertions.assertEquals(plantName, plantService.findById(Long.parseLong(plantId)).get().getPlantName());
        Assertions.assertEquals(plantDescription, plantService.findById(Long.parseLong(plantId)).get().getPlantDescription());
        Assertions.assertEquals(plantCount, plantService.findById(Long.parseLong(plantId)).get().getPlantCount());
        Assertions.assertEquals(Long.parseLong(plantId), plantService.findById(Long.parseLong(plantId)).get().getPlantId());
        Assertions.assertEquals(date, plantService.findById(Long.parseLong(plantId)).get().getPlantDate());
    }

    @ParameterizedTest
    @CsvSource({
            "!, 1, test description, 2024-03-28",
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length, 1, test description, 2024-03-28",
            "'', 1, test description, 2024-03-28"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_addNameVariantsFail(String plantName, int plantCount, String plantDescription, LocalDate date) throws Exception {
        String gardenId = "1";
        String plantId = "1";
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/create-new-plant", gardenId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertNotEquals(plantName, plantService.findById(Long.parseLong(plantId)).get().getPlantName());

    }

    @ParameterizedTest
    @CsvSource({
            "!",
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length",
            "''", "[", "{", "|", "$$", "o_o", "test@gmail.com", ":", ";", ","
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editNameVariantsFail(String plantName) throws Exception {
        String gardenId = "1";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertNotEquals(plantName, plantService.findById(Long.parseLong(plantId)).get().getPlantName());

    }

    @ParameterizedTest
    @CsvSource({
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length a " +
                    "very long plant description that exceeds the maximum length " +
                    "a very long plant description that exceeds the maximum length " +
                    "a very long plant description that exceeds the maximum length " +
                    "a very long plant description that exceeds the maximum length " +
                    "a very long plant description that exceeds the maximum length " +
                    "a very long plant name that exceeds the maximum lengtha very " +
                    "long plant name that exceeds the maximum lengtha very long " +
                    "plant name that exceeds the maximum lengtha very long plant " +
                    "name that exceeds the maximum lengtha very long plant name " +
                    "that exceeds the maximum lengtha very long plant name that " +
                    "exceeds the maximum lengtha very long plant name that exceeds " +
                    "the maximum lengtha very long plant name that exceeds the maximum" +
                    " lengtha very long plant name that exceeds the maximum lengtha very " +
                    "long plant name that exceeds the maximum lengtha very long plant name " +
                    "that exceeds the maximum lengtha very long plant name that exceeds the" +
                    " maximum lengtha very long plant name that exceeds the maximum lengtha " +
                    "very long plant name that exceeds the maximum length"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editDescriptionVariantsFail(String plantDescription) throws Exception {
        String gardenId = "1";
        String plantId = "1";
        String plantName = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile)  // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertNotEquals(plantDescription, plantService.findById(Long.parseLong(plantId)).get().getPlantDescription());

    }

    @ParameterizedTest
    @CsvSource({
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length",
            "''", "[", "{", "|", "$$", "o_o", "test@gmail.com", ":", ";",
            "-1", "0", "-1.0", "a", "!", "{}", "99999999999999999999999999999999999999999", "5..5", "5.5.7",
            "00/00/0000", "13/12/2000", "12/32/2000", "11/11/abcd", "!",
            "test", "''", "{}", "a/12/2000","12/a/2000",
            "71/1/1", "1/12/2000", "12/1/2000", "01/02/2", "01/05/0000"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editDateVariantsFail(String date) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate formattedDate;
        String plantId = "1";
        // try catch for different date input types
        try {
            formattedDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            Assertions.assertNotEquals(date, plantService.findById(Long.parseLong(plantId)).get().getPlantDate());
            return;
        }
        String gardenId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        String plantName = "test";
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", String.valueOf(formattedDate)))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertNotEquals(formattedDate, plantService.findById(Long.parseLong(plantId)).get().getPlantDate());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "!",
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length",
            "''", "[", "{", "|", "$$", "o_o", "test@gmail.com", ":", ";",
            "-1", "0", "-1.0", "a", "!", "{}", "99999999999999999999999999999999999999999", "5..5", "5.5.7"

    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editCountVariantsFail(String plantCount) throws Exception {
        String gardenId = "1";
        String plantName = "test name";
        String plantId = "1";
        String plantDescription = "standardPlant";
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertNotEquals(plantCount, plantService.findById(Long.parseLong(plantId)).get().getPlantCount());

    }

    @ParameterizedTest
    @CsvSource({
            "11/11/2000", "07/06/9999"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editDateVariantsPass(String date) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate formattedDate = LocalDate.parse(date, formatter);

        String gardenId = "1";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        String plantName = "test";
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", String.valueOf(formattedDate)))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertEquals(formattedDate, plantService.findById(Long.parseLong(plantId)).get().getPlantDate());
    }

    @ParameterizedTest
    @CsvSource({
            "plant name", "PLANT NAME", "plantName", "c00l pl4nt", "this, is. a-real_plant"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editNameVariantsPass(String plantName) throws Exception {
        String gardenId = "1";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/my-gardens/"+gardenId))   // Assert the expected HTTP status
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertEquals(plantName, plantService.findById(Long.parseLong(plantId)).get().getPlantName());

    }

    @ParameterizedTest
    @CsvSource({
            "plant description", "PLANT", "plantDescription", "c00l pl4nt", "this, is. a-real_plant",             "!",
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant description that does not exceed the maximum length" +
                    "a very long plant description that does not exceed the maximum length" +
                    "a very long plant description that does not exceed the maximum length" +
                    "a very long plant description that does not exceed the maximum length",
            "''", "[", "{", "|", "$$", "o_o", "test@gmail.com", ":", ";",
            "-1", "0", "-1.0", "a", "!", "{}", "99999999999999999999999999999999999999999", "5..5", "5.5.7"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editDescriptionVariantsPass(String plantDescription) throws Exception {
        String gardenId = "1";
        String gardenName = "test";
        String plantId = "1";
        String plantName = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile) // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/my-gardens/"+gardenId))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertEquals(plantDescription, plantService.findById(Long.parseLong(plantId)).get().getPlantDescription());

    }

    @ParameterizedTest
    @CsvSource({
            "1", "1.0", "9999", "0.1", "9987.123"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editCountVariantsPass(Float plantCount) throws Exception {
        String gardenId = "1";
        String plantName = "test name";
        String plantId = "1";
        String plantDescription = "standardPlant";
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput",       // Form field name
                "default_plant.png",       // Filename
                "image/png",               // Content type
                "image data".getBytes()    // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .file(mockFile)   // Attach the file to the request
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString()))
                .andExpect(MockMvcResultMatchers.redirectedUrl("/my-gardens/"+gardenId))
                .andDo(MockMvcResultHandlers.print());

        Assertions.assertEquals(plantCount, plantService.findById(Long.parseLong(plantId)).get().getPlantCount());

    }

}
