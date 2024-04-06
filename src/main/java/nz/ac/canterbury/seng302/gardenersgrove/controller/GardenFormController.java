package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * Controller for creating a new Garden using a form page
 */
@Controller
@SessionAttributes("userGardens")
public class GardenFormController {

    Logger logger = LoggerFactory.getLogger(GardenFormController.class);
    private final GardenService gardenService;
    private final UserService userService;

    @Autowired
    public GardenFormController(GardenService gardenService, UserService userService) {
        this.gardenService = gardenService;
        this.userService = userService;
    }
    /**
     * Maps the createNewGardenForm html page to /create-new-garden url
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/create-new-garden")
    public String newGardenForm( @RequestParam(name = "gardenName", required = false) String gardenName,
                                 @RequestParam(name = "gardenLocation", required = false) String gardenLocation,
                                 @RequestParam(name = "gardenSize",required = false) String gardenSize,
                                 Model model) {
        model.addAttribute("gardenName", gardenName);
        model.addAttribute("gardenLocation", gardenLocation);
        model.addAttribute("gardenSize", gardenSize);
        logger.info("GET /create-new-garden");
        return "createNewGardenForm";
    }
    /**
     * Logic to handle the confirm new garden form button
     * also validates inputs into form and informs the user if their input is invalid
     * @param gardenName users entered garden name
     * @param gardenLocation users entered garden location
     * @param gardenSize users entered garden size(m^2)
     * @param model (map-like) representation of gardenName, gardenLocation and gardenSize for use in thymeleaf,
     *              with values being set to relevant parameters provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/create-new-garden")
    public String submitNewGardenForm( @RequestParam(name="gardenName") String gardenName,
                                       @RequestParam(name = "gardenLocation") String gardenLocation,
                                       @RequestParam(name = "gardenSize") String gardenSize,
                                       HttpSession session,
                                       Model model,
                                       RedirectAttributes redirectAttributes) {
        logger.info("POST /landingPage");
        //logic to handle checking if Garden Name, Garden Location and Garden size fields are valid
        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult gardenLocationResult = InputValidator.compulsoryAlphaPlusTextField(gardenLocation);
        ValidationResult gardenSizeResult = InputValidator.numberCommaSingleTextField(gardenSize);

        gardenFormErrorText(model,gardenNameResult,gardenLocationResult,gardenSizeResult);

        if(gardenSize.isBlank())
        {
            gardenSize = null;

        }
        model.addAttribute("gardenName", gardenName);
        model.addAttribute("gardenLocation", gardenLocation);
        model.addAttribute("gardenSize", gardenSize);
        if(!gardenNameResult.valid() || !gardenLocationResult.valid() || !gardenSizeResult.valid()) {
            return "createNewGardenForm";
        }

        float floatGardenSize;
        if(gardenSize == null){
            floatGardenSize = Float.NaN;
        }else{
            floatGardenSize = Float.parseFloat(gardenSize.replace(",","."));
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User owner = this.userService.getUserByEmail(authentication.getName());
        Garden garden = new Garden(gardenName, gardenLocation,floatGardenSize,owner);
        gardenService.addGarden(garden);
        session.setAttribute("userGardens", gardenService.getAllUsersGardens(owner.getId()));

        logger.info("Created Garden Page");
        redirectAttributes.addAttribute("gardenId", garden.getGardenId());
        redirectAttributes.addAttribute("gardenName", gardenName);

        return "redirect:/my-gardens/{gardenId}={gardenName}";
    }
    /**
     * Maps the editGardenForm html page to /my-gardens/{gardenId}={gardenName}/edit url
     * @return thymeleaf editGardenForm
     */
    @PreAuthorize("@securityService.isOwner(#gardenId)")
    @GetMapping("/my-gardens/{gardenId}={gardenName}/edit")
    public String editGardenDetails(@PathVariable Long gardenId,
                                    @PathVariable String gardenName,
                                    Model model) {
        logger.info("GET /my-gardens/{}-{}", gardenId, gardenName);

        Optional<Garden> optionalGarden = gardenService.findById(gardenId);
        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            model.addAttribute("gardenName", garden.getGardenName());
            model.addAttribute("gardenLocation", garden.getGardenLocation());
            Float gardenSize = garden.getGardenSize();
            if (Float.isNaN(gardenSize)) {
                model.addAttribute("gardenSize", "");
            } else {
                model.addAttribute("gardenSize", gardenSize);
            }
            return "editGardenForm"; // Thymeleaf template name
        } else {
            return "404";
        }
    }
    /**
     * Logic to handle the confirm new garden form button
     * also validates inputs into form and informs the user if their input is invalid
     * @param gardenName users entered garden name
     * @param gardenLocation users entered garden location
     * @param gardenSize users entered garden size(m^2)
     * @param model (map-like) representation of gardenName, gardenLocation and gardenSize for use in thymeleaf,
     *              with values being set to relevant parameters provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/my-gardens/{gardenId}={gardenName}/edit")
    public String submitEditedGardenForm(@RequestParam(name="gardenName") String gardenName,
                                         @RequestParam(name = "gardenLocation") String gardenLocation,
                                         @RequestParam(name = "gardenSize") String gardenSize,
                                         @PathVariable("gardenId") String gardenIdString,
                                         HttpSession session,
                                         Model model) {
        logger.info("POST / edited garden");
        //logic to handle checking if Garden Name, Garden Location and Garden size fields are valid
        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult gardenLocationResult = InputValidator.compulsoryAlphaPlusTextField(gardenLocation);
        ValidationResult gardenSizeResult = InputValidator.numberCommaSingleTextField(gardenSize);

        gardenFormErrorText(model,gardenNameResult,gardenLocationResult,gardenSizeResult);

        if(gardenSize.isBlank())
        {
            gardenSize = null;

        }
        model.addAttribute("gardenName", gardenName);
        model.addAttribute("gardenLocation", gardenLocation);
        model.addAttribute("gardenSize", gardenSize);
        if(!gardenNameResult.valid() || !gardenLocationResult.valid() || !gardenSizeResult.valid()) {
            return "editGardenForm";

        }
        float floatGardenSize;
        if(gardenSize == null){
            floatGardenSize = Float.NaN;
        }else{
            floatGardenSize = Float.parseFloat(gardenSize.replace(",","."));
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User owner = userService.getUserByEmail(authentication.getName());
        gardenService.updateGarden(Long.parseLong(gardenIdString), new Garden(gardenName, gardenLocation,floatGardenSize, owner));
        logger.info("Edited Garden Page");
        session.setAttribute("userGardens", gardenService.getAllUsersGardens(owner.getId()));

        return "redirect:/my-gardens/{gardenId}={gardenName}";
    }


    /**
     * takes as an input the result of validating the garden name, location and size parameters and prints the apropriate
     */
    private void gardenFormErrorText(Model model, ValidationResult gardenNameResult,
                                     ValidationResult gardenLocationResult, ValidationResult gardenSizeResult)
    {

        // notifies the user that the garden Name is invalid (if applicable)
        if(!gardenNameResult.valid())
        {
            model.addAttribute("GNErrorText","Garden name " + gardenNameResult);
            model.addAttribute("GNErrorClass","errorBorder");
            logger.info("Garden Name failed validation");
        }
        else
        {
            model.addAttribute("GNErrorClass","noErrorBorder");
        }

        // notifies the user that the garden Location is invalid (if applicable)
        if(!gardenLocationResult.valid())
        {
            model.addAttribute("GLErrorText","Location " + gardenLocationResult);
            model.addAttribute("GLErrorClass","errorBorder");
            logger.info("Garden Location failed validation");


        }
        else
        {
            model.addAttribute("GLErrorClass","noErrorBorder");
        }

        // notifies the user that the garden Size is invalid (if applicable)
        if(!gardenSizeResult.valid())
        {
            model.addAttribute("GSErrorText","Garden size " + gardenSizeResult);
            model.addAttribute("GSErrorClass","errorBorder");
            logger.info("Garden Size failed validation");

        }
        else
        {
            model.addAttribute("GSErrorClass","noErrorBorder");
        }
    }


}
