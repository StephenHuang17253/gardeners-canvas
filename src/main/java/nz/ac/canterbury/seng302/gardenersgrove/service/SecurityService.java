package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Friendship;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.util.FriendshipStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("securityService")
public class SecurityService {


    private final UserService userService;

    private final FriendshipService friendshipService;

    private final AuthenticationManager authenticationManager;

    Logger logger = LoggerFactory.getLogger(SecurityService.class);

    /**
     * Constructor for the RegistrationFormController with {@link Autowired} to
     * connect this
     * controller with other services
     *
     * @param userService to use for checking persistence to validate email and password
     * @param authenticationManager to login user after registration
     */
    @Autowired
    public SecurityService(UserService userService,
                           AuthenticationManager authenticationManager,
                           FriendshipService friendshipService){

        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.friendshipService = friendshipService;

    }
    /**
     * Checks if the owner id matches the current logged, in user
     *
     * @param ownerId user id of the user entity associated with a given garden entity
     * @return boolean of if the current logged, in user matches the owner id of a garden
     */
    public boolean isOwner(Long ownerId){
        logger.info("Security check");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByEmail(authentication.getName());
        return Objects.equals(user.getId(), ownerId);
    }


    /**
     * Helper to get the current logged, in user
     *
     * @return user entity of logged, in user
     */
    public User getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = this.userService.getUserByEmail(authentication.getName());
        return user;
    }

    /**
     * Helper to check if a user is friends with the current user.
     * @param userId - id of the user being checked.
     * @return the user if they're a friend.
     */
    public User checkFriendship(Long userId, FriendshipStatus expectedStatus) {
        User currentUser = getCurrentUser();
        User targetUser = userService.getUserById(userId);
        FriendshipStatus status = friendshipService.checkFriendshipStatus(currentUser, targetUser);


        if (status.equals(expectedStatus)) {
            return targetUser;
        } else {
            return null;
        }

    }

    public void changeFriendship(Long userId, FriendshipStatus friendshipStatus) {
        User currentUser = getCurrentUser();
        User targetUser = userService.getUserById(userId);
        Friendship friendship = friendshipService.findFriendship(currentUser, targetUser);

        if (friendship != null) {
            friendshipService.updateFriendShipStatus(friendship.getId(), friendshipStatus);
        }

    }

    /**
     * Set the security context for the user
     * This method is shared functionality between the login and registration pages
     * possibly should be moved to a different class? As not correct to be here
     *
     * @param email of user who is registering
     * @param password of user who is registering
     * @param session http session to set the cookies with the context key
     */
    public void setSecurityContext(String email, String password, HttpSession session) {
        User user = userService.getUserByEmailAndPassword(email, password);

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(user.getEmailAddress(),
                password);

        Authentication authentication = authenticationManager.authenticate(token);
        // Check if the authentication is actually authenticated (in this example any
        // username/password is accepted so this should never be false)
        if (authentication.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authentication);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext());
        }
    }

}
