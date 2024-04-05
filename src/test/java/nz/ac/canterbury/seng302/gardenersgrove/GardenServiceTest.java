package nz.ac.canterbury.seng302.gardenersgrove;


import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.repository.GardenRepository;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.util.Assert;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;


@DataJpaTest
@Import(GardenService.class)
public class GardenServiceTest {

    @Autowired
    private GardenRepository gardenRepository;

    @Test
    public void testGetGardens() {

        GardenService gardenService = new GardenService(gardenRepository);
        Garden garden1 = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                10);
        Garden garden2 = new Garden(
                "Jane's Garden",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                "20 Kirkwood Avenue, Upper Riccarton, Christchurch 8041, New Zealand",
                20);
        gardenRepository.save(garden1);
        gardenRepository.save(garden2);
        List<Garden> gardens = gardenService.getGardens();
        Assertions.assertTrue(gardens.contains(garden1));
        Assertions.assertTrue(gardens.contains(garden2));
    }
    @Mock
    GardenRepository gardenRepo;
    @Test
    public void testFindById() {

        Garden garden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                15);
        when(gardenRepo.findById(1L)).thenReturn(Optional.of(garden));

        GardenService gardenService = new GardenService(gardenRepo);
        Optional<Garden> optionalGarden = gardenService.findById(1L);

        Assertions.assertTrue(optionalGarden.isPresent());
    }

    @Test
    public void testAddGarden() {
        GardenService gardenService = new GardenService(gardenRepository);
        Garden garden = gardenService.addGarden(new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                15));
        Assertions.assertEquals(garden.getGardenName(), "John's Garden");
        Assertions.assertEquals(garden.getGardenAddress(), "114 Ilam Road");
        Assertions.assertEquals(garden.getGardenSuburb(), "Ilam");
        Assertions.assertEquals(garden.getGardenCity(), "Christchurch");
        Assertions.assertEquals(garden.getGardenPostcode(), "8041");
        Assertions.assertEquals(garden.getGardenCountry(), "New Zealand");
        Assertions.assertEquals(garden.getGardenLocation(), "114 Ilam Road, Ilam, Christchurch 8041, New Zealand");
        Assertions.assertEquals(garden.getGardenSize(), 15);
    }

    @Test
    public void testUpdateGarden() {
        GardenService gardenService = new GardenService(gardenRepository);
        Garden oldGarden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                10);
        Garden newGarden = new Garden(
                "Jane's Garden",
                "20 Kirkwood Avenue",
                "Upper Riccarton",
                "Christchurch",
                "8041",
                "New Zealand",
                "20 Kirkwood Avenue, Upper Riccarton, Christchurch 8041, New Zealand",
                20);
        gardenService.addGarden(oldGarden);
        gardenService.updateGarden(oldGarden.getGardenId(), newGarden);
        Assertions.assertEquals(oldGarden.getGardenName(), newGarden.getGardenName());
        Assertions.assertEquals(oldGarden.getGardenAddress(), newGarden.getGardenAddress());
        Assertions.assertEquals(oldGarden.getGardenSuburb(), newGarden.getGardenSuburb());
        Assertions.assertEquals(oldGarden.getGardenCity(), newGarden.getGardenCity());
        Assertions.assertEquals(oldGarden.getGardenPostcode(), newGarden.getGardenPostcode());
        Assertions.assertEquals(oldGarden.getGardenCountry(), newGarden.getGardenCountry());
        Assertions.assertEquals(oldGarden.getGardenLocation(), newGarden.getGardenLocation());
        Assertions.assertEquals(oldGarden.getGardenSize(), newGarden.getGardenSize());


    }

    @Test
    public void testAddPlantToGarden() {
        // Given
        LocalDate dateOfPlanting = LocalDate.of(2024, 3, 14);
        GardenService gardenService = new GardenService(gardenRepository);
        Garden garden = new Garden(
                "John's Garden",
                "114 Ilam Road",
                "Ilam",
                "Christchurch",
                "8041",
                "New Zealand",
                "114 Ilam Road, Ilam, Christchurch 8041, New Zealand",
                10);
        Plant plant = new Plant("John's Plant", 3, "Plant owned by John", dateOfPlanting, garden);

        // When
        gardenService.addGarden(garden);
        gardenService.addPlantToGarden(garden.getGardenId(), plant);

        // That
        Assertions.assertEquals(1, garden.getPlants().size());
        Plant resultPlant = garden.getPlants().get(0);
        Assertions.assertEquals(resultPlant.getPlantName(),"John's Plant");
        Assertions.assertEquals(resultPlant.getPlantCount(),3);
        Assertions.assertEquals(resultPlant.getPlantDescription(),"Plant owned by John");
        Assertions.assertEquals(resultPlant.getPlantDate(),dateOfPlanting);
        Assertions.assertEquals(resultPlant.getGarden(),garden);
    }


}
