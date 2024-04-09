package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.InputValidator.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Controller for creating a new Garden using a form page
 */
@Controller
public class GardenFormController {

    Logger logger = LoggerFactory.getLogger(GardenFormController.class);

    private final GardenService gardenService;

    private final LocationService locationService;

    @Autowired
    public GardenFormController(GardenService gardenService, LocationService locationService) {
        this.gardenService = gardenService;
        this.locationService = locationService;
    }

    @GetMapping("/api/location/suggestions")
    @ResponseBody
    public String getLocationSuggestions(@RequestParam("query") String query) throws IOException, InterruptedException {
        return locationService.getLocationSuggestions(query);
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
        model.addAttribute("myGardens", gardenService.getGardens());
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
    public String submitNewGardenForm(@RequestParam(name="gardenName") String gardenName,
                              @RequestParam(name = "streetAddress") String streetAddress,
                              @RequestParam(name = "suburb") String suburb,
                              @RequestParam(name = "city") String city,
                              @RequestParam(name = "country") String country,
                              @RequestParam(name = "postcode") String postcode,
                              @RequestParam(name = "gardenLocation") String gardenLocation,
                              @RequestParam(name = "gardenSize") String gardenSize,
                              Model model, RedirectAttributes redirectAttributes) {
        logger.info("POST /landingPage");
        //logic to handle checking if Garden Name, Garden Location and Garden size fields are valid

        String locationValues = "";
        if (!streetAddress.isBlank()) {
            locationValues += streetAddress + ", ";
        }
        if (!suburb.isBlank()) {
            locationValues += suburb + ", ";
        }
        if (!city.isBlank()) {
            locationValues += city;
            if (postcode.isBlank()) {
                locationValues += ", ";
            } else {
                locationValues += " ";
            }
        }
        if (!postcode.isBlank()) {
            locationValues += postcode + ", ";
        }
        if (!country.isBlank()) {
            locationValues += country;
        }

        gardenLocation = locationValues;

        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult streetAddressResult = InputValidator.optionalAlphaPlusTextField(streetAddress);
        ValidationResult suburbResult = InputValidator.optionalAlphaPlusTextField(suburb);
        ValidationResult cityResult = InputValidator.compulsoryAlphaPlusTextField(city);
        ValidationResult countryResult = InputValidator.compulsoryAlphaPlusTextField(country);
        ValidationResult postcodeResult = InputValidator.numberCommaSingleTextFieldWithLengthLimit(postcode,10);
        ValidationResult gardenLocationResult = InputValidator.compulsoryAlphaPlusTextField(gardenLocation);
        ValidationResult gardenSizeResult = InputValidator.numberCommaSingleTextField(gardenSize);

        gardenFormErrorText(model,gardenNameResult,streetAddressResult,suburbResult,cityResult,countryResult,postcodeResult,gardenLocationResult,gardenSizeResult);

        if(gardenSize.isBlank())
        {
            gardenSize = null;

        }
        model.addAttribute("gardenName", gardenName);
        model.addAttribute("streetAddress", streetAddress);
        model.addAttribute("suburb", suburb);
        model.addAttribute("city", city);
        model.addAttribute("country", country);
        model.addAttribute("postcode", postcode);
        model.addAttribute("gardenLocation", gardenLocation);
        model.addAttribute("gardenSize", gardenSize);
        model.addAttribute("myGardens", gardenService.getGardens());

        if(!gardenNameResult.valid() || !streetAddressResult.valid() || !suburbResult.valid() || !cityResult.valid() ||
                !countryResult.valid() || !gardenLocationResult.valid() || !gardenSizeResult.valid()) {
            return "createNewGardenForm";
        }

        float floatGardenSize;
        if(gardenSize == null){
            floatGardenSize = Float.NaN;
        }else{
            floatGardenSize = Float.parseFloat(gardenSize.replace(",","."));
        }
        Garden garden = new Garden(gardenName,streetAddress,suburb,city,postcode,country,gardenLocation,floatGardenSize);
        gardenService.addGarden(garden);

        logger.info("Created Garden Page");
        redirectAttributes.addAttribute("gardenId", garden.getGardenId());
        redirectAttributes.addAttribute("gardenName", gardenName);

        return "redirect:/my-gardens/{gardenId}={gardenName}";
    }


    /**
     * Maps the editGardenForm html page to /my-gardens/{gardenId}={gardenName}/edit url
     * @return thymeleaf editGardenForm
     */
    @GetMapping("/my-gardens/{gardenId}={gardenName}/edit")
    public String editGardenDetails(@PathVariable("gardenId") String gardenIdString,
                                    @PathVariable String gardenName,
                                    Model model) {
        logger.info("GET /my-gardens/{}-{}", gardenIdString, gardenName);

        // Convert gardenIdString to Long
        long gardenId = Long.parseLong(gardenIdString);

        Optional<Garden> optionalGarden = gardenService.findById(gardenId);
        model.addAttribute("myGardens", gardenService.getGardens());

        if (optionalGarden.isPresent()) {
            Garden garden = optionalGarden.get();
            model.addAttribute("gardenName", garden.getGardenName());
            model.addAttribute("streetAddress", garden.getGardenAddress());
            model.addAttribute("suburb", garden.getGardenSuburb());
            model.addAttribute("city", garden.getGardenCity());
            model.addAttribute("postcode", garden.getGardenPostcode());
            model.addAttribute("country", garden.getGardenCountry());
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
    public String submitEditedGardenForm( @RequestParam(name="gardenName") String gardenName,
                                       @RequestParam(name = "streetAddress") String streetAddress,
                                       @RequestParam(name = "suburb") String suburb,
                                       @RequestParam(name = "city") String city,
                                       @RequestParam(name = "country") String country,
                                       @RequestParam(name = "postcode") String postcode,
                                       @RequestParam(name = "gardenLocation") String gardenLocation,
                                       @RequestParam(name = "gardenSize") String gardenSize,
                                       @PathVariable("gardenId") String gardenIdString,
                                       Model model) {
        logger.info("POST / edited garden");
        //logic to handle checking if Garden Name, Garden Location and Garden size fields are valid

        String locationValues = "";
        if (!streetAddress.isBlank()) {
            locationValues += streetAddress + ", ";
        }
        if (!suburb.isBlank()) {
            locationValues += suburb + ", ";
        }
        if (!city.isBlank()) {
            locationValues += city;
            if (postcode.isBlank()) {
                locationValues += ", ";
            } else {
                locationValues += " ";
            }
        }
        if (!postcode.isBlank()) {
            locationValues += postcode + ", ";
        }
        if (!country.isBlank()) {
            locationValues += country;
        }

        gardenLocation = locationValues;

        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult streetAddressResult = InputValidator.optionalAlphaPlusTextField(streetAddress);
        ValidationResult suburbResult = InputValidator.optionalAlphaPlusTextField(suburb);
        ValidationResult cityResult = InputValidator.compulsoryAlphaPlusTextField(city);
        ValidationResult countryResult = InputValidator.compulsoryAlphaPlusTextField(country);
        ValidationResult postcodeResult = InputValidator.numberCommaSingleTextFieldWithLengthLimit(postcode,10);
        ValidationResult gardenLocationResult = InputValidator.compulsoryAlphaPlusTextField(gardenLocation);
        ValidationResult gardenSizeResult = InputValidator.numberCommaSingleTextField(gardenSize);

        gardenFormErrorText(model,gardenNameResult,streetAddressResult,suburbResult,cityResult,countryResult,postcodeResult,gardenLocationResult,gardenSizeResult);

        if(gardenSize.isBlank())
        {
            gardenSize = null;

        }
        model.addAttribute("gardenName", gardenName);
        model.addAttribute("streetAddress", streetAddress);
        model.addAttribute("suburb", suburb);
        model.addAttribute("city", city);
        model.addAttribute("country", country);
        model.addAttribute("postcode", postcode);
        model.addAttribute("gardenLocation", gardenLocation);
        model.addAttribute("gardenSize", gardenSize);
        model.addAttribute("myGardens", gardenService.getGardens());
        if(!gardenNameResult.valid() || !streetAddressResult.valid() || !suburbResult.valid() || !cityResult.valid() ||
                !countryResult.valid() || !gardenLocationResult.valid() || !gardenSizeResult.valid()) {
            return "editGardenForm";

        }
        float floatGardenSize;
        if(gardenSize == null){
            floatGardenSize = Float.NaN;
        }else{
            floatGardenSize = Float.parseFloat(gardenSize.replace(",","."));
        }
        gardenService.updateGarden(Long.parseLong(gardenIdString), new Garden(gardenName,streetAddress,suburb,city,postcode,country,gardenLocation,floatGardenSize));
        logger.info("edited garden");

        return "redirect:/my-gardens/{gardenId}={gardenName}";
    }


    /**
     * takes as an input the result of validating the garden name, location and size parameters and prints the apropriate
     */
    private void gardenFormErrorText(Model model, ValidationResult gardenNameResult, ValidationResult streetAddressResult,
                                     ValidationResult suburbResult, ValidationResult cityResult,
                                     ValidationResult countryResult, ValidationResult postcodeResult, ValidationResult gardenLocationResult,
                                     ValidationResult gardenSizeResult)
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

        // notifies the user that the street address is invalid (if applicable)
        if(!streetAddressResult.valid())
        {
            model.addAttribute("AddressErrorText","Address " + streetAddressResult);
            model.addAttribute("AddressErrorClass","errorBorder");
            logger.info("Garden Street failed validation");
        }
        else
        {
            model.addAttribute("AddressErrorClass","noErrorBorder");
        }

        // notifies the user that the suburb is invalid (if applicable)
        if(!suburbResult.valid())
        {
            model.addAttribute("SuburbErrorText","Suburb " + suburbResult);
            model.addAttribute("SuburbErrorClass","errorBorder");
            logger.info("Garden Suburb failed validation");
        }
        else
        {
            model.addAttribute("SuburbErrorClass","noErrorBorder");
        }

        // notifies the user that the city input is invalid (if applicable)
        if(!cityResult.valid())
        {
            if (cityResult == ValidationResult.BLANK) {
                model.addAttribute("CityErrorText","City and Country are required");
            }
            if (cityResult == ValidationResult.NON_ALPHA_PLUS || cityResult == ValidationResult.LENGTH_OVER_LIMIT) {
                model.addAttribute("CityErrorText","City " + cityResult);
            }
            model.addAttribute("CityErrorClass","errorBorder");
            logger.info("Garden City failed validation");
        }
        else
        {
            model.addAttribute("CountryErrorClass","noErrorBorder");
        }

        // notifies the user that the country input is invalid (if applicable)
        if(!countryResult.valid())
        {
            if (countryResult == ValidationResult.BLANK) {
                model.addAttribute("CountryErrorText","City and Country are required");
            }
            if (countryResult == ValidationResult.NON_ALPHA_PLUS || countryResult == ValidationResult.LENGTH_OVER_LIMIT) {
                model.addAttribute("CountryErrorText","Country " + countryResult);
            }
            model.addAttribute("CountryErrorClass","errorBorder");
            logger.info("Garden Country failed validation");
        }
        else
        {
            model.addAttribute("CountryErrorClass","noErrorBorder");
        }

        // notifies the user that the postcode input is invalid (if applicable)
        if(!postcodeResult.valid())
        {

            model.addAttribute("PostCodeErrorText","Postcode " + postcodeResult);
            model.addAttribute("PostCodeErrorClass","errorBorder");
            logger.info("Garden Postcode failed validation");
        }
        else
        {
            model.addAttribute("CountryErrorClass","noErrorBorder");
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
