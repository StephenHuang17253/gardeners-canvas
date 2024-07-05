package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * Controller for creating a new Garden using a form page
 */
@Controller
@SessionAttributes("userGardens")
public class GardenFormController {

    Logger logger = LoggerFactory.getLogger(GardenFormController.class);

    private final GardenService gardenService;
    private final SecurityService securityService;
    private final LocationService locationService;

    private static final int MAX_REQUESTS_PER_SECOND = 2;

    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND);

    private volatile long lastRequestTime = Instant.now().getEpochSecond();

    @Autowired
    public GardenFormController(GardenService gardenService, LocationService locationService,
            SecurityService securityService) {
        this.gardenService = gardenService;
        this.locationService = locationService;
        this.securityService = securityService;
    }

    /**
     * Retrieves location suggestions from the LocationIQ API based on query string
     * provided by frontend JS.
     * Also handles rate limiting to prevent exceeding 2 requests per second, to
     * match our free tier.
     *
     * @param query The search query for location autocomplete suggestions.
     * @return A JSON response string containing location suggestions, or a "429"
     *         string if rate limit is exceeded.
     * @throws IOException          If an I/O error occurs while making the
     *                              requesting
     * @throws InterruptedException If an interruption occurs while waiting for
     *                              response
     */
    @GetMapping("/api/location/suggestions")
    @ResponseBody
    public String getLocationSuggestions(@RequestParam("query") String query) throws IOException, InterruptedException {
        long currentTime = Instant.now().getEpochSecond();
        long timeElapsed = currentTime - lastRequestTime;

        logger.info("Time elapsed: " + timeElapsed);
        // Every second, the number of available permits is reset to 2
        if (timeElapsed >= 1) {
            semaphore.drainPermits();
            semaphore.release(MAX_REQUESTS_PER_SECOND);
            logger.info("A second or more has elapsed, permits reset to: " + semaphore.availablePermits());
            lastRequestTime = currentTime;
        }

        logger.info("Permits left before request: " + semaphore.availablePermits());

        // Check if rate limit exceeded
        if (!semaphore.tryAcquire()) {
            logger.info("Exceeded location API rate limit of 2 requests per second.");
            return "429"; // Frontend script will check if this returns 429 to toggle error messages.
        }
        logger.info("Permits left after request: " + semaphore.availablePermits());

        return locationService.getLocationSuggestions(query);

    }

    /**
     * Maps the createNewGardenPage html page to /create-new-garden url
     *
     * @return thymeleaf createNewGardenPage
     */
    @GetMapping("/create-new-garden")
    public String newGardenForm( @RequestParam(name="gardenName", required = false) String gardenName,
                                 @RequestParam(name = "streetAddress", required = false) String streetAddress ,
                                 @RequestParam(name = "gardenDescription", required = false) String gardenDescription,
                                 @RequestParam(name = "suburb", required = false) String suburb,
                                 @RequestParam(name = "city", required = false) String city,
                                 @RequestParam(name = "country", required = false) String country,
                                 @RequestParam(name = "postcode", required = false )String postcode,
                                 @RequestParam(name = "gardenSize", required = false) String gardenSize,
                                 @RequestParam(name = "longitude", required = false) String longitude,
                                 @RequestParam(name = "latitude", required = false) String latitude,
                                 Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

        model.addAttribute("gardenName", gardenName);
        model.addAttribute("gardenDescription", gardenDescription);
        model.addAttribute("streetAddress", streetAddress);
        model.addAttribute("suburb", suburb);
        model.addAttribute("city", city);
        model.addAttribute("country", country);
        model.addAttribute("postcode", postcode);
        model.addAttribute("gardenSize", gardenSize);

        logger.info("GET /create-new-garden");
        return "createNewGardenPage";
    }

    /**
     * Logic to handle the confirm new garden form button
     * also validates inputs into form and informs the user if their input is
     * invalid
     *
     * @param gardenName         The user-entered garden name
     * @param gardenDescription  The user-entered garden description
     * @param streetAddress      The user-entered street address of the garden
     *                           location.
     * @param suburb             The user-entered suburb of the garden location.
     * @param city               The user-entered city of the garden location.
     * @param country            The user-entered country of the garden location.
     * @param postcode           The user-entered postcode of the garden location.
     * @param gardenSize         The user-entered garden size(m^2)
     * @param session            The HttpSession object for managing user session
     *                           data.
     * @param redirectAttributes RedirectAttributes object
     * @param model              (map-like) representation of gardenName,
     *                           gardenLocation and gardenSize for use in thymeleaf,
     *                           with values being set to relevant parameters
     *                           provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/create-new-garden")
    public String submitNewGardenForm(@RequestParam(name="gardenName") String gardenName,
                                      @RequestParam(name = "gardenDescription") String gardenDescription,
                                      @RequestParam(name = "streetAddress") String streetAddress,
                                      @RequestParam(name = "suburb") String suburb,
                                      @RequestParam(name = "city") String city,
                                      @RequestParam(name = "country") String country,
                                      @RequestParam(name = "postcode") String postcode,
                                      @RequestParam(name = "gardenSize") String gardenSize,
                                      @RequestParam(name = "longitude") String longitude,
                                      @RequestParam(name = "latitude") String latitude,
                                      HttpSession session,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        logger.info("POST /create-new-garden");

        // logic to handle checking if Garden Name, Garden Location and Garden size
        // fields are valid

        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult gardenDescriptionResult = InputValidator.validateDescription(gardenDescription);
        ValidationResult streetAddressResult = InputValidator.optionalAlphaPlusTextField(streetAddress);
        ValidationResult suburbResult = InputValidator.optionalAlphaPlusTextField(suburb);
        ValidationResult cityResult = InputValidator.compulsoryAlphaPlusTextField(city);
        ValidationResult countryResult = InputValidator.compulsoryAlphaPlusTextField(country);
        ValidationResult postcodeResult = InputValidator.validatePostcodeInput(postcode);
        ValidationResult gardenSizeResult = InputValidator.validateGardenAreaInput(gardenSize);

        gardenFormErrorText(model, gardenNameResult, streetAddressResult, suburbResult, cityResult, countryResult,
                postcodeResult, gardenSizeResult, gardenDescriptionResult);


        if (gardenSize.isBlank()) {
            gardenSize = null;
        }

        model.addAttribute("gardenName", gardenName);
        model.addAttribute("gardenDescription", gardenDescription);
        model.addAttribute("streetAddress", streetAddress);
        model.addAttribute("suburb", suburb);
        model.addAttribute("city", city);
        model.addAttribute("country", country);
        model.addAttribute("postcode", postcode);
        model.addAttribute("gardenSize", gardenSize);
        model.addAttribute("latitude", latitude);
        model.addAttribute("longitude", longitude);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

        if (!gardenNameResult.valid() || !streetAddressResult.valid() || !suburbResult.valid() || !cityResult.valid() ||
                !countryResult.valid() || !postcodeResult.valid() || !gardenSizeResult.valid()
                || !gardenDescriptionResult.valid()) {
            return "createNewGardenPage";
        }

        Double doubleGardenSize;
        if (gardenSize == null) {
            doubleGardenSize = 0.0;
        } else {
            doubleGardenSize = Double.parseDouble(gardenSize.replace(",", "."));
        }
        boolean isPublic = false;

        logger.info("Latitude" + latitude);
        User owner = securityService.getCurrentUser();

        Garden garden = new Garden(gardenName, gardenDescription, streetAddress,suburb,city,postcode,country,doubleGardenSize,isPublic,latitude,longitude, owner);

        gardenService.addGarden(garden);
        session.setAttribute("userGardens", gardenService.getAllUsersGardens(owner.getId()));
        model.addAttribute("userGardens", session.getAttribute("userGardens"));

        redirectAttributes.addAttribute("gardenId", garden.getGardenId());

        return "redirect:/my-gardens/{gardenId}";
    }

    /**
     * Maps the editGardenForm html page to /my-gardens/{gardenId}/edit url
     *
     * @return thymeleaf editGardenForm
     */
    @GetMapping("/my-gardens/{gardenId}/edit")
    public String editGardenDetails(@PathVariable Long gardenId,
            HttpServletResponse response,
            Model model) {
        logger.info("GET /my-gardens/{}", gardenId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
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
        model.addAttribute("gardenName", garden.getGardenName());
        model.addAttribute("gardenDescription", garden.getGardenDescription());
        model.addAttribute("streetAddress", garden.getGardenAddress());
        model.addAttribute("suburb", garden.getGardenSuburb());
        model.addAttribute("city", garden.getGardenCity());
        model.addAttribute("postcode", garden.getGardenPostcode());
        model.addAttribute("country", garden.getGardenCountry());
        model.addAttribute("gardenLocation", garden.getGardenLocation());
        model.addAttribute("latitude", garden.getGardenLatitude());
        model.addAttribute("longitude", garden.getGardenLongitude());
        double gardenSize = garden.getGardenSize();
        if (gardenSize == 0.0) {
            model.addAttribute("gardenSize", "");
        } else {
            model.addAttribute("gardenSize", gardenSize);
        }
        return "editGardenForm";
    }

    /**
     * Logic to handle the confirm new garden form button
     * also validates inputs into form and informs the user if their input is
     * invalid
     *
     * @param gardenName        The user-entered garden name
     * @param gardenDescription The user-entered garden description
     * @param streetAddress     The user-entered street address of the garden
     *                          location.
     * @param suburb            The user-entered suburb of the garden location.
     * @param city              The user-entered city of the garden location.
     * @param country           The user-entered country of the garden location.
     * @param postcode          The user-entered postcode of the garden location.
     * @param gardenSize        The user-entered garden size(m^2)
     * @param gardenId          The id of the garden being edited
     * @param session           The HttpSession object for managing user session
     *                          data.
     * @param model             (map-like) representation of gardenName,
     *                          gardenLocation
     *                          and gardenSize for use in thymeleaf,
     *                          with values being set to relevant parameters
     *                          provided
     * @return thymeleaf landingPage
     */
    @PostMapping("/my-gardens/{gardenId}/edit")
    public String submitEditedGardenForm(@RequestParam(name="gardenName") String gardenName,
                                         @RequestParam(name = "gardenDescription") String gardenDescription,
                                         @RequestParam(name = "streetAddress") String streetAddress,
                                       @RequestParam(name = "suburb") String suburb,
                                       @RequestParam(name = "city") String city,
                                       @RequestParam(name = "country") String country,
                                       @RequestParam(name = "postcode") String postcode,
                                       @RequestParam(name = "gardenSize") String gardenSize,
                                         @RequestParam(name = "longitude") String longitude,
                                         @RequestParam(name = "latitude") String latitude,
                                       @PathVariable Long gardenId, HttpSession session,
                                         HttpServletResponse response,
                                         Model model) {
        logger.info("POST / edited garden");


        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        Garden garden = optionalGarden.get();
        Long gardenOwnerId = garden.getOwner().getId();
        if (!securityService.isOwner(gardenOwnerId)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult gardenDescriptionResult = InputValidator.validateDescription(gardenDescription);
        ValidationResult streetAddressResult = InputValidator.optionalAlphaPlusTextField(streetAddress);
        ValidationResult suburbResult = InputValidator.optionalAlphaPlusTextField(suburb);
        ValidationResult cityResult = InputValidator.compulsoryAlphaPlusTextField(city);
        ValidationResult countryResult = InputValidator.compulsoryAlphaPlusTextField(country);
        ValidationResult postcodeResult = InputValidator.validatePostcodeInput(postcode);
        ValidationResult gardenSizeResult = InputValidator.validateGardenAreaInput(gardenSize);

        gardenFormErrorText(model, gardenNameResult, streetAddressResult, suburbResult, cityResult, countryResult,
                postcodeResult, gardenSizeResult, gardenDescriptionResult);

        if (gardenSize.isBlank()) {
            gardenSize = null;
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("gardenName", gardenName);
        model.addAttribute("gardenDescription", gardenDescription);
        model.addAttribute("streetAddress", streetAddress);
        model.addAttribute("suburb", suburb);
        model.addAttribute("city", city);
        model.addAttribute("country", country);
        model.addAttribute("postcode", postcode);
        model.addAttribute("gardenSize", gardenSize);
        model.addAttribute("latitude", latitude);
        model.addAttribute("longitude", longitude);

        if (!gardenNameResult.valid() || !streetAddressResult.valid() || !suburbResult.valid() || !cityResult.valid()
                || !countryResult.valid() || !postcodeResult.valid() || !gardenSizeResult.valid()
                || !gardenDescriptionResult.valid()) {
            return "editGardenForm";
        }
        Double doubleGardenSize;
        if (gardenSize == null) {
            doubleGardenSize = 0.0;
        } else {
            doubleGardenSize = Double.parseDouble(gardenSize.replace(",", "."));
        }

        User owner = securityService.getCurrentUser();
        boolean isPublic = false;
        gardenService.updateGarden(gardenId, new Garden(gardenName,gardenDescription,streetAddress,suburb,city,postcode,country,doubleGardenSize,isPublic,latitude,longitude, owner));
        logger.info("Edited Garden Page");

        session.setAttribute("userGardens", gardenService.getAllUsersGardens(owner.getId()));
        model.addAttribute("userGardens", session.getAttribute("userGardens"));

        return "redirect:/my-gardens/{gardenId}";
    }

    /**
     * takes as an input the result of validating the garden name, location and size
     * parameters and prints the appropriate
     */
    private void gardenFormErrorText(Model model, ValidationResult gardenNameResult,
            ValidationResult streetAddressResult,
            ValidationResult suburbResult, ValidationResult cityResult,
            ValidationResult countryResult, ValidationResult postcodeResult,
            ValidationResult gardenSizeResult, ValidationResult gardenDescriptionResult) {

        // notifies the user that the garden Name is invalid (if applicable)
        if (!gardenNameResult.valid()) {
            if (gardenNameResult == ValidationResult.LENGTH_OVER_LIMIT) {
                gardenNameResult.updateMessage("cannot be greater than 64 characters in length");
            }
            model.addAttribute("GNErrorText", "Garden name " + gardenNameResult);
            model.addAttribute("GNErrorClass", "errorBorder");
            logger.info("Garden Name failed validation");
        } else {
            model.addAttribute("GNErrorClass", "noErrorBorder");
        }

        // notifies the user that the street address is invalid (if applicable)
        if (!streetAddressResult.valid()) {
            if (streetAddressResult == ValidationResult.LENGTH_OVER_LIMIT) {
                streetAddressResult.updateMessage("cannot be longer than 96 characters");
            }
            model.addAttribute("AddressErrorText", "Address " + streetAddressResult);
            logger.info("Garden Street failed validation");
        }

        // notifies the user that the suburb is invalid (if applicable)
        if (!suburbResult.valid()) {
            if (suburbResult == ValidationResult.LENGTH_OVER_LIMIT) {
                suburbResult.updateMessage("cannot be longer than 96 characters");
            }
            model.addAttribute("SuburbErrorText", "Suburb " + suburbResult);
            model.addAttribute("SuburbErrorClass", "errorBorder");
            logger.info("Garden Suburb failed validation");
        } else {
            model.addAttribute("SuburbErrorClass", "noErrorBorder");
        }

        // notifies the user that the city input is invalid (if applicable)
        if(!cityResult.valid())
        {

            if (cityResult == ValidationResult.BLANK) {
                model.addAttribute("CityErrorText", "City and Country are required");
            } else {
                model.addAttribute("CityErrorText","City " + cityResult);
            }

            model.addAttribute("CityErrorClass","errorBorder");
            logger.info("Garden City failed validation");
        }
        else
        {
            model.addAttribute("CityErrorClass","noErrorBorder");
        }

        // notifies the user that the country input is invalid (if applicable)
        if(!countryResult.valid())
        {

            if (countryResult == ValidationResult.BLANK) {
                model.addAttribute("CountryErrorText", "City and Country are required");
            } else {
                model.addAttribute("CountryErrorText","Country " + countryResult);
            }

            model.addAttribute("CountryErrorClass","errorBorder");
            logger.info("Garden Country failed validation");
        } else {
            model.addAttribute("CountryErrorClass", "noErrorBorder");
        }

        // notifies the user that the postcode input is invalid (if applicable)
        if (!postcodeResult.valid()) {
            if (postcodeResult == ValidationResult.LENGTH_OVER_LIMIT) {
                postcodeResult.updateMessage("cannot be longer than 10 digits");
            }

            model.addAttribute("PostCodeErrorText", "Postcode " + postcodeResult);
            model.addAttribute("PostCodeErrorClass", "errorBorder");
            logger.info("Garden Postcode failed validation");
        }
        else
        {
            model.addAttribute("PostCodeErrorClass","noErrorBorder");
        }
        // notifies the user that the garden Size is invalid (if applicable)
        if (!gardenSizeResult.valid()) {
            String message;
            if (gardenSizeResult == ValidationResult.AREA_TOO_LARGE) {
                message = "is too large. \n\r Must be smaller than or equal to 8000000";
            } else if (gardenSizeResult == ValidationResult.AREA_TOO_SMALL) {
                message = "is too small. \n\r Must be larger than or equal to 0.01";
            } else {
                message = gardenSizeResult.toString();
            }
            gardenSizeResult.updateMessage(message);
            model.addAttribute("GSErrorText", "Garden size " + gardenSizeResult);
            model.addAttribute("GSErrorClass", "errorBorder");
            logger.info("Garden Size failed validation");

        } else {
            model.addAttribute("GSErrorClass", "noErrorBorder");
        }

        // notifies the user that the garden Description is invalid (if applicable)
        if (!gardenDescriptionResult.valid()) {
            model.addAttribute("GDErrorText", gardenDescriptionResult);
        }
    }

}
