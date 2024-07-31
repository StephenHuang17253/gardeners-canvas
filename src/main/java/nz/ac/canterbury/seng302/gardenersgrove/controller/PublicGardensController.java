package nz.ac.canterbury.seng302.gardenersgrove.controller;

import jakarta.servlet.http.HttpServletResponse;
import nz.ac.canterbury.seng302.gardenersgrove.model.GardenDetailModel;
import nz.ac.canterbury.seng302.gardenersgrove.entity.*;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.GardenTagService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for viewing all public gardens
 */
@Controller
public class PublicGardensController {

    Logger logger = LoggerFactory.getLogger(PublicGardensController.class);

    private final GardenService gardenService;
    private final SecurityService securityService;
    private final GardenTagService gardenTagService;
    private final FriendshipService friendshipService;
    private static final int COUNT_PER_PAGE = 10;

    @Autowired
    public PublicGardensController(GardenService gardenService, SecurityService securityService,
            FriendshipService friendshipService, GardenTagService gardenTagService) {
        this.gardenService = gardenService;
        this.securityService = securityService;
        this.friendshipService = friendshipService;
        this.gardenTagService = gardenTagService;
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
     * Redirects to pagination page one
     *
     * @return redirect to page/1
     */
    @GetMapping("/public-gardens")
    public String publicGardens(Model model) {
        logger.info("GET /public-gardens");
        return "redirect:/public-gardens/page/1";
    }

    /**
     * returns a page with the 10 most recent public gardens based on current page
     * in pagination
     * Page number index starts at 1, so page 1 gets gardens 1-10 latest gardens,
     * page 2 gets 11-20 and so on
     *
     * @return thymeleaf BrowsePublicGardens html element
     */
    @GetMapping("/public-gardens/page/{pageNumber}")
    public String publicGardensPagination(
            @PathVariable Long pageNumber,
            Model model) {
        logger.info("GET /public-gardens");

        List<Garden> allGardens = gardenService.getAllPublicGardens();
        int totalGardens = allGardens.size();
        int pageSize = 10;
        int startIndex = Math.toIntExact((pageNumber - 1) * pageSize);
        int endIndex = Math.min(startIndex + pageSize, totalGardens);
        int lastPage = (int) Math.ceil((double) totalGardens / pageSize);

        if (lastPage == 0) {
            return "redirect:/home";
        }

        if (pageNumber > lastPage) {
            return "redirect:/public-gardens/page/" + lastPage;
        }

        if (pageNumber < 1) {
            return "redirect:/public-gardens/page/1";
        }

        List<Garden> tenSortedPublicGardens = allGardens.stream()
                .sorted(Comparator.comparing(Garden::getCreationDate).reversed())
                .skip((pageNumber - 1) * pageSize)
                .limit(pageSize)
                .collect(Collectors.toList());

        model.addAttribute("publicGardens", tenSortedPublicGardens);
        model.addAttribute("currentPage", pageNumber + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("SearchErrorText", "");
        model.addAttribute("searchValue", "");

        return "browsePublicGardens";
    }

    /**
     * returns a page with the 10 most recent public gardens based on search and on
     * current page in pagination
     * Page number index starts at 1, so page 1 gets gardens 1-10 latest gardens,
     * page 2 gets 11-20 and so on
     *
     * @return thymeleaf BrowsePublicGardens html element
     */
    @GetMapping("/public-gardens/search/{pageNumber}")
    public String publicGardens(
            @RequestParam(name = "searchInput", defaultValue = "", required = false) String searchInput,
            @RequestParam(name = "appliedSearchTagsList", required = false) List<String> appliedSearchTagsList,
            @PathVariable Long pageNumber,
            Model model) {
        logger.info("GET /public-gardens/search");

        if (appliedSearchTagsList != null) {
            for (String tag : appliedSearchTagsList) {
                logger.info(tag);
            }
        }

        if (Objects.equals(searchInput, "")) {
            return "redirect:/public-gardens/page/1";
        }

        List<Garden> matchingGardens = gardenService.getMatchingGardens(searchInput);

        if (!matchingGardens.isEmpty()) {
            int totalGardens = matchingGardens.size();
            int pageSize = 10;
            int startIndex = Math.toIntExact((pageNumber - 1) * pageSize);
            int endIndex = Math.min(startIndex + pageSize, totalGardens);
            int lastPage = (int) Math.ceil((double) totalGardens / pageSize);

            if (pageNumber > lastPage) {
                return "redirect:/public-gardens/search/" + lastPage;
            }
            if (pageNumber < 1) {
                return "redirect:/public-gardens/search/1";
            }

            List<Garden> tenSortedPublicGardens = matchingGardens.stream()
                    .sorted(Comparator.comparing(Garden::getCreationDate).reversed())
                    .skip((pageNumber - 1) * pageSize)
                    .limit(pageSize)
                    .collect(Collectors.toList());

            model.addAttribute("publicGardens", tenSortedPublicGardens);
            model.addAttribute("currentPage", pageNumber);
            model.addAttribute("totalGardens", totalGardens);
            model.addAttribute("startIndex", startIndex + 1);
            model.addAttribute("endIndex", endIndex);
            model.addAttribute("lastPage", lastPage);

        } else {
            model = resetModel(model);
            model.addAttribute("SearchErrorText", "No gardens match your search");
        }

        model.addAttribute("searchValue", searchInput);
        model.addAttribute("appliedSearchTagsList", appliedSearchTagsList);
        return "browsePublicGardens";
    }

    /**
     * Resets model send to Browse gardens page
     *
     * @param model model to reset
     * @return model with default values
     */
    Model resetModel(Model model) {
        List<Garden> emptyGardensList = new ArrayList<>();
        model.addAttribute("publicGardens", emptyGardensList);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalGardens", 0);
        model.addAttribute("startIndex", 0);
        model.addAttribute("endIndex", 0);
        model.addAttribute("lastPage", 1);
        model.addAttribute("searchValue", "");
        return model;
    }

    /**
     * Get Mapping of the /my-gardens/{gardenId} endpoint
     * Garden Details page of all the plants belonging to the garden
     *
     * @param gardenId id of the garden used in the end-point path
     * @return thymeleaf createNewGardenForm
     */
    @GetMapping("public-gardens/{gardenId}")
    public String viewPublicGarden(@PathVariable Long gardenId,
            @RequestParam(defaultValue = "1") int page,
            HttpServletResponse response,
            Model model) {
        logger.info("GET public-gardens/{}", gardenId);

        Optional<Garden> optionalGarden = gardenService.getGardenById(gardenId);

        if (optionalGarden.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return "404";
        }

        Garden garden = optionalGarden.get();

        User currentUser = securityService.getCurrentUser();
        User gardenOwner = garden.getOwner();

        FriendshipStatus userOwnerRelationship = friendshipService.checkFriendshipStatus(gardenOwner, currentUser);

        if (!garden.getIsPublic() && userOwnerRelationship != FriendshipStatus.ACCEPTED) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            model.addAttribute("message",
                    "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
            return "403";

        }

        securityService.addUserInteraction(gardenId, ItemType.GARDEN, LocalDateTime.now());

        User user = garden.getOwner();
        List<Plant> plants = garden.getPlants();
        int totalPages = (int) Math.ceil((double) plants.size() / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, plants.size());

        model.addAttribute("isOwner", false);
        model.addAttribute("garden", new GardenDetailModel(garden));
        model.addAttribute("weather", null);
        model.addAttribute("profilePicture", user.getProfilePictureFilename());
        model.addAttribute("userName", user.getFirstName() + " " + user.getLastName());

        model.addAttribute("currentPage", page);
        model.addAttribute("lastPage", totalPages);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);

        List<GardenTagRelation> tagRelationsList = gardenTagService.getGardenTagRelationByGarden(garden);

        List<String> tagsList = tagRelationsList.stream()
                .map(GardenTagRelation::getTag)
                .map(GardenTag::getTagName)
                .toList();

        model.addAttribute("tagsList", tagsList);

        return "gardenDetailsPage";

    }

    @GetMapping("/tag/exists")
    @ResponseBody
    public Boolean checkTagExists(@RequestParam("tagName") String tagName) {
       Optional<GardenTag> testTag = gardenTagService.getByName(tagName);
       return testTag.isPresent();
    }

}
