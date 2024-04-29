package nz.ac.canterbury.seng302.gardenersgrove.integration;


import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.repository.PlantRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Using {@link SpringBootTest} instead
 * of @DataJpaTest fixed the tests
 */
@SpringBootTest
@Import({GardenService.class, PlantService.class})
public class PlantServiceTest {


    private static PlantRepository plantRepository;
    private static GardenService gardenService;
    private static PlantService plantService;
    private static FileService fileService;

    @BeforeAll
    public static void setup() {
        gardenService = Mockito.mock(GardenService.class);
        plantRepository = Mockito.mock(PlantRepository.class);
        plantService = new PlantService(plantRepository,gardenService,fileService);
    }
    @Test
    public void testAddPlant() {
        // Given
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        Garden garden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                15);
        List<Plant> mockPlantsList = new ArrayList<>();

        // Mocks for addPlant
        Mockito.when(gardenService.findById(1L)).thenReturn(Optional.of(garden));
        Mockito.when(plantRepository.save(Mockito.any(Plant.class)))
                .thenAnswer(invocation -> {
                    mockPlantsList.add(invocation.getArgument(0));
                    return invocation.getArgument(0);
                });
        // Mocks for getPlants
        Mockito.when(plantService.getPlants()).thenReturn(mockPlantsList);

        // When
        plantService.addPlant("John's Plant", 3, "Plant owned by John", dateOfPlanting, 1L);
        Plant resultPlant = plantService.getPlants().get(0);

        // Then
        Assertions.assertEquals(resultPlant.getPlantName(),"John's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(),3);
        Assertions.assertEquals(resultPlant.getPlantDescription(),"Plant owned by John");
        Assertions.assertEquals(resultPlant.getPlantDate(),dateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden(),garden);

    }

    @Test
    public void testGetPlants() {
        // Given
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        Garden garden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                15);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);
        List<Plant> mockPlantsList = new ArrayList<>();
        mockPlantsList.add(plant);

        // Mocks for addPlant
        Mockito.when(gardenService.findById(1L)).thenReturn(Optional.of(garden));
        Mockito.when(plantRepository.save(plant)).thenReturn(plant);
        // Mock for getPlants
        Mockito.when(plantRepository.findAll()).thenReturn(mockPlantsList);

        // When
        plantService.addPlant("John's Plant", 3, "Plant owned by John", dateOfPlanting, 1L);
        Plant resultPlant = plantService.getPlants().get(0);

        // Then
        Assertions.assertEquals(resultPlant.getPlantName(),"John's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(),3);
        Assertions.assertEquals(resultPlant.getPlantDescription(),"Plant owned by John");
        Assertions.assertEquals(resultPlant.getPlantDate(),dateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden(),garden);

    }

    @Test
    public void testFindById() {
        // Given
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        Garden garden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                15);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);

        // Mocks for addPlant
        Mockito.when(gardenService.findById(1L)).thenReturn(Optional.of(garden));
        Mockito.when(plantRepository.save(plant)).thenReturn(plant);
        // Mock for findById
        Mockito.when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));

        // When
        plantService.addPlant("John's Plant", 3, "Plant owned by John", dateOfPlanting, 1L);
        Optional <Plant> resultOptionalPlant = plantService.findById(1L);

        // Then
        Assertions.assertTrue(resultOptionalPlant.isPresent());
        Plant resultPlant = resultOptionalPlant.get();
        Assertions.assertEquals(resultPlant.getPlantName(),"John's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(),3);
        Assertions.assertEquals(resultPlant.getPlantDescription(),"Plant owned by John");
        Assertions.assertEquals(resultPlant.getPlantDate(),dateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden(),garden);
    }
    @Test
    public void testUpdatePlant() {
        // Given
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        LocalDate newDateOfPlanting = LocalDate.of(2023, 2, 13);
        Garden garden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                15);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);
        Plant newPlant = new Plant("Jane's Plant", 4, "Plant owned by Jane", newDateOfPlanting, garden);

        // Mocks for addPlant
        Mockito.when(gardenService.findById(1L)).thenReturn(Optional.of(garden));
        Mockito.when(plantRepository.save(plant)).thenReturn(plant);
        // Mock for updatePlant
        Mockito.when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));

        // When
        plantService.addPlant("John's Plant", 3, "Plant owned by John", dateOfPlanting, 1L);
        Plant resultPlant = plantService.updatePlant(1L, newPlant);

        // Then
        Assertions.assertEquals(resultPlant.getPlantName(), "Jane's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(), 4);
        Assertions.assertEquals(resultPlant.getPlantDescription(), "Plant owned by Jane");
        Assertions.assertEquals(resultPlant.getPlantDate(), newDateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden(), garden);
    }

    @Test
    public void testUpdatePlantOverloadedConstructor() {
        // Given
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        LocalDate newDateOfPlanting = LocalDate.of(2023, 2, 13);
        Garden garden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                15);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);

        // Mocks for addPlant
        Mockito.when(gardenService.findById(1L)).thenReturn(Optional.of(garden));
        Mockito.when(plantRepository.save(plant)).thenReturn(plant);
        // Mock for updatePlant
        Mockito.when(plantRepository.findById(1L)).thenReturn(Optional.of(plant));

        // When
        plantService.addPlant("John's Plant", 3, "Plant owned by John", dateOfPlanting, 1L);
        Plant resultPlant = plantService.updatePlant(1L, "Jane's Plant", 4f, "Plant owned by Jane", newDateOfPlanting);

        // Then
        Assertions.assertEquals(resultPlant.getPlantName(), "Jane's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(), 4f);
        Assertions.assertEquals(resultPlant.getPlantDescription(), "Plant owned by Jane");
        Assertions.assertEquals(resultPlant.getPlantDate(), newDateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden(), garden);
    }

}
