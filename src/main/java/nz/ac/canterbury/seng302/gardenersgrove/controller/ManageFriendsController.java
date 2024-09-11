package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.time.LocalDateTime;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.RequestFriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserInteractionService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import nz.ac.canterbury.seng302.gardenersgrove.util.ItemType;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;

/**
 * Controller for the Manage Friends page
 */
@Controller
@SessionAttributes("userGardens")
public class ManageFriendsController {

    Logger logger = LoggerFactory.getLogger(ManageFriendsController.class);

    private final FriendshipService friendshipService;
    private final SecurityService securityService;
    private final UserService userService;

    private final UserInteractionService userInteractionService;

    /**
     * Constructor for the ManageFriendsController with {@link Autowired} to
     * connect this controller with other services
     *
     * @param securityService        service to access security methods
     * @param friendshipService      service to access plant repository
     * @param userService            service to manage users
     * @param userInteractionService service to manage user interactions
     */
    @Autowired
    public ManageFriendsController(FriendshipService friendshipService, SecurityService securityService,
            UserService userService, UserInteractionService userInteractionService) {
        this.friendshipService = friendshipService;
        this.securityService = securityService;
        this.userService = userService;
        this.userInteractionService = userInteractionService;
    }

    /**
     * Helper function to create a list of friend models. Used for adding to the
     * model of the Manage Friends page.
     *
     * @return friendModels
     */
    private List<FriendModel> createFriendModel() {
        User currentUser = securityService.getCurrentUser();
        List<FriendModel> friendModels = new ArrayList<>();
        List<Friendship> friendships = friendshipService.getAllUsersFriends(currentUser.getId());
        List<User> userTypeFriends = new ArrayList<>();

        for (Friendship friendship : friendships) {
            if (friendship.getStatus().equals(FriendshipStatus.ACCEPTED)) {
                if (friendship.getUser1().getId().equals(currentUser.getId())) {
                    userTypeFriends.add(friendship.getUser2());
                } else {
                    userTypeFriends.add(friendship.getUser1());
                }
            }
        }
        for (User userTypeFriend : userTypeFriends) {
            String friendProfilePicture = userTypeFriend.getProfilePictureFilename();
            String fName = userTypeFriend.getFirstName();
            String lName = userTypeFriend.getLastName();
            String friendsName = fName + ' ' + lName;
            String friendGardenLink = "/" + userTypeFriend.getId() + "/gardens";
            FriendModel friendModel = new FriendModel(friendProfilePicture, friendsName, friendGardenLink);
            friendModel.setFriendId(userTypeFriend.getId());
            friendModels.add(friendModel);
        }

        return friendModels;
    }

    /**
     * Helper function for creating a list of request (pending or declined)
     * models. Used for adding friend requests to the model of the Manage
     * Friends page.
     *
     * @param friendshipStatus pending or declined
     * @return requestFriendModels
     */
    private List<RequestFriendModel> createRequestFriendModel(FriendshipStatus friendshipStatus) {
        User currentUser = securityService.getCurrentUser();
        List<RequestFriendModel> requestFriendModels = new ArrayList<>();
        List<Friendship> targetFriendships = new ArrayList<>();
        List<Friendship> friendships = friendshipService.getAllUsersFriends(currentUser.getId());
        List<User> userTypeFriends = new ArrayList<>();

        for (Friendship friendship : friendships) {
            if (friendship.getStatus().equals(friendshipStatus)) {
                userTypeFriends.add(friendship.getUser2());
                targetFriendships.add(friendship);
            }
        }

        for (Friendship friendship : targetFriendships) {

            User userTypeFriend;
            boolean isSender = false;
            if (friendship.getUser1().getId().equals(currentUser.getId())) {
                userTypeFriend = friendship.getUser2();
                isSender = true;
            } else {
                userTypeFriend = friendship.getUser1();
            }

            String friendProfilePicture = userTypeFriend.getProfilePictureFilename();
            String fName = userTypeFriend.getFirstName();
            String lName = userTypeFriend.getLastName();
            String friendsName = fName + ' ' + lName;
            RequestFriendModel requestFriendModel = new RequestFriendModel(friendProfilePicture, friendsName, isSender,
                    userTypeFriend.getId());
            requestFriendModels.add(requestFriendModel);
        }

        return requestFriendModels;
    }

    /**
     * Maps the manageFriendsPage html file to /manage-friends url
     *
     * @return thymeleaf manageFriendsPage
     */
    @GetMapping("/manage-friends")
    public String myFriends(@RequestParam(defaultValue = "friends") String activeTab, Model model, HttpServletRequest request) {
        logger.info("GET /manage-friends");

        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null && inputFlashMap.containsKey("activeTab")) {
            activeTab = (String) inputFlashMap.get("activeTab");
        }
        if (inputFlashMap != null) {
            model.addAttribute("errorMessage", inputFlashMap.get("errorMessage"));
            model.addAttribute("goodMessage", false);
        }

        List<FriendModel> acceptedFriendModels = createFriendModel();
        List<RequestFriendModel> pendingFriendModels = createRequestFriendModel(FriendshipStatus.PENDING);
        List<RequestFriendModel> declinedFriendModels = createRequestFriendModel(FriendshipStatus.DECLINED);
        model.addAttribute("userFriends", acceptedFriendModels);
        model.addAttribute("pendingFriends", pendingFriendModels);
        model.addAttribute("declinedFriends", declinedFriendModels);
        model.addAttribute("activeTab", activeTab);

        return "manageFriendsPage";
    }

    /**
     * Get all people in database matching input string
     *
     * @param searchInput input name or email from user
     * @param validEmail  Validation result of if searchInput is an email
     * @return list of results in FriendModels
     */
    List<FriendModel> getSearchResults(String searchInput, ValidationResult validEmail) {
        List<FriendModel> friendModels = new ArrayList<>();
        // get all matching search results
        User[] searchResults = userService.getMatchingUsers(searchInput, validEmail);
        User currentUser = securityService.getCurrentUser();
        if (searchResults.length < 1) {
            return Collections.emptyList();
        }
        for (User foundUser : searchResults) {
            // ensure that a search result is not the current user
            if (!Objects.equals(foundUser.getId(), currentUser.getId())) {
                // creating a friend (person) object for display on frontend
                String friendProfilePicture = foundUser.getProfilePictureFilename();
                String friendsName = foundUser.getFirstName() + " " + foundUser.getLastName();
                String friendGardenLink = "/" + foundUser.getId() + "/gardens";
                FriendModel friendModel = new FriendModel(friendProfilePicture, friendsName, friendGardenLink);
                friendModel.setFriendId(foundUser.getId());
                // add status of friendship from current user to foundUser
                friendModel.setFriendRequestStatus(friendshipService.checkFriendshipStatus(currentUser, foundUser));
                friendModel.setFriendId(foundUser.getId());
                // ensure there is no friendship from foundUser to currentUser
                boolean existsFriendship = friendshipService.checkFriendshipExists(foundUser, currentUser);

                if (!existsFriendship) {
                    friendModels.add(friendModel);
                } else {
                    Friendship friendship = friendshipService.findFriendship(foundUser, currentUser);
                    boolean declinedRequest = (friendship.getStatus() == FriendshipStatus.DECLINED
                            && friendship.getUser1() == foundUser);
                    if (declinedRequest) {
                        friendModels.add(friendModel);
                    }
                }
            }
        }
        return friendModels;
    }

    /**
     * Gets results of search for other users
     *
     * @return thymeleaf manageFriendsPage
     */
    @GetMapping("/manage-friends/search")
    public String searchForUsers(@RequestParam(name = "searchInput", defaultValue = "") String searchInput,
            Model model) {
        logger.info("GET /manage-friends/search");

        // preparing input for validation
        searchInput = searchInput.strip();
        ValidationResult validEmail = InputValidator.validateEmail(searchInput);
        String[] separated = searchInput.split(" ");

        // validating input
        ValidationResult validFName = InputValidator.validateName(separated[0]);
        ValidationResult validLName = InputValidator.validateName((separated.length > 1) ? separated[1] : "");
        List<FriendModel> friendModels = new ArrayList<>();
        // getting results
        if ((validEmail.valid() || validFName.valid() || validLName.valid()) && !searchInput.isEmpty()) {
            friendModels = getSearchResults(searchInput, validEmail);
            model.addAttribute("searchResults", friendModels);
        }
        if (friendModels == null || friendModels.isEmpty()) {
            model.addAttribute("searchErrorText", "There is nobody with that name or email in Gardener's Grove");
            model.addAttribute("userSearch", searchInput);

        }
        List<FriendModel> acceptedFriendModels = createFriendModel();
        List<RequestFriendModel> pendingFriendModels = createRequestFriendModel(FriendshipStatus.PENDING);
        List<RequestFriendModel> declinedFriendModels = createRequestFriendModel(FriendshipStatus.DECLINED);
        model.addAttribute("userFriends", acceptedFriendModels);
        model.addAttribute("pendingFriends", pendingFriendModels);
        model.addAttribute("declinedFriends", declinedFriendModels);
        model.addAttribute("isPotentialFriend", true);
        model.addAttribute("userSearch", searchInput);
        model.addAttribute("activeTab", "search");
        return "manageFriendsPage";
    }

    /**
     * Creates a new friendship with pending status
     *
     * @return thymeleaf manageFriendsPage
     */
    @PostMapping("/manage-friends/send-invite")
    public String createFriendship(@RequestParam("friendId") Long friendId,
            @RequestParam("activeTab") String activeTab,
            @RequestParam(value = "searchInput", required = false) String searchInput,
            RedirectAttributes redirectAttributes,
            Model model) {
        logger.info("POST /manage-friends/send-invite");

        User currentUser = securityService.getCurrentUser();
        User potentialFriend = userService.getUserById(friendId);
        try {
            friendshipService.addFriendship(currentUser, potentialFriend);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("searchErrorText", exception.getMessage());
            return "manageFriendsPage";
        }

        redirectAttributes.addFlashAttribute("activeTab", activeTab);

        if (searchInput != null) {
            return "redirect:/manage-friends/search?searchInput=" + searchInput;
        }

        return "redirect:/manage-friends";
    }

    /**
     * Changes the status of a pending request, based on whether it was accepted
     * or declined, if it is accepted a user interaction is logged for both users
     * showing that they recently interacted
     *
     * @param friendAccepted  boolean, whether the friend request was accepted or
     *                        declined
     * @param pendingFriendId the id of the user who sent the request
     * @param activeTab       the tab which the user is on ( #pending for this
     *                        endpoint )
     * @return thymeleaf manageFriendsPage
     */
    @PostMapping("/manage-friends")
    public String managePendingRequest(@RequestParam(name = "friendAccepted") boolean friendAccepted,
            @RequestParam(name = "pendingFriendId") Long pendingFriendId,
            @RequestParam("activeTab") String activeTab,
            RedirectAttributes redirectAttributes) {
        logger.info("POST /manage-friends");

        User currentUser = securityService.getCurrentUser();

        if (friendAccepted) {
            securityService.changeFriendship(pendingFriendId, FriendshipStatus.ACCEPTED);
            userInteractionService.addUserInteraction(currentUser.getId(), pendingFriendId, ItemType.USER,
                    LocalDateTime.now());
            userInteractionService.addUserInteraction(pendingFriendId, currentUser.getId(), ItemType.USER,
                    LocalDateTime.now());
        } else {
            securityService.changeFriendship(pendingFriendId, FriendshipStatus.DECLINED);
        }

        redirectAttributes.addFlashAttribute("activeTab", activeTab);

        return "redirect:/manage-friends";
    }

    /**
     * This function removes a friendship entity. It is called when the user
     * cancels a pending friend request, or when they remove a friend from their
     * friends list.
     *
     * @param friendId  the user they sent the request to, or removed from
     *                  friends
     * @param activeTab the tab which the user is on ( #pending for this
     *                  endpoint )
     * @return thymeleaf manageFriendsPage
     */
    @PostMapping("/manage-friends/remove")
    public String cancelSentRequest(@RequestParam(name = "friendId") Long friendId,
            @RequestParam("activeTab") String activeTab,
            RedirectAttributes redirectAttributes) {
        logger.info("POST /manage-friends/remove");

        User currentUser = securityService.getCurrentUser();
        User friend = userService.getUserById(friendId);
        Friendship friendship = friendshipService.findFriendship(currentUser, friend);

        if (friendship != null) {
            if (!Objects.equals(friendship.getStatus(), FriendshipStatus.PENDING)) {
                redirectAttributes.addFlashAttribute("errorMessage", "This request has already been declined");
            }
            friendshipService.deleteFriendship(friendship.getId());
            userInteractionService.removeUserInteraction(currentUser.getId(), friendId, ItemType.USER);
            userInteractionService.removeUserInteraction(friendId, currentUser.getId(), ItemType.USER);
        }

        redirectAttributes.addFlashAttribute("activeTab", activeTab);

        return "redirect:/manage-friends";
    }

}
