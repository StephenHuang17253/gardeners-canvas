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
import nz.ac.canterbury.seng302.gardenersgrove.util.TagStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.*;

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
    private static final int COUNT_PER_PAGE = 9;

    @Autowired
    public PublicGardensController(GardenService gardenService, SecurityService securityService,
            FriendshipService friendshipService, GardenTagService gardenTagService) {
        this.gardenService = gardenService;
        this.securityService = securityService;
        this.friendshipService = friendshipService;
        this.gardenTagService = gardenTagService;
    }

    /**
     * Redirects to pagination page one
     *
     * @return redirect to page/1
     */
    @GetMapping("/public-gardens")
    public String publicGardens(Model model) {
        logger.info("GET /public-gardens");
        return "redirect:/public-gardens/search/1";
    }

    private void handlePagniation(int page, int listLength, List<Plant> plants, Model model) {
        int totalPages = (int) Math.ceil((double) listLength / COUNT_PER_PAGE);
        int startIndex = (page - 1) * COUNT_PER_PAGE;
        int endIndex = Math.min(startIndex + COUNT_PER_PAGE, listLength);

        model.addAttribute("currentPage", page);
        model.addAttribute("lastPage", totalPages);
        model.addAttribute("startIndex", startIndex + 1);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("plants", plants.subList(startIndex, endIndex));
        model.addAttribute("plantCount", plants.size());
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
            @RequestParam(name = "appliedTags", required = false) List<String> appliedTags,
            @PathVariable int pageNumber,
            Model model) {
        logger.info("GET /public-gardens/search");

        List<String> paramList = new ArrayList<>();

        if (!searchInput.isEmpty()) {
            paramList.add("searchInput=" + searchInput);
        }

        if (appliedTags != null && !appliedTags.isEmpty()) {
            for (String tagName : appliedTags) {
                paramList.add("appliedTags=" + tagName);
            }
        }

        String paramString = "";
        if (!paramList.isEmpty()) {
            paramString = "?" + String.join("&", paramList);
        }

        if (pageNumber < 1) {
            return "redirect:/public-gardens/search/1" + paramString;
        }

        model.addAttribute("paramString", paramString);

        List<Garden> matchingGardens;

        if (appliedTags != null && !appliedTags.isEmpty()) {
            matchingGardens = gardenTagService.getMatchingGardens(searchInput, appliedTags);
        } else {
            matchingGardens = gardenService.getMatchingGardens(searchInput);
        }

        int startIndex = 0;
        int endIndex = 0;
        int totalGardens = matchingGardens.size();
        int lastPage = Math.max((int) Math.ceil((double) totalGardens / COUNT_PER_PAGE), 1);

        if (pageNumber > lastPage) {
            return "redirect:/public-gardens/search/" + lastPage + paramString;
        }

        List<Garden> tenSortedPublicGardens = new ArrayList<>();

        if (matchingGardens.isEmpty()) {

            model.addAttribute("searchErrorText", "No gardens match your search");

        } else {

            tenSortedPublicGardens = matchingGardens.stream()
                    .sorted(Comparator.comparing(Garden::getCreationDate).reversed())
                    .skip((long) (pageNumber - 1) * COUNT_PER_PAGE)
                    .limit(COUNT_PER_PAGE)
                    .toList();

            startIndex = (pageNumber - 1) * COUNT_PER_PAGE + 1;
            endIndex = Math.min(startIndex + COUNT_PER_PAGE, totalGardens);
        }

        model.addAttribute("currentPage", pageNumber);
        model.addAttribute("publicGardens", tenSortedPublicGardens);
        model.addAttribute("totalGardens", matchingGardens.size());
        model.addAttribute("startIndex", startIndex);
        model.addAttribute("lastPage", lastPage);
        model.addAttribute("endIndex", endIndex);
        model.addAttribute("searchValue", searchInput);
        model.addAttribute("appliedTags", appliedTags);
        return "browsePublicGardens";
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

        boolean isOwner = (Objects.equals(currentUser.getId(), gardenOwner.getId()));

        if (!garden.getIsPublic()) {
            FriendshipStatus userOwnerRelationship;
            if (Objects.equals(gardenOwner.getId(), currentUser.getId())) {
                userOwnerRelationship = FriendshipStatus.ACCEPTED;
            } else {
                userOwnerRelationship = friendshipService.checkFriendshipStatus(gardenOwner, currentUser);
            }

            if (userOwnerRelationship != FriendshipStatus.ACCEPTED) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                model.addAttribute("message",
                        "This isn't your patch of soil. No peeking at the neighbor's garden without an invite!");
                return "403";
            }

        }

        securityService.addUserInteraction(gardenId, ItemType.GARDEN, LocalDateTime.now());

        handlePagniation(page, garden.getPlants().size(), garden.getPlants(), model);

        model.addAttribute("isOwner", isOwner);
        model.addAttribute("garden", new GardenDetailModel(garden));
        model.addAttribute("weather", null);
        model.addAttribute("profilePicture", gardenOwner.getProfilePictureFilename());
        model.addAttribute("userName", gardenOwner.getFirstName() + " " + gardenOwner.getLastName());

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
     * Checks if a tag exists with the name tagName
     *
     * @param tagName tag name to check
     * @return boolean of if tag exists or not
     */
    @GetMapping("/tag/exists")
    @ResponseBody
    public Boolean checkTagExists(@RequestParam("tagName") String tagName) {
        logger.info("GET tag/exists");
        tagName = tagName.trim();
        Optional<GardenTag> testTag = gardenTagService.getByName(tagName);
        return testTag.isPresent();
    }

}
