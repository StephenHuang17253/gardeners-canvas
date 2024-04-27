package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

/**
 * Controller for viewing all the created Gardens
 */
@Controller
public class MyGardensController {

    Logger logger = LoggerFactory.getLogger(MyGardensController.class);

    private final GardenService gardenService;

    private final PlantService plantService;

    private final FileService fileService;

    @Autowired
    public MyGardensController(GardenService gardenService, PlantService plantService, FileService fileService) {
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.fileService = fileService;
    }

    /**
     * Maps the myGardensPage html file to /my-gardens url
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens")
    public String myGardens(Model model) {
        logger.info("GET /my-gardens");
        model.addAttribute("myGardens", gardenService.getGardens());
        model.addAttribute("gardenCount", gardenService.getGardens().size());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        return "myGardensPage";
    }

    /**
     * Gets all the users created gardens
     * and maps them all and there attributes to the gardenDetailsPage
     * but with the custom url of /my-gardens/{gardenId}={gardenName}
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens/{gardenId}={gardenName}")
    public String showGardenDetails(@PathVariable("gardenId") String gardenIdString,
                                    @PathVariable String gardenName,
                                    Model model) {
        logger.info("GET /my-gardens/{}-{}", gardenIdString, gardenName);

        long gardenId = Long.parseLong(gardenIdString);
        Optional<Garden> optionalGarden = gardenService.findById(gardenId);
        model.addAttribute("myGardens", gardenService.getGardens());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            model.addAttribute("gardenName", garden.getGardenName());
            model.addAttribute("gardenLocation", garden.getGardenLocation());
            model.addAttribute("gardenSize", garden.getGardenSize());
            model.addAttribute("gardenId",gardenIdString);
            model.addAttribute("plants",garden.getPlants());
            model.addAttribute("totalPlants",garden.getPlants().size());
            return "gardenDetailsPage";
        } else {
            return "404";
        }
    }

    /**
     * This function is called when a user tries to update a plants image directly from the My Garden's page
     * instead of one of the plant forms.
     * @param gardenIdString id of the garden being edited
     * @param gardenName name of the garden being edited
     * @param plantId id of the plant being edited
     * @param plantPicture the new picture
     * @param model the model
     * @return thymeleaf gardenDetails
     */
    @PostMapping("/my-gardens/{gardenId}={gardenName}/{plantId}/updateImage")
    public String updatePlantImage(@PathVariable("gardenId") String gardenIdString,
                                   @PathVariable String gardenName,
                                   @PathVariable String plantId,
                                   @RequestParam("plantPictureInput") MultipartFile plantPicture,
                                   Model model) {
        logger.info("GET /my-gardens/{}-{}", gardenIdString, gardenName);

        long gardenId = Long.parseLong(gardenIdString);
        Optional<Garden> optionalGarden = gardenService.findById(gardenId);
        model.addAttribute("myGardens", gardenService.getGardens());

        Optional<Plant> plantToUpdate = plantService.findById(Long.parseLong(plantId));
        if(!plantToUpdate.isPresent())
        {
            return "404";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);


        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }
        if (!plantPictureResult.valid()) {
            logger.info("Plant picture validation failed");
            model.addAttribute("plantPictureError", plantPictureResult);
            return "gardenDetailsPage";
        }

        if (plantPictureResult.valid()) {

            if (!plantPicture.isEmpty()) {
                logger.info("Updating plant picture");
                plantService.updatePlantPicture(plantToUpdate.get(), plantPicture);
            }
            logger.info("Plant updated successfully");

            return "redirect:/my-gardens/{gardenId}={gardenName}";
        } else {
            return "404";
        }
    }



}

