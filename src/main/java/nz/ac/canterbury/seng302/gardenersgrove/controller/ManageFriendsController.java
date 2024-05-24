package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.model.FriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.model.PendingFriendModel;
import nz.ac.canterbury.seng302.gardenersgrove.service.FileService;
import nz.ac.canterbury.seng302.gardenersgrove.service.FriendshipService;
import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
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

    /**
     * Constructor for the ManageFriendsController with {@link Autowired} to
     * connect this controller with other services
     *
     * @param securityService service to access security methods
     * @param friendshipService service to access plant repository
     * @param fileService service to manage files
     */
    @Autowired
    public ManageFriendsController(FriendshipService friendshipService, SecurityService securityService, FileService fileService) {
        this.friendshipService = friendshipService;
        this.fileService = fileService;
        this.securityService = securityService;

    }

    /**
     * Helper function to create a list of friend models.
     * Used for adding to the model of the Manage Friends page.
     * @return friendModels
     */
    private List<FriendModel> createFriendModel() {
        User currentUser = securityService.getCurrentUser();
        List<FriendModel> friendModels = new ArrayList<>();
        List<Friendship> friendships = friendshipService.getAllUsersFriends(currentUser.getId());
        List<User> userTypeFriends = new ArrayList<>();

        for (Friendship friendship : friendships) {
            if(friendship.getStatus().equals(FriendshipStatus.ACCEPTED)){
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
            friendModels.add(friendModel);
        }

        return friendModels;
    }

    private List<PendingFriendModel> createPendingFriendModel() {
        User currentUser = securityService.getCurrentUser();
        List<PendingFriendModel> pendingFriendModels = new ArrayList<>();
        List<Friendship> targetFriendships = new ArrayList<>();
        List<Friendship> friendships = friendshipService.getAllUsersFriends(currentUser.getId());
        List<User> userTypeFriends = new ArrayList<>();

        for (Friendship friendship : friendships) {
            if (friendship.getStatus().equals(FriendshipStatus.PENDING)) {
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
            PendingFriendModel pendingFriendModel = new PendingFriendModel(friendProfilePicture, friendsName, isSender, userTypeFriend.getId());
            pendingFriendModels.add(pendingFriendModel);
        }

        return pendingFriendModels;
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
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        List<FriendModel> friendModels = createFriendModel();
        List<PendingFriendModel> pendingFriendModels = createPendingFriendModel();
        model.addAttribute("userFriends", friendModels);
        model.addAttribute("pendingFriends", pendingFriendModels);



        return "manageFriendsPage";
    }
    @PostMapping("/manage-friends")
    public String acceptedFriendRequest(@RequestParam(name = "acceptedFriend", required = true) boolean acceptedFriend,
                                        @RequestParam(name = "pendingFriend", required = true) PendingFriendModel pendingFriendModel,
                               Model model) {
        logger.info("POST /manage-friends");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean loggedIn = authentication != null && authentication.getName() != "anonymousUser";
        model.addAttribute("loggedIn", loggedIn);

        Long userId = pendingFriendModel.getUserId();
        securityService.changeFriendship(userId, FriendshipStatus.ACCEPTED);

        return "manageFriendsPage";
    }
}
