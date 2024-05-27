package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import java.util.Optional;

/**
 * Controller for viewing all the created Gardens
 */
@Controller
@SessionAttributes("userGardens")
public class MyGardensController {

    Logger logger = LoggerFactory.getLogger(MyGardensController.class);

    private final GardenService gardenService;

    private final SecurityService securityService;

    private final PlantService plantService;

    private final FileService fileService;

    @Autowired
    public MyGardensController(GardenService gardenService, SecurityService securityService, PlantService plantService, FileService fileService) {
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.fileService = fileService;
        this.securityService = securityService;
    }

    /**
     * Maps the myGardensPage html file to /my-gardens url
     *
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens")
    public String myGardens(Model model) {
        logger.info("GET /my-gardens");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        return "myGardensPage";
    }

    /**
     * Gets all the users created gardens
     * and maps them all and there attributes to the gardenDetailsPage
     * but with the custom url of /my-gardens/{gardenId}
     *
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens/{gardenId}")
    public String showGardenDetails(@PathVariable Long gardenId,
                                    HttpServletResponse response,
                                    Model model) {
        logger.info("GET /my-gardens/{}", gardenId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (!optionalGarden.isPresent()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();
        logger.info("Garden owner ID: {}, Authenticated user ID: {}",
                garden.getOwner().getId(),
                authentication.getName());

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }


        model.addAttribute("gardenName", garden.getGardenName());
        model.addAttribute("gardenLocation", garden.getGardenLocation());
        model.addAttribute("gardenSize", garden.getGardenSize());
        model.addAttribute("gardenId", gardenId);
        model.addAttribute("plants", garden.getPlants());
        model.addAttribute("totalPlants", garden.getPlants().size());
        model.addAttribute("makeGardenPublic", garden.getIsPublic());
        return "gardenDetailsPage";

    }

    /**
     * This function creates a post mapping for updating the garden's isPublic boolean.
     *
     * @param gardenId - the id of the garden being edited
     * @param makeGardenPublic - the new status of the garden isPublic
     *
     * @return thymeleaf garden detail page
     */
    @PostMapping("/my-gardens/{gardenId}/public")
    public String updateGardenPublicStatus(@PathVariable Long gardenId,
                                           @RequestParam(name = "makeGardenPublic") boolean makeGardenPublic,
                                    HttpServletResponse response,
                                    Model model) {
        logger.info("POST /my-gardens/{gardenId}/public", gardenId);
        logger.info("Value of makeGardenPublic: {}", makeGardenPublic);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (!optionalGarden.isPresent()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();
        logger.info("Garden owner ID: {}, Authenticated user ID: {}",
                garden.getOwner().getId(),
                authentication.getName());

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }

        gardenService.updateGardenPublicity(garden.getGardenId(), makeGardenPublic);
        logger.error("Status of Garden publicity: {}", garden.getIsPublic());

        model.addAttribute("gardenName", garden.getGardenName());
        model.addAttribute("gardenLocation", garden.getGardenLocation());
        model.addAttribute("gardenSize", garden.getGardenSize());
        model.addAttribute("gardenId", gardenId);
        model.addAttribute("plants", garden.getPlants());
        model.addAttribute("totalPlants", garden.getPlants().size());
        model.addAttribute("makeGardenPublic", garden.getIsPublic());
        return "redirect:/my-gardens/{gardenId}";

    }

    /**
     * This function is called when a user tries to update a plants image directly from the My Garden's page
     * instead of one of the plant forms.
     *
     * @param gardenIdString id of the garden being edited
     * @param plantId        id of the plant being edited
     * @param plantPicture   the new picture
     * @param model          the model
     * @return thymeleaf gardenDetails
     */
    @PostMapping("/my-gardens/{gardenId}")
    public String updatePlantImage(@PathVariable("gardenId") String gardenIdString,
                                   @RequestParam("plantId") String plantId,
                                   @RequestParam("plantPictureInput") MultipartFile plantPicture,
                                   Model model) {
        logger.info("POST /my-gardens/{}", gardenIdString);

        long gardenId = Long.parseLong(gardenIdString);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        model.addAttribute("myGardens", gardenService.getGardens());

        Optional<Plant> plantToUpdate = plantService.findById(Long.parseLong((plantId)));
        model.addAttribute("plantToEditId", (Long.parseLong(plantId)));
        if (!plantToUpdate.isPresent()) {
            return "404";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);


        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }

        if (optionalGarden.isPresent()) {

            Garden garden = optionalGarden.get();
            String plantPictureString = getPlantPictureString(plantToUpdate.get().getPlantPictureFilename());
            model.addAttribute("plantPicture", plantPictureString);
            model.addAttribute("gardenName", garden.getGardenName());
            model.addAttribute("gardenLocation", garden.getGardenLocation());
            model.addAttribute("gardenSize", garden.getGardenSize());
            model.addAttribute("gardenId", gardenIdString);
            model.addAttribute("plants", garden.getPlants());
            model.addAttribute("totalPlants", garden.getPlants().size());
            model.addAttribute("makeGardenPublic", garden.getIsPublic());


        } else {
            return "404";
        }


        if (!plantPictureResult.valid()) {
            logger.info("Plant picture validation failed");
            model.addAttribute("plantPictureError", plantPictureResult);
            return "gardenDetailsPage";

        } else {

            if (!plantPicture.isEmpty()) {
                logger.info("Updating plant picture");
                plantService.updatePlantPicture(plantToUpdate.get(), plantPicture);
            }
            logger.info("Plant updated successfully");

            return "redirect:/my-gardens/{gardenId}";
        }

    }

    /**
     * Gets the resource url for the plant picture, or the default plant picture
     * if the plant does not have one
     *
     * @param filename string filename
     * @return string of the plant picture url
     */
    public String getPlantPictureString(String filename) {

        String plantPictureString = "/images/default_plant.png";

        if (filename != null && filename.length() != 0) {
            plantPictureString = MvcUriComponentsBuilder
                    .fromMethodName(PlantFormController.class, "serveFile", filename)
                    .build()
                    .toUri()
                    .toString();
        }
        return plantPictureString;
    }


}

