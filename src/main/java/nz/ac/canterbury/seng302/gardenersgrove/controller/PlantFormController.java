package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import jakarta.servlet.http.HttpServletResponse;

import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Controller for creating a new Plant using a form page
 */
@Controller
public class PlantFormController {

    Logger logger = LoggerFactory.getLogger(PlantFormController.class);

    private final PlantService plantService;
    private final GardenService gardenService;
    private final FileService fileService;
    private final SecurityService securityService;

    @Autowired
    public PlantFormController(PlantService plantService, GardenService gardenService, FileService fileService,
            SecurityService securityService) {
        this.plantService = plantService;
        this.gardenService = gardenService;
        this.fileService = fileService;
        this.securityService = securityService;
    }

    /**
     * Maps the createNewPlantForm html page to /create-new-plant url
     * 
     * @return thymeleaf createNewPlantForm
     */
    @GetMapping("/my-gardens/{gardenId}/create-new-plant")
    public String newPlantForm(@PathVariable Long gardenId,
            @RequestParam(name = "plantName", required = false) String plantName,
            @RequestParam(name = "plantCount", required = false) String plantCount,
            @RequestParam(name = "plantDescription", required = false) String plantDescription,
            @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
            HttpServletResponse response,
            Model model) {
        logger.info("GET /create-new-plant");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        Garden garden = optionalGarden.get();
        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }
        model.addAttribute("gardenId", gardenId); // Pass gardenId to the form
        model.addAttribute("gardenName", garden.getGardenName()); // Pass gardenName to the form
        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);
        model.addAttribute("myGardens", gardenService.getGardens());

        // Sets default plant image
        String plantPictureString = getPlantPictureString("");
        model.addAttribute("plantPicture", plantPictureString);

        return "createNewPlantForm"; // Return the view for creating a new plant
    }

    /**
     * Logic to handle the confirm new plant form button
     * also validates inputs into form and informs the user if their input is
     * invalid
     * 
     * @param plantName        user entered plant name
     * @param plantCount       user entered plant count
     * @param plantDescription user entered plant description
     * @param plantDate        user entered date plant is planted
     * @param model            (map-like) representation of plantName, plantCount,
     *                         plantDescription and plantDate for use in thymeleaf,
     *                         with values being set to relevant parameters provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/my-gardens/{gardenId}/create-new-plant")
    public String submitNewPlantForm(@RequestParam(name = "plantName") String plantName,
            @RequestParam(name = "plantCount", required = false) String plantCount,
            @RequestParam(name = "plantDescription", required = false) String plantDescription,
            @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
            @RequestParam(name = "plantPictureInput") MultipartFile plantPicture,
            @PathVariable("gardenId") Long gardenId,
            HttpServletResponse response,
            Model model) {
        logger.info("POST /create-new-plant");

        int value;
        try {
            value = (int) Double.parseDouble(plantCount);
        } catch (NumberFormatException e) {
            value = 0;
            plantCount = "0";
        }
         

        // logic to handle checking if fields are vaild
        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        ValidationResult plantCountResult = InputValidator.validatePlantCount(plantCount);
        ValidationResult plantNameResult = InputValidator.compulsoryAlphaPlusTextField(plantName, 64);
        ValidationResult plantDescriptionResult = InputValidator.optionalTextField(plantDescription, 512);
        ValidationResult plantDateResult;

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        Garden garden = optionalGarden.get();
        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }

        if (plantDate == null) {
            plantDateResult = ValidationResult.OK;
        } else {
            String dateString = plantDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            plantDateResult = InputValidator.validatePlantDate(dateString);
        }

        // Plant image is optional
        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }

        plantFormErrorText(model, plantPictureResult, plantNameResult, plantCountResult, plantDescriptionResult, plantDateResult);

        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        // Sets default plant image
        String plantPictureString = getPlantPictureString("");
        model.addAttribute("plantPicture", plantPictureString);

        if (!plantPictureResult.valid() || !plantNameResult.valid() || !plantCountResult.valid()
                || !plantDescriptionResult.valid() || !plantDateResult.valid()){
            return "createNewPlantForm";
        }

        int integerPlantCount = Integer.parseInt(String.valueOf(value));
        Plant newPlant = plantService.addPlant(plantName, integerPlantCount, plantDescription, plantDate, gardenId);
        if (!plantPicture.isEmpty()) {
            plantService.updatePlantPicture(newPlant, plantPicture);
        }

        return "redirect:/my-gardens/{gardenId}";
    }

    /**
     * Maps the editPlantForm html page to /create-new-plant url
     * Pre populates the values with the plants record from the database,
     * sends user to 404 page if plant is not found
     * 
     * @return thymeleaf createNewPlantForm
     */
    @GetMapping("/my-gardens/{gardenId}/{plantId}/edit")
    public String editPlantForm(@PathVariable("gardenId") Long gardenId,
            @PathVariable("plantId") Long plantId,
            HttpServletResponse response,
            Model model) {
        logger.info("GET /my-gardens/{gardenId}/{plantId}/edit");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        Garden garden = optionalGarden.get();
        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }
        Optional<Plant> plantToUpdate = plantService.findById(plantId);
        if (plantToUpdate.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        model.addAttribute("gardenId", gardenId); // Pass gardenId to the form
        model.addAttribute("gardenName", garden.getGardenName()); // Pass gardenName to the form
        String plantPicture = plantToUpdate.get().getPlantPictureFilename();
        model.addAttribute("plantPicture", plantPicture);
        model.addAttribute("plantName", plantToUpdate.get().getPlantName());
        model.addAttribute("plantCount", plantToUpdate.get().getPlantCount());
        model.addAttribute("plantDescription", plantToUpdate.get().getPlantDescription());
        model.addAttribute("plantDate", plantToUpdate.get().getPlantDate());
        return "editPlantForm"; // Return the view for creating a new plant
    }

    /**
     * Logic to handle the confirmation of the edit plant form button
     * also validates inputs into form and informs the user if their input is
     * invalid
     *
     * @param plantName        user entered plant name
     * @param plantCount       user entered plant count
     * @param plantDescription user entered plant description
     * @param plantDate        user entered date plant is planted
     * @param model            (map-like) representation of plantName, plantCount,
     *                         plantDescription and plantDate for use in thymeleaf,
     *                         with values being set to relevant parameters provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/my-gardens/{gardenId}/{plantId}/edit")
    public String submiteditPlantForm(@RequestParam(name = "plantName") String plantName,
            @RequestParam(name = "plantCount", required = false) String plantCount,
            @RequestParam(name = "plantDescription", required = false) String plantDescription,
            @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
            @RequestParam(name = "plantPictureInput", required = false) MultipartFile plantPicture,
            @PathVariable("gardenId") Long gardenId,
            @PathVariable("plantId") Long plantId,
            HttpServletResponse response,
            Model model) {
        logger.info("POST /my-gardens/{gardenId}/{plantId}/edit");

        Optional<Plant> plantToUpdate = plantService.findById(plantId);
        if (plantToUpdate.isEmpty()) {
            return "404";
        }

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        Garden garden = optionalGarden.get();
        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }


        int value;
        try {
            value = (int) Double.parseDouble(plantCount);
        } catch (NumberFormatException e) {
            value = 0;
            plantCount = "0";
        }

        // logic to handle checking if fields are vaild
        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        ValidationResult plantNameResult = InputValidator.compulsoryAlphaPlusTextField(plantName, 64);
        ValidationResult plantCountResult = InputValidator.validatePlantCount(plantCount);
        ValidationResult plantDescriptionResult = InputValidator.optionalTextField(plantDescription, 512);
        ValidationResult plantDateResult;
        if (plantDate == null) {
            plantDateResult = ValidationResult.OK;
        } else {
            String dateString = plantDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            plantDateResult = InputValidator.validatePlantDate(dateString);
        }


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }

        plantFormErrorText(model, plantPictureResult, plantNameResult, plantCountResult, plantDescriptionResult, plantDateResult);

        String plantPictureString = plantToUpdate.get().getPlantPictureFilename();
        model.addAttribute("plantPicture", plantPictureString);
        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);

        if (!plantPictureResult.valid() || !plantNameResult.valid() || !plantCountResult.valid()
                || !plantDescriptionResult.valid() || !plantDateResult.valid()){
            return "editPlantForm";
        }
        int integerPlantCount = Integer.parseInt(String.valueOf(value));
        plantService.updatePlant(plantId, plantName, integerPlantCount, plantDescription, plantDate);
        if (!plantPicture.isEmpty()) {
            plantService.updatePlantPicture(plantToUpdate.get(), plantPicture);
        }
        return "redirect:/my-gardens/{gardenId}";
    }

    /**
     * Takes as an input the result of validating the plant name, count, description
     * and date,
     * and prints the appropriate messages.
     * 
     * @param plantNameResult        result of validating name (OK or appropriate
     *                               error)
     * @param plantCountResult       result of validating count (OK or appropriate
     *                               error)
     * @param plantDescriptionResult result of validating description (OK or
     *                               appropriate error)
     */
    private void plantFormErrorText(Model model, ValidationResult plantPictureResult, ValidationResult plantNameResult,
            ValidationResult plantCountResult, ValidationResult plantDescriptionResult,
            ValidationResult plantDateResult) {

        if (!plantPictureResult.valid()) {
            model.addAttribute("plantPictureError", plantPictureResult);
        }

        // notifies the user that the plant Name is invalid (if applicable)
        if (!plantNameResult.valid()) {
            if (plantNameResult == ValidationResult.LENGTH_OVER_LIMIT) {
                plantNameResult.updateMessage("cannot be greater than 64 characters in length");
            } else {
                plantNameResult.updateMessage(
                        "cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes");
            }
            model.addAttribute("PNErrorText", "Plant name " + plantNameResult);
            model.addAttribute("PNErrorClass", "true");
        } else {
            model.addAttribute("PNErrorClass", "null");
        }

        // notifies the user that the plant Count is invalid (if applicable)
        if (!plantCountResult.valid()) {
            model.addAttribute("PCErrorText", plantCountResult);
            model.addAttribute("PCErrorClass", "true");

        } else {
            model.addAttribute("PCErrorClass", "null");
        }

        // notifies the user that the plant Description is invalid (if applicable)
        if (!plantDescriptionResult.valid()) {
            if (plantDescriptionResult.toString().equals(ValidationResult.INVALID_DESCRIPTION.toString())) {
                plantDescriptionResult.updateMessage("Plant description must be less than 512 characters");
            }
            model.addAttribute("PDErrorText", plantDescriptionResult);
            model.addAttribute("PDErrorClass", "true");

        } else {
            model.addAttribute("PDErrorClass", "null");
        }

        if (!plantDateResult.valid()) {
            model.addAttribute("PAErrorText", plantDateResult);
            model.addAttribute("PAErrorClass", "true");
        } else {
            model.addAttribute("PAErrorClass", "null");
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

    /**
     * Serves the file from the file service
     *
     * @param filename file to retrieve
     * @return response with the file
     */
    @GetMapping("/files/plants/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
        logger.info("GET /files/plants/" + filename);
        try {
            Resource file = fileService.loadFile(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (MalformedURLException error) {
            error.printStackTrace();
        }
        return null;

    }

}