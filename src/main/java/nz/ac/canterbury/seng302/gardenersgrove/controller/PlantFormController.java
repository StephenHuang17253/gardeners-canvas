package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controller for creating a new Plant using a form page
 */
@Controller
public class PlantFormController {

    Logger logger = LoggerFactory.getLogger(PlantFormController.class);

    private final PlantService plantService;
    private final GardenService gardenService;

    @Autowired
    public PlantFormController(PlantService plantService, GardenService gardenService) {
        this.plantService = plantService;
        this.gardenService = gardenService;
    }
    /**
     * Maps the createNewPlantForm html page to /create-new-plant url
     * @return thymeleaf createNewPlantForm
     */
    @GetMapping("/my-gardens/{gardenId}={gardenName}/create-new-plant")
    public String newPlantForm(@PathVariable("gardenId") Long gardenId,
                               @PathVariable("gardenName") String gardenName,
                               @RequestParam(name = "plantName", required = false) String plantName,
                               @RequestParam(name = "plantCount", required = false) Float plantCount,
                               @RequestParam(name = "plantDescription", required = false) String plantDescription,
                               @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
                               Model model) {
        model.addAttribute("gardenId", gardenId); // Pass gardenId to the form
        model.addAttribute("gardenName", gardenName); // Pass gardenName to the form
        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);
        model.addAttribute("myGardens", gardenService.getGardens());
        logger.info("GET /create-new-plant");
        return "createNewPlantForm"; // Return the view for creating a new plant
    }

    /**
     * Logic to handle the confirm new plant form button
     * also validates inputs into form and informs the user if their input is invalid
     * @param plantName        user entered plant name
     * @param plantCount       user entered plant count
     * @param plantDescription user entered plant description
     * @param plantDate        user entered date plant is planted
     * @param model            (map-like) representation of plantName, plantCount, plantDescription and plantDate for use in thymeleaf,
     *                         with values being set to relevant parameters provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/my-gardens/{gardenId}={gardenName}/create-new-plant")
    public String submitNewPlantForm(@RequestParam(name = "plantName") String plantName,
                                     @RequestParam(name = "plantCount", required = false) String plantCount,
                                     @RequestParam(name = "plantDescription", required = false) String plantDescription,
                                     @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
                                     @PathVariable("gardenId") Long gardenId,
                                     Model model) {
        logger.info("POST /landingPage");
        //logic to handle checking if fields are vaild
        ValidationResult plantNameResult = InputValidator.compulsoryAlphaPlusTextField(plantName);
        ValidationResult plantCountResult = InputValidator.numberCommaSingleTextField(plantCount);
        ValidationResult plantDescriptionResult = InputValidator.optionalTextFieldWithLengthLimit(plantDescription, 512);


        plantFormErrorText(model, plantNameResult, plantCountResult, plantDescriptionResult);

        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);
        model.addAttribute("myGardens", gardenService.getGardens());


        if (!plantNameResult.valid() || !plantCountResult.valid() || !plantDescriptionResult.valid()){
            System.out.println("Passed");
            return "createNewPlantForm";
        }
        if(plantCount.isBlank()) {plantCount = "1.0";}
        float floatPlantCount = Float.parseFloat(plantCount.replace(",", "."));
        plantService.addPlant(plantName, floatPlantCount, plantDescription, plantDate, gardenId);
        logger.info("Created new Plant");
        return "redirect:/my-gardens/{gardenId}={gardenName}";
    }



    /**
     * Maps the editPlantForm html page to /create-new-plant url
     * Pre populates the values with the plants record from the database,
     * sends user to 404 page if plant is not found
     * @return thymeleaf createNewPlantForm
     */
    @GetMapping("/my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit")
    public String editPlantForm(@PathVariable("gardenId") Long gardenId,
                                @PathVariable("gardenName") String gardenName,
                                @PathVariable("plantId") Long plantId,
                                @PathVariable("plantName") String plantName,
                               Model model) {
        Optional<Plant> plantToUpdate =  plantService.findById(plantId);
        if(!plantToUpdate.isPresent())
        {
            return "404";
        }
        model.addAttribute("gardenId", gardenId); // Pass gardenId to the form
        model.addAttribute("gardenName", gardenName); // Pass gardenName to the form
        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantToUpdate.get().getPlantCount());
        model.addAttribute("plantDescription", plantToUpdate.get().getPlantDescription());
        model.addAttribute("plantDate", plantToUpdate.get().getPlantDate());
        model.addAttribute("myGardens", gardenService.getGardens());
        logger.info("GET /my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit");
        return "editPlantForm"; // Return the view for creating a new plant
    }

    /**
     * Logic to handle the confirm the edit  plant form button
     * also validates inputs into form and informs the user if their input is invalid
     *
     * @param plantName        user entered plant name
     * @param plantCount       user entered plant count
     * @param plantDescription user entered plant description
     * @param plantDate        user entered date plant is planted
     * @param model            (map-like) representation of plantName, plantCount, plantDescription and plantDate for use in thymeleaf,
     *                         with values being set to relevant parameters provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit")
    public String submiteditPlantForm(@RequestParam(name = "plantName") String plantName,
                                     @RequestParam(name = "plantCount", required = false) String plantCount,
                                     @RequestParam(name = "plantDescription", required = false) String plantDescription,
                                     @RequestParam(name = "plantDate", required = false) LocalDate plantDate,
                                     @PathVariable("gardenId") Long gardenId,
                                      @PathVariable("plantId") Long plantId,
                                     Model model) {
        logger.info("POST /my-gardens/{gardenId}={gardenName}/{plantId}={plantName}/edit");
        //logic to handle checking if fields are vaild
        ValidationResult plantNameResult = InputValidator.compulsoryAlphaPlusTextField(plantName);
        ValidationResult plantCountResult = InputValidator.numberCommaSingleTextField(plantCount);
        ValidationResult plantDescriptionResult = InputValidator.optionalTextFieldWithLengthLimit(plantDescription, 512);


        plantFormErrorText(model, plantNameResult, plantCountResult, plantDescriptionResult);

        model.addAttribute("plantName", plantName);
        model.addAttribute("plantCount", plantCount);
        model.addAttribute("plantDescription", plantDescription);
        model.addAttribute("plantDate", plantDate);
        model.addAttribute("myGardens", gardenService.getGardens());

        if (!plantNameResult.valid() || !plantCountResult.valid() || !plantDescriptionResult.valid()){
            System.out.println("Passed");
            return "editPlantForm";
        }

        if(plantCount.isBlank()) {plantCount = "1.0";}
        float floatPlantCount = Float.parseFloat(plantCount.replace(",", "."));
        plantService.updatePlant(plantId, plantName, floatPlantCount, plantDescription, plantDate);
        logger.info("updated Plant");
        return "redirect:/my-gardens/{gardenId}={gardenName}";
    }




    /**
     * Takes as an input the result of validating the plant name, count, description and date,
     * and prints the appropriate messages.
     * @param plantNameResult result of validating name (OK or appropriate error)
     * @param plantCountResult result of validating count (OK or appropriate error)
     * @param plantDescriptionResult result of validating description (OK or appropriate error)
     */
    private void plantFormErrorText (Model model, ValidationResult plantNameResult,
            ValidationResult plantCountResult, ValidationResult plantDescriptionResult){

        // notifies the user that the plant Name is invalid (if applicable)
        if (!plantNameResult.valid()) {
            plantNameResult.updateMessage("cannot be empty and must only include letters, numbers, spaces, dots, hyphens or apostrophes");
            model.addAttribute("PNErrorText", "Plant name " + plantNameResult);
            model.addAttribute("PNErrorClass", "errorBorder");
            logger.info("Plant Name failed validation");
        } else {
            model.addAttribute("PNErrorClass", "noErrorBorder");
        }

        // notifies the user that the plant Count is invalid (if applicable)
        if (!plantCountResult.valid()) {
            model.addAttribute("PCErrorText", "Plant count " + plantCountResult);
            model.addAttribute("PCErrorClass", "errorBorder");
            logger.info("Plant Count failed validation");


        } else {
            model.addAttribute("PCErrorClass", "noErrorBorder");
        }

        // notifies the user that the plant Description is invalid (if applicable)
        if (!plantDescriptionResult.valid()) {
            model.addAttribute("PDErrorText", "Plant description " + plantDescriptionResult);
            model.addAttribute("PDErrorClass", "errorBorder");
            logger.info("Plant Description failed validation");

        } else {
            model.addAttribute("PDErrorClass", "noErrorBorder");
        }

    }

}