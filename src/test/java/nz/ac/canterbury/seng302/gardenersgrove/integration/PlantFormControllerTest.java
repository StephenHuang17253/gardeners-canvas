package nz.ac.canterbury.seng302.gardenersgrove.integration;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.ProfanityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PlantFormControllerTest {

    @Autowired
    WebApplicationContext webApplicationContext;
    private MockMvc mockMvc;

    @Autowired
    private PlantService plantService;
    @Autowired
    private UserService userService;
    @Autowired
    private GardenService gardenService;

    @MockBean
    private ProfanityService profanityService;

    User mockUser = new User("Test", "Test", "test@gmail.com", LocalDate.now());
    Garden testGarden;

    Garden anotherGarden;
    List<Plant> plantList = new ArrayList<>();

    @BeforeAll
    void before_or_after_all() {

        Mockito.when(profanityService.containsProfanity(Mockito.any())).thenReturn(false);

        userService.addUser(mockUser, "1es1P@ssword");
        LocalDate date1 = LocalDate.of(2024, 3, 27);
        testGarden = gardenService.addGarden(new Garden(
                "test",
                "test",
                "test",
                "test",
                "test",
                "80",
                "test",
                10.0,
                false,
                "",
                "",
                mockUser));

        anotherGarden = gardenService.addGarden(new Garden(
                "anotherGarden",
                "test",
                "test",
                "test",
                "test",
                "80",
                "test",
                10.0,
                false,
                "",
                "",
                mockUser));

        plantList.add(plantService.addPlant("testName1",
                1,
                "testDescription1",
                date1,
                testGarden.getGardenId()));

        plantList.add(plantService.addPlant("testName2",
                1,
                "testDescription2",
                date1,
                testGarden.getGardenId()));

        plantList.add(plantService.addPlant("testName3",
                1,
                "testDescription2",
                date1,
                testGarden.getGardenId()));

        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void heartbeat() throws Exception {
        String gardenId = "1";
        mockMvc.perform(get("/my-gardens/{gardenId}/create-new-plant", gardenId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_OnePlantAdded() throws Exception {

        Plant expectedPlant = plantList.get(0);

        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/create-new-plant", testGarden.getGardenId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", expectedPlant.getPlantName())
                .param("plantCount", String.valueOf(expectedPlant.getPlantCount()))
                .param("plantDescription", expectedPlant.getPlantDescription())
                .param("plantDate", expectedPlant.getPlantDate().toString()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertEquals(expectedPlant.getPlantName(), actualPlant.getPlantName());
        Assertions.assertEquals(expectedPlant.getPlantDescription(), actualPlant.getPlantDescription());
        Assertions.assertEquals(expectedPlant.getPlantCount(), actualPlant.getPlantCount());

    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_plantEdited() throws Exception {

        Plant expectedPlant = plantList.get(1);

        String newPlantName = "test";
        String newPlantDescription = "standardPlant";
        int newPlantCount = 1;
        LocalDate newPlantDate = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // This should match the name of the form field for the file in the
                                     // request
                "image.jpg", // Filename
                "image/jpeg", // Content type
                "image data".getBytes() // File content
        );

        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", newPlantName)
                .param("plantCount", String.valueOf(newPlantCount))
                .param("plantDescription", newPlantDescription)
                .param("plantDate", newPlantDate.toString()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertEquals(newPlantName, actualPlant.getPlantName());
        Assertions.assertEquals(newPlantDescription, actualPlant.getPlantDescription());
        Assertions.assertEquals(newPlantCount, actualPlant.getPlantCount());
        Assertions.assertEquals(newPlantDate, actualPlant.getPlantDate());
    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void importPlant_validPlant_addsPlant_returnsRedirect() throws Exception {

        Plant expectedPlant = plantList.get(2);
        MvcResult response = mockMvc.perform(MockMvcRequestBuilders.post("/import-plant").
                        param("gardenId", String.valueOf(anotherGarden.getGardenId()))
                        .param("plantId", String.valueOf(expectedPlant.getPlantId())))
                .andExpect(status().is3xxRedirection()).andReturn();


        long newPlantId = Long.parseLong((String) response.getModelAndView().getModelMap().getAttribute("plantId"));

        Optional<Garden> updatedGarden = gardenService.getGardenById(anotherGarden.getGardenId());
        Plant importedPlant = plantService.findById(newPlantId).get();
        Plant newGardenPlant = updatedGarden.get().getPlants().get(0);

        Assertions.assertEquals(expectedPlant.getPlantName(), importedPlant.getPlantName());
        Assertions.assertEquals(expectedPlant.getPlantName(), newGardenPlant.getPlantName());

        Assertions.assertEquals(1, updatedGarden.get().getPlants().size());
        Assertions.assertEquals(importedPlant.getPlantName(), newGardenPlant.getPlantName());
        Assertions.assertEquals(importedPlant.getPlantPictureFilename(), newGardenPlant.getPlantPictureFilename());

    }

    @Test
    @WithMockUser(username = "test@gmail.com")
    void importPlant_invalidPlant_returns404() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/import-plant").
                param("gardenId", String.valueOf(0L))
                .param("plantId", String.valueOf(0L)))
                .andExpect(status().is4xxClientError());

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
    void plantFormController_addNameVariantsFail(String plantName, int plantCount, String plantDescription,
            LocalDate date) throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/create-new-plant", testGarden.getGardenId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", date.toString()));

        Plant expectedPlant = plantList.get(plantList.size() - 1);
        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertNotEquals(plantName, actualPlant.getPlantName());

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
    void plantFormController_editNameVariantsFail(String plantName) throws Exception {

        Plant expectedPlant = plantList.get(1);

        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", date.toString()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertNotEquals(plantName, actualPlant.getPlantName());

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
                    "very long plant name that exceeds the maximum length",
            "[", "{", "|", "$$", ":", ";",
            "-1", "0", "-1.0", "!", "{}", "99999999999999999999999999999999999999999", "5..5", "5.5.7", "😘😗😙😚☺️🙂🤗"
    })
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_editDescriptionVariantsFail(String plantDescription) throws Exception {

        Plant expectedPlant = plantList.get(1);

        String plantName = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", date.toString()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertNotEquals(plantDescription, actualPlant.getPlantDescription());

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
            "test", "''", "{}", "a/12/2000", "12/a/2000",
            "71/1/1", "1/12/2000", "12/1/2000", "01/02/2", "01/05/0000"
    })
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_editDateVariantsFail(String date) throws Exception {

        Plant expectedPlant = plantList.get(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate formattedDate;
        // try catch for different date input types
        try {
            formattedDate = LocalDate.parse(date, formatter);
        } catch (DateTimeParseException e) {
            Assertions.assertNotEquals(date,
                    expectedPlant.getPlantDate().toString());
            return;
        }
        String plantDescription = "standardPlant";
        int plantCount = 1;
        String plantName = "test";
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request// TODO: handle exception
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", String.valueOf(formattedDate)));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertNotEquals(formattedDate.toString(), actualPlant.getFormattedPlantDate());

    }

    @ParameterizedTest
    @CsvSource({
            "0", "0.9", "1000001", "1000000.00000000000000000001", "-1", "0.0", "e", "3e3"
    })
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_editCountVariantsFail(String plantCount) throws Exception {

        Plant expectedPlant = plantList.get(1);

        String plantName = "test name";

        String plantDescription = "standardPlant";
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", plantCount)
                .param("plantDescription", plantDescription)
                .param("plantDate", date.toString()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertNotEquals(plantCount, String.valueOf(actualPlant.getPlantCount()));

    }

    @ParameterizedTest
    @CsvSource({
            "11/11/2000", "07/06/2023"
    })
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_editDateVariantsPass(String date) throws Exception {

        Plant expectedPlant = plantList.get(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        LocalDate formattedDate = LocalDate.parse(date, formatter);

        String plantDescription = "standardPlant";
        int plantCount = 1;
        String plantName = "test";
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", String.valueOf(formattedDate)));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertEquals(formattedDate, actualPlant.getPlantDate());
    }

    @ParameterizedTest
    @CsvSource({
            "plant name", "PLANT NAME", "plantName", "c00l pl4nt", "this, is. a-real_plant"
    })
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_editNameVariantsPass(String plantName) throws Exception {

        Plant expectedPlant = plantList.get(1);

        String plantDescription = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", date.toString()))
                .andExpect(MockMvcResultMatchers
                        .redirectedUrl("/my-gardens/" + testGarden.getGardenId()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertEquals(plantName, actualPlant.getPlantName());

    }

    @ParameterizedTest
    @CsvSource({
            "plant description", "PLANT", "plantDescription", "c00l pl4nt", "this, is. a-real_plant",
            "a very long plant name that exceeds the maximum length" +
                    "a very long plant description that does not exceed the maximum length" +
                    "a very long plant description that does not exceed the maximum length" +
                    "a very long plant description that does not exceed the maximum length" +
                    "a very long plant description that does not exceed the maximum length",
            "😚☺️🙂🤗hello"
    })
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_editDescriptionVariantsPass(String plantDescription) throws Exception {
        Plant expectedPlant = plantList.get(1);
        String plantName = "standardPlant";
        int plantCount = 1;
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", date.toString()))
                .andExpect(MockMvcResultMatchers
                        .redirectedUrl("/my-gardens/" + testGarden.getGardenId()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertEquals(plantDescription, actualPlant.getPlantDescription());

    }

    @ParameterizedTest
    @CsvSource({
            "1", "9999", "1000000", "1.0", "100.0000000000000"
    })
    @WithMockUser(username = "test@gmail.com")
    void plantFormController_editCountVariantsPass(String plantCount) throws Exception {

        Plant expectedPlant = plantList.get(1);

        String plantName = "test name";
        String plantDescription = "standardPlant";
        LocalDate date = LocalDate.of(2024, 3, 28);
        MockMultipartFile mockFile = new MockMultipartFile(
                "plantPictureInput", // Form field name
                "default_plant.png", // Filename
                "image/png", // Content type
                "image data".getBytes() // File content as byte array
        );
        mockMvc.perform(MockMvcRequestBuilders
                .multipart("/my-gardens/{gardenId}/{plantId}/edit", testGarden.getGardenId(),
                        expectedPlant.getPlantId())
                .file(mockFile) // Attach the file to the request
                .param("plantName", plantName)
                .param("plantCount", String.valueOf(plantCount))
                .param("plantDescription", plantDescription)
                .param("plantDate", date.toString()))
                .andExpect(MockMvcResultMatchers
                        .redirectedUrl("/my-gardens/" + testGarden.getGardenId()));

        Optional<Plant> optionalPlant = plantService.findById(expectedPlant.getPlantId());
        Assertions.assertTrue(optionalPlant.isPresent());
        Plant actualPlant = optionalPlant.get();

        Assertions.assertEquals((int) Double.parseDouble(plantCount.replace(',', '.')), actualPlant.getPlantCount());

    }

}
