package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.model.WeatherModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Plant;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.PlantService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.WeatherService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.support.RequestContextUtils;

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

    private final WeatherService weatherService;

    private final ObjectMapper objectMapper;

    private static final int MAX_REQUESTS_PER_SECOND = 10;

    private final Semaphore semaphore = new Semaphore(MAX_REQUESTS_PER_SECOND);

    private static final int COUNT_PER_PAGE = 10;

    private volatile long lastRequestTime = Instant.now().getEpochSecond();

    /**
     * Constructor for the GardensController with {@link Autowired} to connect
     * this controller with other services
     *
     * @param gardenService   service to access garden repository
     * @param securityService service to access security methods
     * @param plantService    service to access plant repository
     */
    @Autowired
    public GardensController(GardenService gardenService, SecurityService securityService, PlantService plantService,
            WeatherService weatherService, ObjectMapper objectMapper) {
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.securityService = securityService;
        this.weatherService = weatherService;
        this.objectMapper = objectMapper;
    }

    /**
     * Maps the myGardensPage html file to /my-gardens url
     *
     * @param page   page number
     * @param filter string of filter values: None, Public, Private
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens")
    public String myGardens(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "All") String filter,
            Model model) {
        logger.info("GET /my-gardens");

        model.addAttribute("loggedIn", securityService.isLoggedIn());
        User user = securityService.getCurrentUser();
        List<Garden> gardens = gardenService.getAllUsersGardens(user.getId());

        long publicGardensCount = gardens.stream().filter(Garden::getIsPublic).count();
        long privateGardensCount = gardens.stream().filter(garden -> !garden.getIsPublic()).count();

        if (Objects.equals(filter, "Public")) {
            gardens = gardens.stream().filter(Garden::getIsPublic).toList();
        } else if (Objects.equals(filter, "Private")) {
            gardens = gardens.stream().filter(garden -> !garden.getIsPublic()).toList();
        }

        int totalPages = (int) Math.ceil((double) gardens.size() / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, gardens.size());

        model.addAttribute("myGardens", gardens.subList(startIndex, endIndex));
        model.addAttribute("currentPage", page);
        model.addAttribute("lastPage", totalPages);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalGardens", gardens.size());
        model.addAttribute("filter", filter);
        model.addAttribute("publicGardensCount", publicGardensCount);
        model.addAttribute("privateGardensCount", privateGardensCount);
        model.addAttribute("userName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("profilePicture", user.getProfilePictureFilename());

        return "myGardensPage";
    }

    /**
     * Helper method for getting weather data for a garden
     *
     * @param garden Garden entity of
     * @return list of DailyWeather components
     */
    private List<WeatherModel> getGardenWeatherData(Garden garden) {
        List<WeatherModel> weatherList = new ArrayList<>();
        DailyWeather noWeather = null;
        try {
            WeatherResponseData gardenWeather = showGardenWeather(garden.getGardenLatitude(),
                    garden.getGardenLongitude());
            List<DailyWeather> pastWeather = gardenWeather.getPastWeather();
            weatherList.add(new WeatherModel(pastWeather.get(0)));
            weatherList.add(new WeatherModel(pastWeather.get(1)));
            weatherList.add(new WeatherModel(gardenWeather.getCurrentWeather()));
            weatherList.addAll(gardenWeather.getForecastWeather().stream()
                    .map(WeatherModel::new)
                    .collect(Collectors.toList()));
        } catch (NullPointerException error) {
            noWeather = new DailyWeather("no_weather_available_icon.png", null, null);
            noWeather.setError("Location not found, please update your location to see the weather");
        } catch (UnavailableException e) {
            noWeather = new DailyWeather("not_found.png", null, null);
        }

        if (noWeather != null) {
            weatherList.add(new WeatherModel(noWeather));
        }

        return weatherList;
    }

    private void handleWeatherMessages(List<WeatherModel> weatherList, Model model){
        WeatherModel beforeYesterdayWeather = weatherList.get(0);
        WeatherModel yesterdayWeather = weatherList.get(1);
        WeatherModel currentWeather = weatherList.get(2);
        if (currentWeather.getDescription().equals("Rainy")) {
            model.addAttribute("message", "Outdoor plants don’t need any water today");
            model.addAttribute("goodMessage", true);
        }

        if (Objects.equals(beforeYesterdayWeather.getDescription(), "Sunny")
                && Objects.equals(yesterdayWeather.getDescription(), "Sunny")
                && Objects.equals(currentWeather.getDescription(), "Sunny")) {
            model.addAttribute("message", "There hasn’t been any rain recently, make sure to water your plants if they need it");
            model.addAttribute("goodMessage", false);
        }
    }

    private void handlePagniation(int page, int listLength, Model model){
        int totalPages = (int) Math.ceil((double) listLength / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, listLength);

        model.addAttribute("currentPage", page);
        model.addAttribute("lastPage", totalPages);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
    }

    /**
     * Get Mapping of the /my-gardens/{gardenId} endpoint Garden Details page of
     * all the plants belonging to the garden
     *
     * @param gardenId id of the garden used in the end-point path
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("/my-gardens/{gardenId}")
    public String showGardenDetails(@PathVariable Long gardenId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String plantPictureError,
            @RequestParam(required = false) String weatherListJson,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model) throws JsonProcessingException {
        logger.info("GET /my-gardens/{}", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("message",
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        List<WeatherModel> weatherList = null;

        if(weatherListJson != null){
            weatherList = objectMapper.readValue(weatherListJson, new TypeReference<List<WeatherModel>>() {});
        }

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            weatherList = (List<WeatherModel>) inputFlashMap.get("weatherList");
        }

        if(weatherList == null || weatherList.isEmpty()){
            weatherList = getGardenWeatherData(garden);
        }
        if (weatherList.size() > 1) {
            handleWeatherMessages(weatherList, model);
        }

        User user = garden.getOwner();
        List<Plant> plants = garden.getPlants();
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        handlePagniation(page, plants.size(), model);

        weatherListJson = objectMapper.writeValueAsString(weatherList);
        model.addAttribute("weatherListJson", weatherListJson);

        model.addAttribute("isOwner", true);
        model.addAttribute("garden", new GardenDetailModel(garden));
        model.addAttribute("weatherList", weatherList);
        model.addAttribute("gradientClass", "g" + LocalTime.now().getHour());
        model.addAttribute("currentTime", formattedTime);
        model.addAttribute("profilePicture", user.getProfilePictureFilename());
        model.addAttribute("userName", user.getFirstName() + " " + user.getLastName());
        model.addAttribute("plantPictureError", plantPictureError);

        return "gardenDetailsPage";
    }

    /**
     * This function creates a post mapping for updating the garden's isPublic
     * boolean.
     *
     * @param gardenId         - the id of the garden being edited
     * @param makeGardenPublic - the new status of the garden isPublic
     *
     * @return thymeleaf garden detail page
     */
    @PostMapping("/my-gardens/{gardenId}/public")
    public String updateGardenPublicStatus(@PathVariable Long gardenId,
            @RequestParam(name = "makeGardenPublic", required = false, defaultValue = "false") boolean makeGardenPublic,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam("weatherListJson")  String weatherListJson,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response) throws JsonProcessingException {
        logger.info("POST /my-gardens/{}/public", gardenId);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }
        logger.info(weatherListJson);

        Garden garden = optionalGarden.get();

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }

        gardenService.updateGardenPublicity(garden.getGardenId(), makeGardenPublic);
        List<WeatherModel> weatherList = objectMapper.readValue(weatherListJson, new TypeReference<List<WeatherModel>>() {});
        redirectAttributes.addFlashAttribute("page", page);
        redirectAttributes.addFlashAttribute("weatherList", weatherList);

        return "redirect:/my-gardens/{gardenId}";

    }

    /**
     * This function is called when a user tries to update a plants image
     * directly from the My Garden's page instead of one of the plant forms.
     *
     * @param gardenId id of the garden being edited
     * @param plantId        id of the plant being edited
     * @param plantPicture   the new picture
     * @param model          the model
     * @return thymeleaf gardenDetails
     */
    @PostMapping("/my-gardens/{gardenId}")
    public String updatePlantImage(@PathVariable Long gardenId,
            @RequestParam("plantId") Long plantId,
            @RequestParam("weatherListJson")  String weatherListJson,
            @RequestParam("plantPictureInput") MultipartFile plantPicture,
            @RequestParam(defaultValue = "1") int page,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response,
            Model model) throws JsonProcessingException {
        logger.info("POST /my-gardens/{}", gardenId);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        Optional<Plant> plantToUpdate = plantService.findById(plantId);
        if (optionalGarden.isEmpty() || plantToUpdate.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }

        ValidationResult plantPictureResult = FileValidator.validateImage(plantPicture, 10, FileType.IMAGES);
        if (plantPicture.isEmpty()) {
            plantPictureResult = ValidationResult.OK;
        }

        List<WeatherModel> weatherList = objectMapper.readValue(weatherListJson, new TypeReference<List<WeatherModel>>() {});
        redirectAttributes.addFlashAttribute("weatherList", weatherList);
        redirectAttributes.addAttribute("plantToEditId", plantId);
        redirectAttributes.addAttribute("page", page);

        if (plantPictureResult.valid() && !plantPicture.isEmpty()) {
            plantService.updatePlantPicture(plantToUpdate.get(), plantPicture);
        } else {
            redirectAttributes.addAttribute("plantPictureError", plantPictureResult.toString());
        }
        return "redirect:/my-gardens/{gardenId}";

    }

    /**
     * This function is called when trying to access another user's gardens.
     *
     * @param model - the model
     * @param userId - id of the user being viewed
     * @return thymeleaf gardensPage
     */
    @GetMapping("/{userId}/gardens")
    public String friendsGardens(Model model,
            @PathVariable("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "All") String filter,
            HttpServletResponse response) {
        logger.info("GET {}/gardens", userId);

        User friend;
        try {
            friend = securityService.checkFriendship(userId, FriendshipStatus.ACCEPTED);
            if (friend == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return "403";
            }
        } catch (Exception exception) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return "403";
        }
        String friendName = String.format("%s %s", friend.getFirstName(), friend.getLastName());
        List<Garden> friendGardens = friend.getGardens();
        model.addAttribute("friendName", friendName);
        model.addAttribute("friendGardens", friendGardens);

        long publicGardensCount = friendGardens.stream().filter(Garden::getIsPublic).count();
        List<Garden> gardens = friendGardens.stream().filter(Garden::getIsPublic).toList();

        int totalPages = (int) Math.ceil((double) gardens.size() / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, gardens.size());

        model.addAttribute("friendId", userId);
        model.addAttribute("myGardens", gardens.subList(startIndex, endIndex));
        model.addAttribute("currentPage", page);
        model.addAttribute("lastPage", totalPages);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("totalGardens", gardens.size());
        model.addAttribute("publicGardensCount", publicGardensCount);
        model.addAttribute("userName", friend.getFirstName() + " " + friend.getLastName());
        model.addAttribute("firstName", friend.getFirstName());
        model.addAttribute("profilePicture", friend.getProfilePictureFilename());

        return "gardensPage";
    }

    WeatherResponseData showGardenWeather(String gardenLatitude, String gardenLongitude) throws UnavailableException {

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
            throw new UnavailableException("429");
        }
        logger.info("Permits left after request: {}", semaphore.availablePermits());
        return weatherService.getWeather(gardenLatitude, gardenLongitude);

    }

}
