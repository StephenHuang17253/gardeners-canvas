package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
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
     * Adds the loggedIn attribute to the model for all requests
     *
     * @param model
     */
    @ModelAttribute
    public void addLoggedInAttribute(Model model) {
        model.addAttribute("loggedIn", securityService.isLoggedIn());
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

        // logic to handle checking if fields are vaild
        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        ValidationResult plantNameResult = InputValidator.compulsoryAlphaPlusTextField(plantName, 64);
        ValidationResult plantDescriptionResult = InputValidator.validateDescription(plantDescription);
        ValidationResult plantCountResult = InputValidator.validatePlantCount(plantCount);
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

        plantFormErrorText(model, plantPictureResult, plantNameResult, plantCountResult, plantDescriptionResult,
                plantDateResult);

        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);

        // Sets default plant image
        String plantPictureString = getPlantPictureString("");
        model.addAttribute("plantPicture", plantPictureString);

        if (!plantPictureResult.valid() || !plantNameResult.valid() || !plantCountResult.valid()
                || !plantDescriptionResult.valid() || !plantDateResult.valid()) {
            return "createNewPlantForm";
        }

        int plantCountValue = 0;

        if (!Objects.equals(plantCount, "")) {
            plantCountValue = (int) (Double.parseDouble(plantCount.replace(",", ".")));
        }

        Plant newPlant = plantService.addPlant(plantName, plantCountValue, plantDescription, plantDate, gardenId);
        if (!plantPicture.isEmpty()) {
            plantService.updatePlantPicture(newPlant, plantPicture);
        }
        securityService.addUserInteraction(newPlant.getPlantId(), ItemType.PLANT, LocalDateTime.now());

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


        // logic to handle checking if fields are vaild
        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        ValidationResult plantNameResult = InputValidator.compulsoryAlphaPlusTextField(plantName, 64);
        ValidationResult plantDescriptionResult = InputValidator.validateDescription(plantDescription);
        ValidationResult plantCountResult = InputValidator.validatePlantCount(plantCount);
        ValidationResult plantDateResult;
        if (plantDate == null) {
            plantDateResult = ValidationResult.OK;
        } else {
            String dateString = plantDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            plantDateResult = InputValidator.validatePlantDate(dateString);
        }

        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }

        plantFormErrorText(model, plantPictureResult, plantNameResult, plantCountResult, plantDescriptionResult,
                plantDateResult);

        String plantPictureString = plantToUpdate.get().getPlantPictureFilename();
        model.addAttribute("plantPicture", plantPictureString);
        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);

        if (!plantPictureResult.valid() || !plantNameResult.valid() || !plantCountResult.valid()
                || !plantDescriptionResult.valid() || !plantDateResult.valid()) {
            return "editPlantForm";
        }

        int plantCountValue = 0;

        if (!Objects.equals(plantCount, "")) {
            plantCountValue = (int) (Double.parseDouble(plantCount.replace(",", ".")));
        }

        plantService.updatePlant(plantId, plantName, plantCountValue, plantDescription, plantDate);
        if (!plantPicture.isEmpty()) {
            plantService.updatePlantPicture(plantToUpdate.get(), plantPicture);
        }
        securityService.addUserInteraction(plantId, ItemType.PLANT, LocalDateTime.now());

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
        }

        // notifies the user that the plant Count is invalid (if applicable)
        if (!plantCountResult.valid()) {
            model.addAttribute("PCErrorText", plantCountResult);
        }

        // notifies the user that the plant Description is invalid (if applicable)
        if (!plantDescriptionResult.valid()) {
            model.addAttribute("PDErrorText", plantDescriptionResult);
        }

        if (!plantDateResult.valid()) {
            model.addAttribute("PAErrorText", plantDateResult);
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

        if (filename != null && !filename.isEmpty()) {
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
        logger.info("GET /files/plants/{}", filename);
        try {
            Resource file = fileService.loadFile(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                    .body(file);
        } catch (MalformedURLException error) {
            logger.error(error.getMessage());
        }
        return null;

    }

    /**
     * Add plant to db and then redirect to confirm copy plant
     *
     * @param gardenId           id of garden the plant has to be copied to
     * @param plantId            id of plant that is being copied
     * @param redirectAttributes redirectAttributes to send to confirmation form
     * @param response           response in case of error
     * @return confirmation of import form
     */
    @PostMapping("/import-plant")
    public String importPlant(@RequestParam("gardenId") Long gardenId,
                              @RequestParam("plantId") Long plantId, RedirectAttributes redirectAttributes,
                              HttpServletResponse response) {
        logger.info("POST /import-plant");
        Optional<Plant> optionalPlant = plantService.findById(plantId);

        if (optionalPlant.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Plant toCopyPlant = optionalPlant.get();

        Plant newPlant = plantService.addPlant(toCopyPlant.getPlantName(), toCopyPlant.getPlantCount(), toCopyPlant.getPlantDescription(), toCopyPlant.getPlantDate(), gardenId);
        if (toCopyPlant.getPlantPictureFilename() != null) {
            plantService.updatePlantPicture(newPlant, toCopyPlant.getPlantPictureFilename());
        }

        redirectAttributes.addAttribute("plantId", newPlant.getPlantId());
        redirectAttributes.addAttribute("gardenId", gardenId);
        redirectAttributes.addAttribute("gardenIdOfOriginalPlant", toCopyPlant.getGarden().getGardenId());

        return "redirect:/import-plant/confirm";
    }


    /**
     * Cancels copying of plant and deletes copy from database
     *
     * @param gardenIdOfOriginalPlant garden that was being copied from (used to return to previous garden page)
     * @param plantId                 id of plant to be deleted
     * @param response                error page
     * @return redirect to another page
     */
    @PostMapping("/import-plant/cancel")
    public String cancelImportPlant(@RequestParam("gardenIdOfOriginalPlant") Long gardenIdOfOriginalPlant,
                                    @RequestParam("plantId") Long plantId,
                                    RedirectAttributes redirectAttributes,
                                    HttpServletResponse response) {
        logger.info("POST /import-plant/cancel");
        Optional<Plant> optionalPlant = plantService.findById(plantId);

        if (optionalPlant.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        try {
            plantService.deletePlant(plantId);
        } catch (IOException error) {
            response.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            return "500";
        }

        redirectAttributes.addAttribute("gardenId", gardenIdOfOriginalPlant);

        return "redirect:/public-gardens/{gardenId}";
    }

    /**
     * Confirmation form to copy plant
     *
     * @param gardenId                id of the owner's garden the plant is being copied to
     * @param plantId                 id of the new copy of the plant
     * @param gardenIdOfOriginalPlant garden that was being copied from
     * @param response                response for error
     * @param model                   model of attributes
     * @return confirmation form
     */
    @GetMapping("/import-plant/confirm")
    public String importPlantConfirmationForm(@RequestParam("gardenId") Long gardenId,
                                              @RequestParam("plantId") Long plantId,
                                              @RequestParam("gardenIdOfOriginalPlant") Long gardenIdOfOriginalPlant,
                                              HttpServletResponse response,
                                              Model model) {
        logger.info("GET /import-plant/confirm");

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

        model.addAttribute("gardenIdOfOriginalPlant", gardenIdOfOriginalPlant); // Pass gardenId to the form
        model.addAttribute("gardenName", garden.getGardenName()); // Pass gardenName to the form
        String plantPicture = plantToUpdate.get().getPlantPictureFilename();
        model.addAttribute("plantId", plantId);
        model.addAttribute("plantPicture", plantPicture);
        model.addAttribute("plantName", plantToUpdate.get().getPlantName());
        model.addAttribute("plantCount", plantToUpdate.get().getPlantCount());
        model.addAttribute("plantDescription", plantToUpdate.get().getPlantDescription());
        model.addAttribute("plantDate", plantToUpdate.get().getPlantDate());

        return "importPlantForm";
    }


}