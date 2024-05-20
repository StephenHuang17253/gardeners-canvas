package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.List;
import java.util.Optional;

/**
 * Controller for viewing all the created Gardens
 */
@Controller
@SessionAttributes("userGardens")
public class GardensController {

    Logger logger = LoggerFactory.getLogger(GardensController.class);

    private final GardenService gardenService;

    private final SecurityService securityService;

    private final PlantService plantService;

    private final FileService fileService;

    /**
     * Constructor for the GardensController with {@link Autowired} to
     * connect this controller with other services
     *
     * @param gardenService service to access garden repository
     * @param securityService service to access security methods
     * @param plantService service to access plant repository
     * @param fileService service to manage files
     */
    @Autowired
    public GardensController(GardenService gardenService, SecurityService securityService, PlantService plantService, FileService fileService) {
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
        return "gardenDetailsPage";

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
     * This function is called when trying to access another user's gardens.
     * @param model - the model
     * @param userId - id of the user being viewed
     * @return thymeleaf gardensPage
     */
    @GetMapping("{userId}/gardens")
    public String friendsGardens(Model model,
                                 @PathVariable("userId") Long userId,
                                 HttpServletResponse response) {
        logger.info("GET /my-gardens");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        User friend;
        try {
            friend = securityService.checkFriendship(userId);
            if (friend == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return "403";
            }
        } catch(Exception exception) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }



        String friendName = String.format("%s %s", friend.getFirstName(), friend.getLastName());
        List<Garden> gardenList = friend.getGardens();
        model.addAttribute("friendName", friendName);
        model.addAttribute("userGardens", gardenList);

        return "gardensPage";
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

