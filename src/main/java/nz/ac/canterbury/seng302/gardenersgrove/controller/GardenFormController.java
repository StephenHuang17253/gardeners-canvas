package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.component.Constants;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenNavModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.LocationService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;

/**
 * Controller for creating a new Garden using a form page
 */
@Controller
@SessionAttributes(Constants.USER_GARDENS_ATTRIBUTE)
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

        logger.info("Time elapsed: {}", timeElapsed);
        // Every second, the number of available permits is reset to 2
        if (timeElapsed >= 1) {
            semaphore.drainPermits();
            semaphore.release(MAX_REQUESTS_PER_SECOND);
            logger.info("A second or more has elapsed, permits reset to: {}", semaphore.availablePermits());
            lastRequestTime = currentTime;
        }

        logger.info("Permits left before request: {}", semaphore.availablePermits());

        // Check if rate limit exceeded
        if (!semaphore.tryAcquire()) {
            logger.info("Exceeded location API rate limit of 2 requests per second.");
            return "429"; // Frontend script will check if this returns 429 to toggle error messages.
        }
        logger.info("Permits left after request: {}", semaphore.availablePermits());

        return locationService.getLocationSuggestions(query);

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
    @GetMapping("/api/location/coordinates")
    @ResponseBody
    public JsonNode getLatitudeLongitudeValues(@RequestParam("query") String query)
            throws IOException, InterruptedException {
        long currentTime = Instant.now().getEpochSecond();
        long timeElapsed = currentTime - lastRequestTime;

        logger.info("Time elapsed: {}", timeElapsed);
        // Every second, the number of available permits is reset to 2
        if (timeElapsed >= 1) {
            semaphore.drainPermits();
            semaphore.release(MAX_REQUESTS_PER_SECOND);
            logger.info("A second or more has elapsed, permits reset to: {}", semaphore.availablePermits());
            lastRequestTime = currentTime;
        }

        logger.info("Permits left before request: {}", semaphore.availablePermits());

        // Check if rate limit exceeded
        if (!semaphore.tryAcquire()) {
            logger.info("Exceeded location API rate limit of 2 requests per second.");

        }
        logger.info("Permits left after request: {}", semaphore.availablePermits());

        return locationService.getLatitudeLongitude(query);

    }

    /**
     * Maps the createNewGardenPage html page to /create-new-garden url
     *
     * @return thymeleaf createNewGardenPage
     */
    @GetMapping("/create-new-garden")
    public String newGardenForm(@RequestParam(name = Constants.GARDEN_NAME_ATTRIBUTE, required = false) String gardenName,
            @RequestParam(name = Constants.STREET_ADDRESS_ATTRIBUTE, required = false) String streetAddress,
            @RequestParam(name = Constants.GARDEN_DESCRIPTION_ATTRIBUTE, required = false) String gardenDescription,
            @RequestParam(name = Constants.SUBURB_ATTRIBUTE, required = false) String suburb,
            @RequestParam(name = "city", required = false) String city,
            @RequestParam(name = Constants.COUNTRY_ATTRIBUTE, required = false) String country,
            @RequestParam(name = Constants.POSTCODE_ATTRIBUTE, required = false) String postcode,
            @RequestParam(name = Constants.GARDEN_SIZE_ATTRIBUTE, required = false) String gardenSize,
            @RequestParam(name = Constants.LONGITUDE_ATTRIBUTE, required = false) String longitude,
            @RequestParam(name = Constants.LATITUDE_ATTRIBUTE, required = false) String latitude,
            Model model) {

        model.addAttribute(Constants.GARDEN_NAME_ATTRIBUTE, gardenName);
        model.addAttribute(Constants.GARDEN_DESCRIPTION_ATTRIBUTE, gardenDescription);
        model.addAttribute(Constants.STREET_ADDRESS_ATTRIBUTE, streetAddress);
        model.addAttribute(Constants.SUBURB_ATTRIBUTE, suburb);
        model.addAttribute("city", city);
        model.addAttribute(Constants.COUNTRY_ATTRIBUTE, country);
        model.addAttribute(Constants.POSTCODE_ATTRIBUTE, postcode);
        model.addAttribute(Constants.GARDEN_SIZE_ATTRIBUTE, gardenSize);

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
    public String submitNewGardenForm(@RequestParam(name = Constants.GARDEN_NAME_ATTRIBUTE) String gardenName,
            @RequestParam(name = Constants.GARDEN_DESCRIPTION_ATTRIBUTE) String gardenDescription,
            @RequestParam(name = Constants.STREET_ADDRESS_ATTRIBUTE) String streetAddress,
            @RequestParam(name = Constants.SUBURB_ATTRIBUTE) String suburb,
            @RequestParam(name = "city") String city,
            @RequestParam(name = Constants.COUNTRY_ATTRIBUTE) String country,
            @RequestParam(name = Constants.POSTCODE_ATTRIBUTE) String postcode,
            @RequestParam(name = Constants.GARDEN_SIZE_ATTRIBUTE) String gardenSize,
            @RequestParam(name = Constants.LONGITUDE_ATTRIBUTE) String longitude,
            @RequestParam(name = Constants.LATITUDE_ATTRIBUTE) String latitude,
            HttpSession session,
            Model model,
            RedirectAttributes redirectAttributes) throws IOException, InterruptedException {

        logger.info("POST /create-new-garden");

        // logic to handle checking if Garden Name, Garden Location and Garden size
        // fields are valid

        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult gardenDescriptionResult = InputValidator.validateDescription(gardenDescription);
        ValidationResult streetAddressResult = InputValidator.validateAddressInput(streetAddress);
        ValidationResult suburbResult = InputValidator.optionalAlphaPlusTextField(suburb, 100);
        ValidationResult cityResult = InputValidator.compulsoryAlphaPlusTextField(city, 200);
        ValidationResult countryResult = InputValidator.compulsoryAlphaPlusTextField(country, 100);
        ValidationResult postcodeResult = InputValidator.validatePostcodeInput(postcode);
        ValidationResult gardenSizeResult = InputValidator.validateGardenAreaInput(gardenSize);

        gardenFormErrorText(model, gardenNameResult, streetAddressResult, suburbResult, cityResult, countryResult,
                postcodeResult, gardenSizeResult, gardenDescriptionResult);

        if (gardenSize.isBlank()) {
            gardenSize = null;
        }

        model.addAttribute(Constants.GARDEN_NAME_ATTRIBUTE, gardenName);
        model.addAttribute(Constants.GARDEN_DESCRIPTION_ATTRIBUTE, gardenDescription);
        model.addAttribute(Constants.STREET_ADDRESS_ATTRIBUTE, streetAddress);
        model.addAttribute(Constants.SUBURB_ATTRIBUTE, suburb);
        model.addAttribute("city", city);
        model.addAttribute(Constants.COUNTRY_ATTRIBUTE, country);
        model.addAttribute(Constants.POSTCODE_ATTRIBUTE, postcode);
        model.addAttribute(Constants.GARDEN_SIZE_ATTRIBUTE, gardenSize);
        model.addAttribute(Constants.LATITUDE_ATTRIBUTE, latitude);
        model.addAttribute(Constants.LONGITUDE_ATTRIBUTE, longitude);

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

        User owner = securityService.getCurrentUser();

        Garden garden = new Garden(gardenName, gardenDescription, streetAddress, suburb, city, postcode, country,
                doubleGardenSize, isPublic, latitude, longitude, owner);

        User user = securityService.getCurrentUser();
        gardenService.addGarden(garden);
        List<Garden> gardens = gardenService.getAllUsersGardens(user.getId());
        List<GardenNavModel> gardenModels = new ArrayList<>();
        for (Garden g : gardens) {
            gardenModels.add(new GardenNavModel(g.getGardenId(), g.getGardenName()));
        }
        session.setAttribute(Constants.USER_GARDENS_ATTRIBUTE, gardenModels);
        model.addAttribute(Constants.USER_GARDENS_ATTRIBUTE, session.getAttribute(Constants.USER_GARDENS_ATTRIBUTE));

        findingGardenCoordinates(garden);

        redirectAttributes.addAttribute("gardenId", garden.getGardenId());

        return "redirect:/my-gardens/{gardenId}";
    }

    private void findingGardenCoordinates(Garden garden) throws IOException, InterruptedException {
        if (Objects.equals(garden.getGardenLatitude(), "")) {
            JsonNode coordData = getLatitudeLongitudeValues(garden.getGardenLocation());
            if (coordData.get(0) != null) {
                String lat = coordData.get(0).get("lat").asText();
                String lon = coordData.get(0).get("lon").asText();
                garden.updateLocation(lat, lon);
                gardenService.updateGardenCoordinates(garden.getGardenId(), lat, lon);
                logger.info("Forward geocoding request made to get lat and lon");
            }
        }
    }

    /**
     * Maps the editGardenPage html page to /my-gardens/{gardenId}/edit url
     *
     * @return thymeleaf editGardenPage
     */
    @GetMapping("/my-gardens/{gardenId}/edit")
    public String editGardenDetails(@PathVariable Long gardenId,
            HttpServletResponse response,
            Model model) {
        logger.info("GET /my-gardens/{}", gardenId);

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
        model.addAttribute(Constants.GARDEN_NAME_ATTRIBUTE, garden.getGardenName());
        model.addAttribute(Constants.GARDEN_DESCRIPTION_ATTRIBUTE, garden.getGardenDescription());
        model.addAttribute(Constants.STREET_ADDRESS_ATTRIBUTE, garden.getGardenAddress());
        model.addAttribute(Constants.SUBURB_ATTRIBUTE, garden.getGardenSuburb());
        model.addAttribute("city", garden.getGardenCity());
        model.addAttribute(Constants.POSTCODE_ATTRIBUTE, garden.getGardenPostcode());
        model.addAttribute(Constants.COUNTRY_ATTRIBUTE, garden.getGardenCountry());
        model.addAttribute(Constants.LATITUDE_ATTRIBUTE, garden.getGardenLatitude());
        model.addAttribute(Constants.LONGITUDE_ATTRIBUTE, garden.getGardenLongitude());
        double gardenSize = garden.getGardenSize();
        if (gardenSize == 0.0) {
            model.addAttribute(Constants.GARDEN_SIZE_ATTRIBUTE, "");
        } else {
            model.addAttribute(Constants.GARDEN_SIZE_ATTRIBUTE, gardenSize);
        }
        return "editGardenPage";
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
    public String submitEditedGardenForm(@RequestParam(name = Constants.GARDEN_NAME_ATTRIBUTE) String gardenName,
            @RequestParam(name = Constants.GARDEN_DESCRIPTION_ATTRIBUTE) String gardenDescription,
            @RequestParam(name = Constants.STREET_ADDRESS_ATTRIBUTE) String streetAddress,
            @RequestParam(name = Constants.SUBURB_ATTRIBUTE) String suburb,
            @RequestParam(name = "city") String city,
            @RequestParam(name = Constants.COUNTRY_ATTRIBUTE) String country,
            @RequestParam(name = Constants.POSTCODE_ATTRIBUTE) String postcode,
            @RequestParam(name = Constants.GARDEN_SIZE_ATTRIBUTE) String gardenSize,
            @RequestParam(name = Constants.LONGITUDE_ATTRIBUTE) String longitude,
            @RequestParam(name = Constants.LATITUDE_ATTRIBUTE) String latitude,
            @PathVariable Long gardenId, HttpSession session,
            HttpServletResponse response,
            Model model) throws IOException, InterruptedException {
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

        ValidationResult gardenNameResult = InputValidator.compulsoryAlphaPlusTextField(gardenName);
        ValidationResult gardenDescriptionResult = InputValidator.validateDescription(gardenDescription);
        ValidationResult streetAddressResult = InputValidator.validateAddressInput(streetAddress);
        ValidationResult suburbResult = InputValidator.optionalAlphaPlusTextField(suburb, 100);
        ValidationResult cityResult = InputValidator.compulsoryAlphaPlusTextField(city, 200);
        ValidationResult countryResult = InputValidator.compulsoryAlphaPlusTextField(country, 100);
        ValidationResult postcodeResult = InputValidator.validatePostcodeInput(postcode);
        ValidationResult gardenSizeResult = InputValidator.validateGardenAreaInput(gardenSize);

        gardenFormErrorText(model, gardenNameResult, streetAddressResult, suburbResult, cityResult, countryResult,
                postcodeResult, gardenSizeResult, gardenDescriptionResult);

        if (gardenSize.isBlank()) {
            gardenSize = null;
        }

        if (!gardenNameResult.valid() || !streetAddressResult.valid() || !suburbResult.valid() || !cityResult.valid()
                || !countryResult.valid() || !postcodeResult.valid() || !gardenSizeResult.valid()
                || !gardenDescriptionResult.valid()) {
            model.addAttribute(Constants.GARDEN_NAME_ATTRIBUTE, gardenName);
            model.addAttribute(Constants.GARDEN_DESCRIPTION_ATTRIBUTE, gardenDescription);
            model.addAttribute(Constants.STREET_ADDRESS_ATTRIBUTE, streetAddress);
            model.addAttribute(Constants.SUBURB_ATTRIBUTE, suburb);
            model.addAttribute("city", city);
            model.addAttribute(Constants.COUNTRY_ATTRIBUTE, country);
            model.addAttribute(Constants.POSTCODE_ATTRIBUTE, postcode);
            model.addAttribute(Constants.GARDEN_SIZE_ATTRIBUTE, gardenSize);
            model.addAttribute(Constants.LATITUDE_ATTRIBUTE, latitude);
            model.addAttribute(Constants.LONGITUDE_ATTRIBUTE, longitude);
            return "editGardenPage";
        }
        Double doubleGardenSize;
        if (gardenSize == null) {
            doubleGardenSize = 0.0;
        } else {
            doubleGardenSize = Double.parseDouble(gardenSize.replace(",", "."));
        }

        User owner = securityService.getCurrentUser();
        boolean isPublic = false;
        Garden updatedGarden = gardenService.updateGarden(gardenId,
                new Garden(gardenName, gardenDescription, streetAddress, suburb, city,
                        postcode, country, doubleGardenSize, isPublic, latitude, longitude, owner));
        logger.info("Edited Garden Page");

        findingGardenCoordinates(updatedGarden);

        User user = securityService.getCurrentUser();
        List<Garden> gardens = gardenService.getAllUsersGardens(user.getId());
        List<GardenNavModel> gardenModels = new ArrayList<>();
        for (Garden g : gardens) {
            gardenModels.add(new GardenNavModel(g.getGardenId(), g.getGardenName()));
        }
        session.setAttribute(Constants.USER_GARDENS_ATTRIBUTE, gardenModels);
        model.addAttribute(Constants.USER_GARDENS_ATTRIBUTE, session.getAttribute(Constants.USER_GARDENS_ATTRIBUTE));

        return "redirect:/my-gardens/{gardenId}";
    }

    /**
     * takes as an input the result of validating the garden name, location and size
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
        }

        // notifies the user that the street address is invalid (if applicable)
        if (!streetAddressResult.valid()) {
            if (streetAddressResult == ValidationResult.LENGTH_OVER_LIMIT) {
                streetAddressResult.updateMessage("cannot be longer than 200 characters");
            }
            model.addAttribute("AddressErrorText", "Address " + streetAddressResult);
        }

        // notifies the user that the suburb is invalid (if applicable)
        if (!suburbResult.valid()) {
            if (suburbResult == ValidationResult.LENGTH_OVER_LIMIT) {
                suburbResult.updateMessage("cannot be longer than 100 characters");
            }
            model.addAttribute("SuburbErrorText", "Suburb " + suburbResult);
        }

        // notifies the user that the city input is invalid (if applicable)
        if (!cityResult.valid()) {
            String message;
            if (cityResult == ValidationResult.BLANK) {
                message = "City and Country are required";
            } else {
                message = "City " + cityResult.toString();
            }
            model.addAttribute("CityErrorText", message);
        }

        // notifies the user that the country input is invalid (if applicable)
        if (!countryResult.valid()) {
            String message;
            if (countryResult == ValidationResult.BLANK) {
                message = "City and Country are required";
            } else {
                message = "Country " + countryResult.toString();
            }
            model.addAttribute("CountryErrorText", message);
        }

        // notifies the user that the postcode input is invalid (if applicable)
        if (!postcodeResult.valid()) {
            if (postcodeResult == ValidationResult.LENGTH_OVER_LIMIT) {
                postcodeResult.updateMessage("cannot be longer than 10 digits");
            }
            model.addAttribute("PostCodeErrorText", "Postcode " + postcodeResult);
        }

        // notifies the user that the garden Size is invalid (if applicable)
        if (!gardenSizeResult.valid()) {
            String message = gardenSizeResult.toString();
            gardenSizeResult.updateMessage(message);
            model.addAttribute("GSErrorText", "Garden size " + gardenSizeResult);
        }

        // notifies the user that the garden Description is invalid (if applicable)
        if (!gardenDescriptionResult.valid()) {
            model.addAttribute("GDErrorText", gardenDescriptionResult);
        }
    }

}
