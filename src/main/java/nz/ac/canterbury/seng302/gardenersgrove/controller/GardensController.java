package nz.ac.canterbury.seng302.gardenersgrove.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.component.Constants;
import nz.ac.canterbury.seng302.gardenersgrove.component.DailyWeather;
import nz.ac.canterbury.seng302.gardenersgrove.component.WeatherResponseData;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.WeatherModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import nz.ac.canterbury.seng302.gardenersgrove.util.PriorityType;
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.fileValidation.FileValidator;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private final GardenTagService gardenTagService;
    private final ProfanityService profanityService;
    private final ObjectMapper objectMapper;

    private static final int COUNT_PER_PAGE = 9;

    /**
     * Constructor for the GardensController with {@link Autowired} to connect
     * this controller with other services
     *
     * @param gardenService    service to access garden repository
     * @param securityService  service to access security methods
     * @param plantService     service to access plant repository
     * @param weatherService   service to perform weather api calls
     * @param objectMapper     used for JSON conversion
     * @param gardenTagService service to access tag repository
     */
    @Autowired
    public GardensController(GardenService gardenService, SecurityService securityService, PlantService plantService,
            WeatherService weatherService, ObjectMapper objectMapper, GardenTagService gardenTagService,
            ProfanityService profanityService) {
        this.gardenService = gardenService;
        this.plantService = plantService;
        this.securityService = securityService;
        this.weatherService = weatherService;
        this.gardenTagService = gardenTagService;
        this.objectMapper = objectMapper;
        this.profanityService = profanityService;
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
            HttpServletRequest request,
            Model model) {
        logger.info("GET /my-gardens");

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
        model.addAttribute(Constants.CURRENT_PAGE_ATTRIBUTE, page);
        model.addAttribute(Constants.LAST_PAGE_ATTRIBUTE, totalPages);
        model.addAttribute(Constants.START_INDEX_ATTRIBUTE, startIndex + 1);
        model.addAttribute(Constants.END_INDEX_ATTRIBUTE, endIndex);
        model.addAttribute("totalGardens", gardens.size());
        model.addAttribute("filter", filter);
        model.addAttribute("publicGardensCount", publicGardensCount);
        model.addAttribute("privateGardensCount", privateGardensCount);
        model.addAttribute(Constants.USER_NAME_ATTRIBUTE, user.getFirstName() + " " + user.getLastName());
        model.addAttribute(Constants.PROFILE_PICTURE_ATTRIBUTE, user.getProfilePictureFilename());

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
            WeatherResponseData gardenWeather = weatherService.getWeather(garden.getGardenLatitude(),
                    garden.getGardenLongitude());

            weatherList.addAll(gardenWeather.getRetrievedWeatherData().stream()
                    .map(WeatherModel::new)
                    .toList());
        } catch (NullPointerException error) {
            noWeather = new DailyWeather("no_weather_available_icon.png", null, null);
            noWeather.setError("Location not found, please update your location to see the weather");
        }

        if (noWeather != null) {
            weatherList.add(new WeatherModel(noWeather));
        }

        return weatherList;
    }

    private void handleWeatherMessages(List<WeatherModel> weatherList, Garden garden, Model model) {
        WeatherModel beforeYesterdayWeather = weatherList.get(0);
        WeatherModel yesterdayWeather = weatherList.get(1);
        WeatherModel currentWeather = weatherList.get(2);
        if (currentWeather.getDescription().equals("Rainy")) {
            model.addAttribute(Constants.MESSAGE_ATTRIBUTE, "Outdoor plants don't need any water today");
            model.addAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, true);
        }

        if (Objects.equals(beforeYesterdayWeather.getDescription(), Constants.SUNNY_ATTRIBUTE)
                && Objects.equals(yesterdayWeather.getDescription(), Constants.SUNNY_ATTRIBUTE)
                && Objects.equals(currentWeather.getDescription(), Constants.SUNNY_ATTRIBUTE)) {
            model.addAttribute(Constants.MESSAGE_ATTRIBUTE,
                    "There hasn't been any rain recently, make sure to water your plants if they need it");
            model.addAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
            gardenService.changeGardenNeedsWatering(garden.getGardenId(), true);
        } else {
            gardenService.changeGardenNeedsWatering(garden.getGardenId(), false);
        }
    }

    private void handlePagniation(int page, int listLength, List<Plant> plants, Model model) {
        int totalPages = (int) Math.ceil((double) listLength / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, listLength);

        model.addAttribute(Constants.CURRENT_PAGE_ATTRIBUTE, page);
        model.addAttribute(Constants.LAST_PAGE_ATTRIBUTE, totalPages);
        model.addAttribute(Constants.START_INDEX_ATTRIBUTE, startIndex + 1);
        model.addAttribute(Constants.END_INDEX_ATTRIBUTE, endIndex);
        model.addAttribute("plants", plants.subList(startIndex, endIndex));
        model.addAttribute("plantCount", plants.size());
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
            RedirectAttributes redirectAttributes,
            Model model) throws ServletException {
        logger.info("GET /my-gardens/{}", gardenId);
        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();

        if (!securityService.isOwner(garden.getOwner().getId())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute(Constants.MESSAGE_ATTRIBUTE,
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";
        }

        securityService.addUserInteraction(gardenId, ItemType.GARDEN, LocalDateTime.now());

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);

        List<WeatherModel> weatherList = null;

        try {
            if (weatherListJson != null) {
                weatherList = objectMapper.readValue(weatherListJson, new TypeReference<List<WeatherModel>>() {
                });
            }
        } catch (Exception e) {
            logger.error("An error occurred while mapping the weatherListJson: {}", e.getMessage());
        }

        try {
            if (inputFlashMap != null) {
                weatherList = (List<WeatherModel>) inputFlashMap.get("weatherList");
            }
        } catch (Exception e) {
            logger.error("An error occurred while getting the weather list from the flash map: {}", e.getMessage());
        }

        if (weatherList == null || weatherList.isEmpty()) {
            weatherList = getGardenWeatherData(garden);
        }
        if (weatherList.size() > 1) {
            handleWeatherMessages(weatherList, garden, model);
        }

        User user = garden.getOwner();
        List<Plant> plants = garden.getPlants();
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        handlePagniation(page, plants.size(), garden.getPlants(), model);

        try {
            if (weatherListJson != null) {
                weatherList = objectMapper.readValue(weatherListJson, new TypeReference<List<WeatherModel>>() {
                });
            }
        } catch (Exception e) {
            logger.error("An error occurred while reading the weatherListJson: {}", e.getMessage());
        }

        weatherListJson = null;

        try {
            weatherListJson = objectMapper.writeValueAsString(weatherList);
        } catch (Exception e) {
            logger.error("An error occurred while writing the weatherListJson: {}", e.getMessage());
            logger.error("An error occurred while writing the weatherListJson: {}", e.getMessage());
        }

        model.addAttribute("weatherListJson", weatherListJson);

        model.addAttribute("isOwner", true);
        model.addAttribute("garden", new GardenDetailModel(garden));
        model.addAttribute("weatherList", weatherList);
        model.addAttribute("gradientClass", "g" + LocalTime.now().getHour());
        model.addAttribute("currentTime", formattedTime);
        model.addAttribute(Constants.PROFILE_PICTURE_ATTRIBUTE, user.getProfilePictureFilename());
        model.addAttribute(Constants.USER_NAME_ATTRIBUTE, user.getFirstName() + " " + user.getLastName());
        model.addAttribute("plantPictureError", plantPictureError);

        // Used for displaying messages after a redirect e.g. from the verify page
        if (inputFlashMap != null) {
            model.addAttribute(Constants.OPEN_MODAL_ATTRIBUTE, inputFlashMap.get(Constants.OPEN_MODAL_ATTRIBUTE));
            model.addAttribute(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE, inputFlashMap.get(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE));

            // The below If statements check if the status of a pending tag has
            // been updated and adjust error messages acrodingly if thats the case
            if (inputFlashMap.get(Constants.PENDING_TAG_NAME_ATTRIBUTE) == null) {
                model.addAttribute(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE, inputFlashMap.get(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE));
            } else {
                Optional<GardenTag> pendingTag = gardenTagService
                        .getByName(inputFlashMap.get(Constants.PENDING_TAG_NAME_ATTRIBUTE).toString());

                if (pendingTag.isPresent()) {
                    if (pendingTag.get().getTagStatus() == TagStatus.INAPPROPRIATE) {
                        // security service will return old strike count so adding one to account for
                        // this tag.
                        int userStrikes = securityService.getCurrentUser().getStrikes() + 1;
                        logger.info("{} now has {} strikes", garden.getOwner().getFirstName(), userStrikes);
                        model.addAttribute(Constants.TAG_ERROR_TEXT_ATTRIBUTE, "This tag does not meet the language " +
                                "standards for Gardener's Grove. A warning strike has been added to your account");
                        if (userStrikes == 5) {
                            model.addAttribute(Constants.TAG_ERROR_TEXT_ATTRIBUTE,
                                    "You have added an inappropriate tag for the fifth time." +
                                            " You have been sent a warning email. " +
                                            "If you add another inappropriate tag, you will be banned for a week.");
                        } else if (userStrikes == 6) {
                            redirectAttributes.addFlashAttribute(Constants.MESSAGE_ATTRIBUTE,
                                    "Your account is blocked for 7 days due to inappropriate conduct");
                            redirectAttributes.addFlashAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
                            request.logout();
                            return "redirect:/login";
                        }
                        model.addAttribute(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE, "");
                    } else if (pendingTag.get().getTagStatus() == TagStatus.APPROPRIATE) {
                        model.addAttribute(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE, "");
                    } else {
                        model.addAttribute(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE, inputFlashMap.get(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE));
                    }
                } else {
                    model.addAttribute(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE, inputFlashMap.get(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE));
                }
            }
        }

        List<GardenTagRelation> tagRelationsList = gardenTagService.getGardenTagRelationByGarden(garden);

        List<String> tagsList = tagRelationsList.stream()
                .map(GardenTagRelation::getTag)
                .filter(tag -> tag.getTagStatus() == TagStatus.APPROPRIATE)
                .map(GardenTag::getTagName)
                .toList();

        List<String> pendingTags = tagRelationsList.stream()
                .map(GardenTagRelation::getTag)
                .filter(tag -> tag.getTagStatus() == TagStatus.PENDING)
                .map(GardenTag::getTagName)
                .toList();

        model.addAttribute("pendingTags", pendingTags);

        if (garden.getOwner().isBanned()) {
            redirectAttributes.addFlashAttribute(Constants.MESSAGE_ATTRIBUTE,
                    "Your account is blocked for 7 days due to inappropriate conduct");
            redirectAttributes.addFlashAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
            request.logout();
            return "redirect:/login";
        }

        model.addAttribute("tagsList", tagsList);
        return "gardenDetailsPage";
    }

    /**
     * This function creates a post mapping for updating the garden's isPublic
     * boolean.
     *
     * @param gardenId         - the id of the garden being edited
     * @param makeGardenPublic - the new status of the garden isPublic
     * @return thymeleaf garden detail page
     */
    @PostMapping("/my-gardens/{gardenId}/public")
    public String updateGardenPublicStatus(@PathVariable Long gardenId,
            @RequestParam(name = "makeGardenPublic", required = false, defaultValue = "false") boolean makeGardenPublic,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(value = "weatherListJson", required = false) String weatherListJson,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response) throws JsonProcessingException {
        logger.info("POST /my-gardens/{}/public", gardenId);

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

        gardenService.updateGardenPublicity(garden.getGardenId(), makeGardenPublic);
        List<WeatherModel> weatherList = null;
        if (weatherListJson != null) {
            try {
                weatherList = objectMapper.readValue(weatherListJson, new TypeReference<List<WeatherModel>>() {
                });
            } catch (Exception e) {
                logger.error("An error occurred while reading the weatherListJson: {}", e.getMessage());
            }
        }
        redirectAttributes.addFlashAttribute("page", page);
        redirectAttributes.addFlashAttribute("weatherList", weatherList);

        return "redirect:/my-gardens/{gardenId}";

    }

    /**
     * Helper to set the model of garden details page for non-blue sky scenarios
     * after tag post-mapping
     * 
     * @param garden entity of the page to be displayed
     * @param tag    text
     * @param page   index
     * @return filename of thymeleaf template
     */
    private String setGardenDetailModel(Garden garden, String tag, int page, Model model) {
        List<WeatherModel> weatherList;
        weatherList = getGardenWeatherData(garden);
        if (weatherList.size() > 1) {
            handleWeatherMessages(weatherList, garden, model);
        }

        User user = garden.getOwner();
        List<Plant> plants = garden.getPlants();
        String formattedTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a"));
        handlePagniation(page, plants.size(), garden.getPlants(), model);

        model.addAttribute(Constants.OPEN_MODAL_ATTRIBUTE, "true");
        model.addAttribute("tagText", tag);
        model.addAttribute("isOwner", true);
        model.addAttribute("garden", new GardenDetailModel(garden));
        model.addAttribute("weatherList", weatherList);
        model.addAttribute("gradientClass", "g" + LocalTime.now().getHour());
        model.addAttribute("currentTime", formattedTime);
        model.addAttribute(Constants.PROFILE_PICTURE_ATTRIBUTE, user.getProfilePictureFilename());
        model.addAttribute(Constants.USER_NAME_ATTRIBUTE, user.getFirstName() + " " + user.getLastName());

        List<GardenTagRelation> tagRelationsList = gardenTagService.getGardenTagRelationByGarden(garden);

        List<String> tagsList = tagRelationsList.stream()
                .map(GardenTagRelation::getTag)
                .filter(gardenTag -> gardenTag.getTagStatus() == TagStatus.APPROPRIATE)
                .map(GardenTag::getTagName)
                .toList();

        List<String> pendingTags = tagRelationsList.stream()
                .map(GardenTagRelation::getTag)
                .filter(gardenTag -> gardenTag.getTagStatus() == TagStatus.PENDING)
                .map(GardenTag::getTagName)
                .toList();

        model.addAttribute("pendingTags", pendingTags);
        model.addAttribute("tagsList", tagsList);

        return "gardenDetailsPage";
    }

    /**
     * This function creates a post mapping for adding a tag to a garden
     *
     * @param gardenId id of garden to add tag to
     * @param tag      tag string
     * @param page     pagination page
     * @return template for garden page or redirect to garden page
     */
    @PostMapping("/my-gardens/{gardenId}/tag")
    public String addGardenTag(@PathVariable Long gardenId,
            @RequestParam("tag") String tag,
            @RequestParam(defaultValue = "1") int page,
            RedirectAttributes redirectAttributes,
            HttpServletResponse response,
            HttpServletRequest request,
            Model model) throws ServletException {
        logger.info("POST /my-gardens/{}/tag", gardenId);

        tag = tag.trim();

        ValidationResult tagResult = InputValidator.validateTag(tag);
        boolean gardenAlreadyHasThisTag = false;

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);
        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();

        if (tagResult.valid()) {
            GardenTag gardenTag = new GardenTag(tag);

            Optional<GardenTag> gardenTagOptional = gardenTagService.getByName(tag);

            if (gardenTagOptional.isPresent()) {
                gardenTag = gardenTagOptional.get();

            } else {
                gardenTagService.addGardenTag(gardenTag);
                asynchronousTagProfanityCheck(tag, garden.getOwner());
            }

            gardenAlreadyHasThisTag = gardenTagService.getGardenTagRelationByGardenAndTag(garden, gardenTag)
                    .isPresent();

            if (!gardenAlreadyHasThisTag && tagResult.valid() && gardenTag.getTagStatus() != TagStatus.INAPPROPRIATE) {
                gardenTagService.addGardenTagRelation(new GardenTagRelation(garden, gardenTag));
            }

        }

        Optional<GardenTag> newTag = gardenTagService.getByName(tag);

        if (!tagResult.valid() || gardenAlreadyHasThisTag || (newTag.isPresent() &&
                newTag.get().getTagStatus() == TagStatus.INAPPROPRIATE)) {

            if (gardenAlreadyHasThisTag) {
                model.addAttribute(Constants.TAG_ERROR_TEXT_ATTRIBUTE, "This tag has already been added to the garden.");
            } else if (!tagResult.valid()) {
                model.addAttribute(Constants.TAG_ERROR_TEXT_ATTRIBUTE, tagResult);
            }
            if (newTag.isPresent() && newTag.get().getTagStatus() == TagStatus.INAPPROPRIATE) {
                int userStrikes = securityService.handleStrikeUser(garden.getOwner());
                logger.info("{} has received a strike", garden.getOwner().getFirstName());
                logger.info("{} now has {} strikes", garden.getOwner().getFirstName(), garden.getOwner().getStrikes());
                model.addAttribute(Constants.TAG_ERROR_TEXT_ATTRIBUTE, "This tag does not meet the language " +
                        "standards for Gardener's Grove. A warning strike has been added to your account");
                if (userStrikes == 5) {
                    model.addAttribute(Constants.TAG_ERROR_TEXT_ATTRIBUTE, "You have added an inappropriate tag for the fifth time." +
                            " You have been sent a warning email. " +
                            "If you add another inappropriate tag, you will be banned for a week.");
                }

                if (garden.getOwner().isBanned()) {
                    redirectAttributes.addFlashAttribute(Constants.MESSAGE_ATTRIBUTE,
                            "Your account is blocked for 7 days due to inappropriate conduct");
                    redirectAttributes.addFlashAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
                    request.logout();
                    return "redirect:/login";
                }
            }

            List<WeatherModel> weatherList;
            weatherList = getGardenWeatherData(garden);
            if (weatherList.size() > 1) {
                handleWeatherMessages(weatherList, garden, model);
            }

            if (garden.getOwner().isBanned()) {
                redirectAttributes.addFlashAttribute(Constants.MESSAGE_ATTRIBUTE,
                        "Your account is blocked for 7 days due to inappropriate conduct");
                redirectAttributes.addFlashAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
                request.logout();
                return "redirect:/login";
            }
            return setGardenDetailModel(garden, tag, page, model);
        }

        if (newTag.isPresent() && newTag.get().getTagStatus() == TagStatus.PENDING) {
            redirectAttributes.addFlashAttribute(Constants.TAG_MESSAGE_TEXT_ATTRIBUTE,
                    String.format("Your tag \"%s\" is currently being checked for profanity. " +
                            "If it follows the language standards for our app, it will be added to your garden.", tag));
            redirectAttributes.addFlashAttribute(Constants.PENDING_TAG_NAME_ATTRIBUTE, tag);
        }

        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addFlashAttribute(Constants.OPEN_MODAL_ATTRIBUTE, "true");

        if (garden.getOwner().isBanned()) {
            redirectAttributes.addFlashAttribute(Constants.MESSAGE_ATTRIBUTE,
                    "Your account is blocked for 7 days due to inappropriate conduct");
            redirectAttributes.addFlashAttribute(Constants.GOOD_MESSAGE_ATTRIBUTE, false);
            request.logout();
            return "redirect:/login";
        }

        return "redirect:/my-gardens/{gardenId}";

    }

    /**
     * This function is called when a user tries to update a plants image
     * directly from the My Garden's page instead of one of the plant forms.
     *
     * @param gardenId     id of the garden being edited
     * @param plantId      id of the plant being edited
     * @param plantPicture the new picture
     * @param model        the model
     * @return thymeleaf gardenDetails
     */
    @PostMapping("/my-gardens/{gardenId}")
    public String updatePlantImage(@PathVariable Long gardenId,
            @RequestParam("plantId") Long plantId,
            @RequestParam("weatherListJson") String weatherListJson,
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

        List<WeatherModel> weatherList = objectMapper.readValue(weatherListJson,
                new TypeReference<List<WeatherModel>>() {
                });
        redirectAttributes.addFlashAttribute("weatherList", weatherList);
        redirectAttributes.addAttribute("plantToEditId", plantId);
        redirectAttributes.addAttribute("page", page);

        if (plantPictureResult.valid() && !plantPicture.isEmpty()) {
            plantService.updatePlantPicture(plantToUpdate.get(), plantPicture);
            securityService.addUserInteraction(plantId, ItemType.PLANT, LocalDateTime.now());
        } else {
            redirectAttributes.addAttribute("plantPictureError", plantPictureResult.toString());
        }
        return "redirect:/my-gardens/{gardenId}";

    }

    /**
     * This function is called when trying to access another user's gardens.
     *
     * @param model  - the model
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
        List<Garden> gardens = friend.getGardens();
        model.addAttribute("friendName", friendName);
        model.addAttribute("friendGardens", gardens);

        int gardensCount = gardens.size();

        int totalPages = (int) Math.ceil((double) gardens.size() / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, gardens.size());

        model.addAttribute("friendId", userId);
        model.addAttribute("myGardens", gardens.subList(startIndex, endIndex));
        model.addAttribute(Constants.CURRENT_PAGE_ATTRIBUTE, page);
        model.addAttribute(Constants.LAST_PAGE_ATTRIBUTE, totalPages);
        model.addAttribute(Constants.START_INDEX_ATTRIBUTE, startIndex + 1);
        model.addAttribute(Constants.END_INDEX_ATTRIBUTE, endIndex);
        model.addAttribute("totalGardens", gardens.size());
        model.addAttribute("gardensCount", gardensCount);
        model.addAttribute(Constants.USER_NAME_ATTRIBUTE, friend.getFirstName() + " " + friend.getLastName());
        model.addAttribute("firstName", friend.getFirstName());
        model.addAttribute(Constants.PROFILE_PICTURE_ATTRIBUTE, friend.getProfilePictureFilename());

        return "gardensPage";
    }

    /**
     * Retrieves tag suggestions from the Garden Tag Repository through the Garden
     * Tag Service
     *
     * @param query - The search query for tag autocomplete suggestions
     * @return a list of garden tags whose names are similar to the query
     */
    @GetMapping("/tag/suggestions")
    @ResponseBody
    public List<GardenTag> getTagSuggestions(@RequestParam("query") String query) {
        return gardenTagService.getAllSimilar(query);
    }

    private void asynchronousTagProfanityCheck(String tagName, User user) {
        Thread asyncThread = new Thread((() -> {
            boolean tagContainsProfanity = profanityService.containsProfanity(tagName, PriorityType.LOW);
            if (!tagContainsProfanity) {
                gardenTagService.updateGardenTagStatus(tagName, TagStatus.APPROPRIATE);
            } else {
                securityService.handleStrikeUser(user);
                gardenTagService.updateGardenTagStatus(tagName, TagStatus.INAPPROPRIATE);
                gardenTagService.deleteRelationByTagName(tagName);
                logger.info("{} has {} strikes", user.getFirstName(), user.getStrikes());

            }
        }));
        asyncThread.start();

    }

}
