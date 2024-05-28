package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.*;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller for the Manage Friends page
 */
@Controller
@SessionAttributes("userGardens")
public class ManageFriendsController {

    Logger logger = LoggerFactory.getLogger(GardensController.class);

    private final FriendshipService friendshipService;

    private final SecurityService securityService;

    private final FileService fileService;

    private final UserService userService;

    /**
     * Constructor for the ManageFriendsController with {@link Autowired} to
     * connect this controller with other services
     *
     * @param securityService   service to access security methods
     * @param friendshipService service to access plant repository
     * @param fileService       service to manage files
     */
    @Autowired
    public ManageFriendsController(FriendshipService friendshipService, SecurityService securityService, FileService fileService, UserService userService) {
        this.friendshipService = friendshipService;
        this.fileService = fileService;
        this.securityService = securityService;
        this.userService = userService;

    }

    /**
     * Helper function to create a list of friend models.
     * Used for adding to the model of the Manage Friends page.
     *
     * @return friendModels
     */
    private List<FriendModel> createFriendModel() {
        User currentUser = securityService.getCurrentUser();
        List<FriendModel> friendModels = new ArrayList<>();
        List<Friendship> friendships = friendshipService.getAllUsersFriends(currentUser.getId());
        List<User> userTypeFriends = new ArrayList<>();

        for (Friendship friendship : friendships) {
            if (Objects.equals(friendship.getUser1().getId(), currentUser.getId())) {
                userTypeFriends.add(friendship.getUser2());
            } else {
                userTypeFriends.add(friendship.getUser1());
            }
        }
        for (User userTypeFriend : userTypeFriends) {
            String friendProfilePicture = userTypeFriend.getProfilePictureFilename();
            String fName = userTypeFriend.getFirstName();
            String lName = userTypeFriend.getLastName();
            String friendsName = fName + ' ' + lName;
            String friendGardenLink = "/" + userTypeFriend.getId() + "/gardens";
            FriendModel friendModel = new FriendModel(friendProfilePicture, friendsName, friendGardenLink);
            friendModels.add(friendModel);
        }

        return friendModels;
    }

    /**
     * Maps the manageFriendsPage html file to /manage-friends url
     *
     * @return thymeleaf manageFriendsPage
     */
    @GetMapping("/manage-friends")
    public String myFriends(Model model) {
        logger.info("GET /manage-friends");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

        List<FriendModel> friendModels = createFriendModel();
        model.addAttribute("userFriends", friendModels);
        model.addAttribute("SearchErrorText", "");


        return "manageFriendsPage";
    }

    /**
     * Gets results of search for other users
     *
     * @return thymeleaf manageFriendsPage
     */
    @GetMapping("/manage-friends/search")
    public String searchForUsers(@RequestParam(name = "searchInput", defaultValue = "") String searchInput, Model model) {
        logger.info("GET /manage-friends/search");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);
        //preparing input for validation
        searchInput = searchInput.strip();
        ValidationResult validEmail = InputValidator.validateEmail(searchInput);
        String[] seperated = searchInput.split(" ");

        //validating input
        ValidationResult validFName = InputValidator.validateName(seperated[0]);
        ValidationResult validLName = InputValidator.validateName((seperated.length > 1) ? seperated[1] : "");
        List<FriendModel> friendModels = new ArrayList<>();
        //getting results
        if ((validEmail.valid() || validFName.valid() || validLName.valid()) && !searchInput.isEmpty()) {
            User[] searchResults = userService.getMatchingUsers(searchInput, validEmail);
            User currentUser = userService.getUserByEmail(Objects.requireNonNull(authentication).getName());
            if (searchResults.length >= 1) {
                for (User user : searchResults) {
                    if (!Objects.equals(user.getId(), currentUser.getId())) {
                        String friendProfilePicture = user.getProfilePictureFilename();
                        String friendsName = user.getFirstName() + " " + user.getLastName();
                        String friendGardenLink = "/" + user.getId() + "/gardens";
                        FriendModel friendModel = new FriendModel(friendProfilePicture, friendsName, friendGardenLink);
                        // add status of friendship from current user to other user
                        friendModel.setFriendRequestStatus(friendshipService.checkFriendshipStatus(currentUser, user));
                        friendModel.setFriendId(user.getId());
                        // ensure there is not friendship from other user to this user
                        if (!friendshipService.checkFriendshipExists(user, currentUser)) {
                            friendModels.add(friendModel);
                        }

                    }
                }
            }
            model.addAttribute("userFriends", friendModels);
        }
        if (friendModels.isEmpty()) {
            model.addAttribute("SearchErrorText", "There is nobody with that name or email in Gardener's Grove");
        }
        model.addAttribute("isPotentialFriend", true);
        return "manageFriendsPage";
    }

    /**
     * Creates a new friendship with pending status
     *
     * @return thymeleaf manageFriendsPage
     */
    @PostMapping("/manage-friends/send-invite")
    public String createFriendship(@RequestParam("friendId") Long friendId, Model model) {
        logger.info("GET /manage-friends/send-invite");
        model.addAttribute("searchInput", "");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && !Objects.equals(authentication.getName(), "anonymousUser");
        model.addAttribute("loggedIn", loggedIn);

        User currentUser = userService.getUserByEmail(Objects.requireNonNull(authentication).getName());
        User potentialFriend = userService.getUserById(friendId);
        try {
            friendshipService.addFriendship(currentUser, potentialFriend);
        } catch (IllegalArgumentException exception) {
            model.addAttribute("SearchErrorText", exception.getMessage());
            return "manageFriendsPage";
        }

        return "redirect:/manage-friends";
    }


}
