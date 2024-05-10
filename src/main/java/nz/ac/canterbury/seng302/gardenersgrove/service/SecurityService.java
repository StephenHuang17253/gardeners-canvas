package nz.ac.canterbury.seng302.gardenersgrove.service;

import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Garden;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
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

@Service("securityService")
public class SecurityService {


    private final UserService userService;

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
    public SecurityService(UserService userService, AuthenticationManager authenticationManager){
        this.userService = userService;
        this.authenticationManager = authenticationManager;

    }
    /**
     * Cheeks if the owner id matches the current logged, in user
     *
     * @param ownerId user id of the user entity associated with a given garden entity
     * @return boolean of if the current logged, in user matches the owner id of a garden
     */
    public boolean isOwner(Long ownerId){
        logger.info("Security check");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.getUserByEmail(authentication.getName());
        return user.getId() == ownerId;
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
