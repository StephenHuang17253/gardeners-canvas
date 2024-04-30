package nz.ac.canterbury.seng302.gardenersgrove.integration;

import nz.ac.canterbury.seng302.gardenersgrove.controller.PlantFormController;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
    private PlantService plantService;

    User mockUser = new User("Test", "Test", "test@gmail.com", LocalDate.now());


    @InjectMocks
    private static PlantFormController plantFormController;

    @BeforeEach
    public void initial() {
        LocalDate date = LocalDate.of(2024, 3, 27);
        Garden test_garden = new Garden(
                "test",
                "test",
                "test",
                "test",
                "80",
                "test",
                1.0f,
                mockUser

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
        Plant mockPlant = Mockito.spy(Plant.class);
        when(mockPlant.getPlantId()).thenReturn(1L);
        LocalDate date = LocalDate.of(2024, 3, 27);
        String gardenId = "1";
        String gardenName = "test";
        String plantPictureInput = "image.jpg";

        mockMvc.perform(post("/my-gardens/{gardenId}/create-new-plant",  gardenId).with(csrf())
                        .param("plantName","test")
                        .param("plantCount", String.valueOf(1))
                        .param("plantDescription","test")
                        .param("plantDate", String.valueOf(date))
                        .param("plantPictureInput", plantPictureInput)
                        .param("gardenId", String.valueOf(1L)))
                .andDo(MockMvcResultHandlers.print());
        Mockito.verify(plantService, Mockito.times(1)).addPlant("test", 1, "test", date, 1L);
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_plantEdited() throws Exception {
        // Create a mock Plant object using Mockito.spy
        Plant mockPlant = Mockito.spy(Plant.class);
        when(mockPlant.getPlantId()).thenReturn(1L);

        // Prepare test data
        String gardenId = "1";
        LocalDate date = LocalDate.of(2024, 3, 27);

        // Create a mock file for upload
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // This should match the name of the form field for the file in the request
                "image.jpg",         // Filename
                "image/jpeg",        // Content type
                "image data".getBytes() // File content
        );

        // Perform the request with MockMvc
        mockMvc.perform(MockMvcRequestBuilders.multipart("/my-gardens/{gardenId}/{plantId}/edit", gardenId, mockPlant.getPlantId())
                        .file(mockFile) // Attach the file using the file() method
                        .param("plantName", "test")
                        .param("plantCount", "1")
                        .param("plantDescription", "test")
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andDo(MockMvcResultHandlers.print());

        // Verify the method call
        Mockito.verify(plantService, Mockito.times(1)).updatePlant(1L, "test", 1F, "test", date);
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
        Plant mockPlant = Mockito.spy(Plant.class);
        when(mockPlant.getPlantId()).thenReturn(1L);
        String gardenId = "1";
        String gardenName = "test";
        String plantPictureInput = "image.jpg";
        mockMvc.perform(post("/my-gardens/{gardenId}/create-new-plant", gardenId)
                        .contentType(MediaType.MULTIPART_FORM_DATA) // Set content type to multipart/form-data
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", plantPictureInput))
                .andDo(MockMvcResultHandlers.print());

        Mockito.verify(plantService, Mockito.never()).addPlant(plantName, plantCount, plantDescription, date, Long.parseLong(gardenId));
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
        String gardenName = "test";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        String plantPictureInput = "image.jpg";
        mockMvc.perform(post("/my-gardens/{gardenId}/{plantId}/edit", gardenId, gardenName, plantId, plantName)
                        .contentType(MediaType.MULTIPART_FORM_DATA) // Set content type to multipart/form-data
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", plantPictureInput))
                .andDo(MockMvcResultHandlers.print());

        Mockito.verify(plantService, Mockito.never()).updatePlant(
                Long.parseLong(plantId), plantName, (float)plantCount, plantDescription, date);

    }

    @ParameterizedTest
    @CsvSource({
            "!",
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length" +
                    "a very long plant name that exceeds the maximum length",
            "''", "[", "{", "|", "$$", "o_o", "test@gmail.com", ":", ";"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editDescriptionVariantsFail(String plantName) throws Exception {
        String gardenId = "1";
        String gardenName = "test";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        String plantPictureInput = "image.jpg";
        mockMvc.perform(post("/my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit", gardenId, gardenName, plantId, plantName)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .param("plantName", plantDescription)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantName)
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", plantPictureInput))
                .andDo(MockMvcResultHandlers.print());

        Mockito.verify(plantService, Mockito.never()).updatePlant(
                Long.parseLong(plantId), plantName, (float)plantCount, plantDescription, date);

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
        // try catch for different date input types
        try {
            formattedDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            //checks that if input value cannot be parsed into a date then it does not interact with plantService
            Mockito.verifyNoInteractions(plantService);
            return;
        }
        String plantPictureInput = "image.jpg";
        String gardenId = "1";
        String gardenName = "test";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        String plantName = "test";

        mockMvc.perform(post("/my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit", gardenId, gardenName, plantId, plantName)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", formattedDate.toString())
                        .param("gardenId", gardenId)
                .param("plantPictureInput", plantPictureInput))
                .andDo(MockMvcResultHandlers.print());

        Mockito.verify(plantService, Mockito.never()).updatePlant(
                Long.parseLong(plantId), plantName, (float)plantCount, plantDescription, formattedDate);

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
        String gardenName = "test";
        String plantName = "test name";
        String plantId = "1";
        String plantDescription = "standardPlant";
        String plantPictureInput = "image.jpg";
        LocalDate date = LocalDate.of(2024, 3, 28);

        // try catch for different input types
        try {
            Float.parseFloat(plantCount);
        } catch (NumberFormatException e) {
            // If plant count is non numeric it will throw an error and won't interact with plantService
            Mockito.verifyNoInteractions(plantService);
            return;
        }

        mockMvc.perform(post("/my-gardens/{gardenId}/{plantId}/edit", gardenId, gardenName, plantId, plantName)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", plantCount)
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", plantPictureInput))
                .andDo(MockMvcResultHandlers.print());
        Mockito.verify(plantService, Mockito.never()).updatePlant(
                Long.parseLong(plantId), plantName, Float.parseFloat(plantCount), plantDescription, date);
    }

    @ParameterizedTest
    @CsvSource({
            "11/11/2000", "09/05/1253", "01/02/0003", "07/06/9999"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editDateVariantsPass(String date) throws Exception {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate formattedDate = LocalDate.parse(date, formatter);

        String gardenId = "1";
        String gardenName = "test";
        String plantId = "1";
        String plantDescription = "standardPlant";
        String plantPictureInput = "image.jpg";
        int plantCount = 1;
        String plantName = "test";

        mockMvc.perform(post("/my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit", gardenId, gardenName, plantId, plantName)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantPictureInput", plantPictureInput)
                        .param("plantDate", formattedDate.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", plantPictureInput))
                .andDo(MockMvcResultHandlers.print());

        Mockito.verify(plantService, Mockito.times(1)).updatePlant(
                Long.parseLong(plantId), plantName, (float)plantCount, plantDescription, formattedDate);

    }

    @ParameterizedTest
    @CsvSource({
            "plant name", "PLANT NAME", "plantName", "c00l pl4nt", "this, is. a-real_plant"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editNameVariantsPass(String plantName) throws Exception {
        String gardenId = "1";
        String gardenName = "test";
        String plantId = "1";
        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile plantPictureInput = new MockMultipartFile(
                "plantPictureInput", "image.jpg", MediaType.IMAGE_JPEG_VALUE, "some image data".getBytes());
        mockMvc.perform(post("/my-gardens/{gardenId}/{plantId}/edit", gardenId, plantId)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", String.valueOf(plantPictureInput)))

                .andDo(MockMvcResultHandlers.print());

        Mockito.verify(plantService,  Mockito.times(1)).updatePlant(
                Long.parseLong(plantId), plantName, (float)plantCount, plantDescription, date);

    }

    @ParameterizedTest
    @CsvSource({
            "plant description", "PLANT", "plantDescription", "c00l pl4nt", "this, is. a-real_plant"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editDescriptionVariantsPass(String plantDescription) throws Exception {
        String gardenId = "1";
        String gardenName = "test";
        String plantId = "1";
        String plantName = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        String plantPictureInput = "image.jpg";
        mockMvc.perform(post("/my-gardens/{gardenId}/{plantId}/edit", gardenId, gardenName, plantId, plantName)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", String.valueOf(plantCount))
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", plantPictureInput))
                .andDo(MockMvcResultHandlers.print());

        Mockito.verify(plantService, Mockito.times(1)).updatePlant(
                Long.parseLong(plantId), plantName, (float)plantCount, plantDescription, date);

    }

    @ParameterizedTest
    @CsvSource({
            "1", "1.0", "9999", "0.1", "9987.123"
    })
    @WithMockUser(username = "test@gmail.com")
    public void plantFormController_editCountVariantsPass(String plantCount) throws Exception {
        String gardenId = "1";
        String gardenName = "test";
        String plantName = "test name";
        String plantId = "1";
        String plantDescription = "standardPlant";
        LocalDate date = LocalDate.of(2024, 3, 28);
        String plantPictureInput = "image.jpg";
        mockMvc.perform(post("/my-gardens/{gardenId}/{plantId}/edit", gardenId, gardenName, plantId, plantName)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .param("plantName", plantName)
                        .param("plantCount", plantCount)
                        .param("plantDescription", plantDescription)
                        .param("plantDate", date.toString())
                        .param("gardenId", gardenId)
                        .param("plantPictureInput", plantPictureInput))

                .andDo(MockMvcResultHandlers.print());


        Float.parseFloat(plantCount);
        Mockito.verify(plantService, Mockito.times(1)).updatePlant(
                Long.parseLong(plantId), plantName, Float.parseFloat(plantCount), plantDescription, date);
    }

}
