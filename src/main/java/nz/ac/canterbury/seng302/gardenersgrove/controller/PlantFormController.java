package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.component.Constants;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.PlantCategory;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.apache.tomcat.util.bcel.Const;
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
     * Maps the createNewPlantForm html page to /create-new-plant url
     *
     * @return thymeleaf createNewPlantForm
     */
    @GetMapping("/my-gardens/{gardenId}/create-new-plant")
    public String newPlantForm(@PathVariable Long gardenId,
            @RequestParam(name = Constants.PLANT_NAME_ATTRIBUTE, required = false) String plantName,
            @RequestParam(name = Constants.PLANT_COUNT_ATTRIBUTE, required = false) String plantCount,
            @RequestParam(name = Constants.PLANT_DESCRIPTION_ATTRIBUTE, required = false) String plantDescription,
            @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
            @RequestParam(name = "plantCategory", required = false) String plantCategory,
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
        model.addAttribute(Constants.GARDEN_ID_ATTRIBUTE, gardenId); // Pass gardenId to the form
        model.addAttribute(Constants.GARDEN_NAME_ATTRIBUTE, garden.getGardenName()); // Pass gardenName to the form
        model.addAttribute(Constants.PLANT_NAME_ATTRIBUTE, plantName);
        model.addAttribute(Constants.PLANT_COUNT_ATTRIBUTE, plantCount);
        model.addAttribute(Constants.PLANT_CATEGORY_ATTRIBUTE, plantCategory);
        model.addAttribute(Constants.PLANT_DESCRIPTION_ATTRIBUTE, plantDescription);
        model.addAttribute(Constants.PLANT_DATE_ATTRIBUTE, plantDate);
        model.addAttribute("myGardens", gardenService.getGardens());
        model.addAttribute(Constants.PLANT_CATEGORIES_ATTRIBUTE, plantService.getPlantCategories());

        // Sets default plant image
        String plantPictureString = getPlantPictureString("");
        model.addAttribute(Constants.PLANT_PICTURE_ATTRIBUTE, plantPictureString);

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
    public String submitNewPlantForm(@RequestParam(name = Constants.PLANT_NAME_ATTRIBUTE) String plantName,
            @RequestParam(name = Constants.PLANT_COUNT_ATTRIBUTE, required = false) String plantCount,
            @RequestParam(name = Constants.PLANT_DESCRIPTION_ATTRIBUTE, required = false) String plantDescription,
            @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
            @RequestParam(name = "plantPictureInput") MultipartFile plantPicture,
            @RequestParam(name = "plantCategory") String plantCategory,
            @PathVariable(Constants.GARDEN_ID_ATTRIBUTE) Long gardenId,
            HttpServletResponse response,
            Model model) {
        logger.info("POST /create-new-plant");

        // logic to handle checking if fields are vaild
        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        ValidationResult plantNameResult = InputValidator.compulsoryAlphaPlusTextField(plantName, 64);
        ValidationResult plantDescriptionResult = InputValidator.validateDescription(plantDescription);
        ValidationResult plantCountResult = InputValidator.validatePlantCount(plantCount);
        ValidationResult plantDateResult;
        ValidationResult plantCategoryResult = InputValidator.compulsoryTextField(plantCategory);

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

        if (plantCategory == null || plantCategory.isEmpty()) {
            plantCategoryResult = ValidationResult.INVALID_CATEGORY;
        }

        // Plant image is optional
        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }


        plantFormErrorText(model, plantPictureResult, plantNameResult, plantCountResult, plantDescriptionResult,
                plantDateResult, plantCategoryResult);

        model.addAttribute(Constants.PLANT_NAME_ATTRIBUTE, plantName);
        model.addAttribute(Constants.PLANT_COUNT_ATTRIBUTE, plantCount);
        model.addAttribute(Constants.PLANT_CATEGORY_ATTRIBUTE, plantCategory);
        model.addAttribute(Constants.PLANT_DESCRIPTION_ATTRIBUTE, plantDescription);
        model.addAttribute(Constants.PLANT_DATE_ATTRIBUTE, plantDate);
        model.addAttribute(Constants.PLANT_CATEGORIES_ATTRIBUTE, plantService.getPlantCategories());

        // Sets default plant image
        String plantPictureString = getPlantPictureString("");
        model.addAttribute(Constants.PLANT_PICTURE_ATTRIBUTE, plantPictureString);

        if (!plantPictureResult.valid() || !plantNameResult.valid() || !plantCountResult.valid()
                || !plantDescriptionResult.valid() || !plantDateResult.valid() || !plantCategoryResult.valid()) {
            return "createNewPlantForm";
        }

        int plantCountValue = 0;

        if (!Objects.equals(plantCount, "")) {
            plantCountValue = (int) (Double.parseDouble(plantCount.replace(",", ".")));
        }

        PlantCategory plantCategoryValue = null;
        if (plantCategory != null) {
            plantCategory = plantCategory.replace(' ', '_');
            plantCategoryValue = PlantCategory.valueOf(plantCategory.toUpperCase());
        }

        Plant newPlant = plantService.addPlant(plantName, plantCountValue, plantDescription, plantDate, gardenId, plantCategoryValue);

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
    public String editPlantForm(@PathVariable(Constants.GARDEN_ID_ATTRIBUTE) Long gardenId,
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

        String plantCount = String.valueOf(plantToUpdate.get().getPlantCount());

        if (Objects.equals(plantCount, "0")) {
            plantCount = "";
        }

        LocalDate plantDateLocal = plantToUpdate.get().getPlantDate();

        String plantDate = "";

        if (plantDateLocal != null) {
            plantDate = plantDateLocal.toString();
        } else {
            plantDate = null;
        }

        model.addAttribute(Constants.GARDEN_ID_ATTRIBUTE, gardenId); // Pass gardenId to the form
        model.addAttribute(Constants.GARDEN_NAME_ATTRIBUTE, garden.getGardenName()); // Pass gardenName to the form
        String plantPicture = plantToUpdate.get().getPlantPictureFilename();
        model.addAttribute(Constants.PLANT_PICTURE_ATTRIBUTE, plantPicture);
        model.addAttribute(Constants.PLANT_NAME_ATTRIBUTE, plantToUpdate.get().getPlantName());
        model.addAttribute(Constants.PLANT_CATEGORY_ATTRIBUTE, plantToUpdate.get().getPlantCategory());
        model.addAttribute(Constants.PLANT_COUNT_ATTRIBUTE, plantCount);
        model.addAttribute(Constants.PLANT_DESCRIPTION_ATTRIBUTE, plantToUpdate.get().getPlantDescription());
        model.addAttribute(Constants.PLANT_DATE_ATTRIBUTE, plantDate);
        model.addAttribute(Constants.PLANT_CATEGORIES_ATTRIBUTE, plantService.getPlantCategories());
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
    public String submiteditPlantForm(@RequestParam(name = Constants.PLANT_NAME_ATTRIBUTE) String plantName,
            @RequestParam(name = Constants.PLANT_COUNT_ATTRIBUTE, required = false) String plantCount,
            @RequestParam(name = Constants.PLANT_DESCRIPTION_ATTRIBUTE, required = false) String plantDescription,
            @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
            @RequestParam(name = "plantPictureInput", required = false) MultipartFile plantPicture,
            @RequestParam(name = "plantCategory") String plantCategory,
            @PathVariable(Constants.GARDEN_ID_ATTRIBUTE) Long gardenId,
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
        ValidationResult plantCategoryResult = InputValidator.compulsoryTextField(plantCategory);

        if (plantDate == null) {
            plantDateResult = ValidationResult.OK;
        } else {
            String dateString = plantDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            plantDateResult = InputValidator.validatePlantDate(dateString);
        }

        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }

        if (plantCategory == null || plantCategory.isEmpty()) {
            plantCategoryResult = ValidationResult.INVALID_CATEGORY;
        }

        plantFormErrorText(model, plantPictureResult, plantNameResult, plantCountResult, plantDescriptionResult,
                plantDateResult, plantCategoryResult);

        String plantPictureString = plantToUpdate.get().getPlantPictureFilename();
        model.addAttribute(Constants.PLANT_PICTURE_ATTRIBUTE, plantPictureString);
        model.addAttribute(Constants.PLANT_NAME_ATTRIBUTE, plantName);
        model.addAttribute(Constants.PLANT_COUNT_ATTRIBUTE, plantCount);
        model.addAttribute(Constants.PLANT_DESCRIPTION_ATTRIBUTE, plantDescription);
        model.addAttribute(Constants.PLANT_DATE_ATTRIBUTE, plantDate);
        model.addAttribute(Constants.PLANT_CATEGORY_ATTRIBUTE, plantCategory);
        model.addAttribute(Constants.PLANT_CATEGORIES_ATTRIBUTE, plantService.getPlantCategories());

        if (!plantPictureResult.valid() || !plantNameResult.valid() || !plantCountResult.valid()
                || !plantDescriptionResult.valid() || !plantDateResult.valid() || !plantCategoryResult.valid()) {
            return "editPlantForm";
        }

        int plantCountValue = 0;

        if (!Objects.equals(plantCount, "")) {
            plantCountValue = (int) (Double.parseDouble(plantCount.replace(",", ".")));
        }

        PlantCategory plantCategoryValue = null;
        if (plantCategory != null) {
            plantCategory = plantCategory.replace(' ', '_');
            plantCategoryValue = PlantCategory.valueOf(plantCategory.toUpperCase());
        }

        plantService.updatePlant(plantId, plantName, plantCountValue, plantDescription, plantDate, plantCategoryValue);
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
            ValidationResult plantDateResult, ValidationResult plantCategoryResult) {

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
            plantDescriptionResult.updateMessage(
                    "Description must be 512 characters or less and contain some letters");
            model.addAttribute("PDErrorText", plantDescriptionResult);
        }

        if (!plantDateResult.valid()) {
            model.addAttribute("PAErrorText", plantDateResult);
        }

        if (!plantCategoryResult.valid()) {
            model.addAttribute("PCAErrorText", plantCategoryResult);
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
            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename\"" + file.getFilename() + "\"");
            headers.add(HttpHeaders.CONTENT_TYPE, fileService.getImageFileType(filename));
            return ResponseEntity.ok().headers(headers).body(file);
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
    public String importPlant(@RequestParam(Constants.GARDEN_ID_ATTRIBUTE) Long gardenId,
            @RequestParam("plantId") Long plantId,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response) {
        logger.info("POST /import-plant");
        Optional<Plant> optionalPlant = plantService.findById(plantId);

        if (optionalPlant.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Plant toCopyPlant = optionalPlant.get();

        Plant newPlant = plantService.addPlant(toCopyPlant.getPlantName(), toCopyPlant.getPlantCount(),
                toCopyPlant.getPlantDescription(), toCopyPlant.getPlantDate(), gardenId, PlantCategory.TREE);
        if (toCopyPlant.getPlantPictureFilename() != null) {
            plantService.updatePlantPicture(newPlant, toCopyPlant.getPlantPictureFilename());
        }
        securityService.addUserInteraction(plantId, ItemType.PLANT, LocalDateTime.now());
        securityService.addUserInteraction(newPlant.getPlantId(), ItemType.PLANT, LocalDateTime.now());

        redirectAttributes.addAttribute("plantId", newPlant.getPlantId());
        redirectAttributes.addAttribute(Constants.GARDEN_ID_ATTRIBUTE, gardenId);
        redirectAttributes.addAttribute("gardenIdOfOriginalPlant", toCopyPlant.getGarden().getGardenId());

        return "redirect:/import-plant/confirm";
    }

    /**
     * Cancels copying of plant and deletes copy from database
     *
     * @param gardenIdOfOriginalPlant garden that was being copied from (used to
     *                                return to previous garden page)
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

        redirectAttributes.addAttribute(Constants.GARDEN_ID_ATTRIBUTE, gardenIdOfOriginalPlant);

        return "redirect:/public-gardens/{gardenId}";
    }

    /**
     * Confirmation form to copy plant
     *
     * @param gardenId                id of the owner's garden the plant is being
     *                                copied to
     * @param plantId                 id of the new copy of the plant
     * @param gardenIdOfOriginalPlant garden that was being copied from
     * @param response                response for error
     * @param model                   model of attributes
     * @return confirmation form
     */
    @GetMapping("/import-plant/confirm")
    public String importPlantConfirmationForm(@RequestParam(Constants.GARDEN_ID_ATTRIBUTE) Long gardenId,
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
        model.addAttribute(Constants.GARDEN_NAME_ATTRIBUTE, garden.getGardenName()); // Pass gardenName to the form
        String plantPicture = plantToUpdate.get().getPlantPictureFilename();
        model.addAttribute("plantId", plantId);
        model.addAttribute(Constants.PLANT_PICTURE_ATTRIBUTE, plantPicture);
        model.addAttribute(Constants.PLANT_NAME_ATTRIBUTE, plantToUpdate.get().getPlantName());
        model.addAttribute(Constants.PLANT_COUNT_ATTRIBUTE, plantToUpdate.get().getPlantCount());
        model.addAttribute(Constants.PLANT_DESCRIPTION_ATTRIBUTE, plantToUpdate.get().getPlantDescription());
        model.addAttribute(Constants.PLANT_DATE_ATTRIBUTE, plantToUpdate.get().getPlantDate());
        model.addAttribute(Constants.PLANT_CATEGORY_ATTRIBUTE, plantToUpdate.get().getPlantCategory());

        return "importPlantForm";
    }

}