package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.TokenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;

import org.springframework.ui.Model;

/**
 * This is a basic spring boot controller for the login form page, 
 * note the {@link Controller} annotation which defines this.
 * This controller defines endpoints as functions with specific HTTP mappings
 */
@Controller
public class LoginPageController {
    Logger logger = LoggerFactory.getLogger(LoginPageController.class);
    
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    /**
     * Constructor for the LoginPageController with {@link Autowired} to connect this
     * controller with other services
     * 
     * @param userService
     * @param authenticationManager
     */
    @Autowired
    public LoginPageController(UserService userService, AuthenticationManager authenticationManager, TokenService tokenService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    /**
     * Set the security context for the user
     * This method is shared functionality between the login and registration pages
     * possibly should be moved to a different class? As not correct to be here
     * 
     * @param email
     * @param password
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

    /**
     * Gets the login page
     * 
     * @return thymeleaf loginPage
     */
    @GetMapping("/login")
    public String loginPage(Model model, HttpServletRequest request) {
        logger.info("GET /login");
        
        Map<String, ?> inputFlashMap = RequestContextUtils.getInputFlashMap(request);
        if (inputFlashMap != null) {
            model.addAttribute("message", inputFlashMap.get("message"));
        }

        model.addAttribute("validEmail", true);
        model.addAttribute("validLogin", true);

        return "loginPage";
    }

    /**
     * Handles the login request
     * 
     * @param email the email parameter
     * @return thymeleaf loginPage
     */
    @PostMapping("/login")
    public String handleLoginRequest(HttpServletRequest request, @RequestParam String emailAddress,
            @RequestParam String password, Model model) {
        logger.info("POST /login");

        User existingUser = userService.getUserByEmail(emailAddress);

        // For the given email address if there is an existing unverified user whose
        // token has expired, delete them and their associated token
        if (existingUser != null && !existingUser.isVerified()) {

            Token token = tokenService.getTokenByUser(existingUser);

            if (token != null && token.isExpired()) {
                userService.deleteUser(existingUser);
                tokenService.deleteToken(token);
            }
        }

        ValidationResult validEmail = InputValidator.validateEmail(emailAddress);

        if (!validEmail.valid()) {
            model.addAttribute("emailAddressError", validEmail);
            return "loginPage";
        }

        User user = userService.getUserByEmailAndPassword(emailAddress, password);

        if (user == null) {
            model.addAttribute("loginError", "The email address is unknown, or the password is invalid");
            return "loginPage";
        }

        if (!user.isVerified()) {
            return "redirect:/verify/" + user.getEmailAddress();
        }

        setSecurityContext(emailAddress, password, request.getSession());

        return "redirect:/home";

    }

}