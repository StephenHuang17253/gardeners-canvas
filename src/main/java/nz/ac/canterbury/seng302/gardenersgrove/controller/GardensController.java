package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Semaphore;


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

    private final WeatherService weatherService;

    private static final int MAX_REQUESTS_PER_SECOND = 10;

    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND);

    private volatile long lastRequestTime = Instant.now().getEpochSecond();
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
    public GardensController(GardenService gardenService, SecurityService securityService, PlantService plantService, FileService fileService, WeatherService weatherService) {
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.fileService = fileService;
        this.securityService = securityService;
        this.weatherService = weatherService;
    }

    /**
     * Maps the myGardensPage html file to /my-gardens url
     *
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens")
    public String myGardens(Model model) {
        logger.info("GET /my-gardens");

        model.addAttribute("loggedIn",  securityService.isLoggedIn());
        User user = securityService.getCurrentUser();
        model.addAttribute("myGardens", gardenService.getAllUsersGardens(user.getId()));

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

        model.addAttribute("loggedIn",  securityService.isLoggedIn());

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();

        if (!securityService.isOwner(garden.getOwner().getId())) {
            if (!garden.getIsPublic()) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return "403";
            }
            List<DailyWeather> weather = new ArrayList<>();
            try {
                WeatherResponseData gardenWeather = showGardenWeather(garden.getGardenLatitude(), garden.getGardenLongitude());
                weather.add(gardenWeather.getCurrentWeather());
                weather.addAll(gardenWeather.getForecastWeather());

                DailyWeather currentWeather = gardenWeather.getCurrentWeather();
                List<DailyWeather> pastWeather = gardenWeather.getPastWeather();
                DailyWeather yesterdayWeather = pastWeather.get(0);
                DailyWeather beforeYesterdayWeather = pastWeather.get(1);

                if (currentWeather.getDescription().equals("Rainy")) {
                    model.addAttribute("message","Outdoor plants don’t need any water today");
                } else if ("Sunny".equals(yesterdayWeather.getDescription()) && "Sunny".equals(beforeYesterdayWeather.getDescription())) {
                    model.addAttribute("message","There hasn’t been any rain recently, make sure to water your plants if they need it");
                }

            } catch (Error error) {
                DailyWeather noWeather = new DailyWeather("not_found.png", null, null);
            } catch (NullPointerException error) {
                DailyWeather noWeather = new DailyWeather("no_weather_available_icon.png", null, null);
                noWeather.setError("Location not found, please update your location to see the weather");
                weather.add(noWeather);
            }

            model.addAttribute("isOwner", false);
            model.addAttribute("gardenName", garden.getGardenName());
            model.addAttribute("gardenLocation", garden.getGardenLocation());
            model.addAttribute("gardenSize", garden.getGardenSize());
            model.addAttribute("gardenDescription", garden.getGardenDescription());
            model.addAttribute("gardenId", gardenId);
            model.addAttribute("plants", garden.getPlants());
            model.addAttribute("totalPlants", garden.getPlants().size());
            model.addAttribute("makeGardenPublic", garden.getIsPublic());
            model.addAttribute("weather", weather);
            return "gardenDetailsPage";
        }

        List<DailyWeather> weather = new ArrayList<>();
        try {
            WeatherResponseData gardenWeather = showGardenWeather(garden.getGardenLatitude(), garden.getGardenLongitude());
            weather.add(gardenWeather.getCurrentWeather());
            weather.addAll(gardenWeather.getForecastWeather());

            DailyWeather currentWeather = gardenWeather.getCurrentWeather();
            List<DailyWeather> pastWeather = gardenWeather.getPastWeather();
            DailyWeather yesterdayWeather = pastWeather.get(0);
            DailyWeather beforeYesterdayWeather = pastWeather.get(1);

            if (currentWeather.getDescription().equals("Rainy")) {
                model.addAttribute("message","Outdoor plants don’t need any water today");
            } else if ("Sunny".equals(yesterdayWeather.getDescription()) && "Sunny".equals(beforeYesterdayWeather.getDescription())) {
                model.addAttribute("message","There hasn’t been any rain recently, make sure to water your plants if they need it");
            }

        } catch (Error error) {
            DailyWeather noWeather = new DailyWeather("not_found.png", null, null);
        } catch (NullPointerException error) {
            DailyWeather noWeather = new DailyWeather("no_weather_available_icon.png", null, null);
            noWeather.setError("Location not found, please update your location to see the weather");
            weather.add(noWeather);
        }

        model.addAttribute("isOwner", true);
        model.addAttribute("gardenName", garden.getGardenName());
        model.addAttribute("gardenLocation", garden.getGardenLocation());
        model.addAttribute("gardenSize", garden.getGardenSize());
        model.addAttribute("gardenDescription", garden.getGardenDescription());
        model.addAttribute("gardenId", gardenId);
        model.addAttribute("plants", garden.getPlants());
        model.addAttribute("totalPlants", garden.getPlants().size());
        model.addAttribute("makeGardenPublic", garden.getIsPublic());
        model.addAttribute("weather", weather);
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
                                           @RequestParam(name = "makeGardenPublic", required = false, defaultValue = "false") boolean makeGardenPublic,
                                    HttpServletResponse response,
                                    Model model) {
        logger.info("POST /my-gardens/{gardenId}/public", gardenId);
        logger.info("Value of makeGardenPublic: {}", makeGardenPublic);

        model.addAttribute("loggedIn",  securityService.isLoggedIn());

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
        if (plantToUpdate.isEmpty()) {
            return "404";
        }

        model.addAttribute("loggedIn",  securityService.isLoggedIn());


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

        model.addAttribute("loggedIn",  securityService.isLoggedIn());

        User friend;
        try {
            friend = securityService.checkFriendship(userId, FriendshipStatus.ACCEPTED);
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
        model.addAttribute("friendGardens", gardenList);

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

        if (filename != null && !filename.isEmpty()) {
            plantPictureString = MvcUriComponentsBuilder
                    .fromMethodName(PlantFormController.class, "serveFile", filename)
                    .build()
                    .toUri()
                    .toString();
        }
        return plantPictureString;
    }


    WeatherResponseData showGardenWeather(String gardenLatitude, String gardenLongitude) {

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
            throw new Error("429");
        }
        logger.info("Permits left after request: " + semaphore.availablePermits());
        return weatherService.getWeather(gardenLatitude,gardenLongitude);

    }

}

