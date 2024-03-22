package nz.ac.canterbury.seng302.gardenersgrove.controller;

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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.Validation;
import nz.ac.canterbury.seng302.gardenersgrove.validation.OldValidationResult;

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
    private final Validation validation;

    /**
     * Constructor for the LoginPageController with {@link Autowired} to connect this
     * controller with other services
     * 
     * @param userService
     * @param authenticationManager
     * @param validation
     */
    @Autowired
    public LoginPageController(UserService userService, AuthenticationManager authenticationManager, Validation validation) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.validation = validation;
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
    public String loginPage(Model model) {
        logger.info("GET /login");

        boolean validEmail = true;
        boolean validLogin = true;

        model.addAttribute("validEmail", validEmail);
        model.addAttribute("validLogin", validLogin);

        return "loginPage";
    }

    /**
     * Handles the login request
     * 
     * @param email the email parameter
     * @return thymeleaf loginPage
     */
    @PostMapping("/login")
    public String handleLoginRequest(HttpServletRequest request, @RequestParam String email,
            @RequestParam String password, Model model) {
        logger.info("POST /login");

        OldValidationResult validEmail = validation.validateEmail(email, false);

        boolean validLogin = userService.getUserByEmailAndPassword(email, password) != null;

        if (!validEmail.isValid() || !validLogin) {
            model.addAttribute("emailError", validEmail.getErrorMessage());
            model.addAttribute("loginError", "The email address is unknown, or the password is invalid");

            return "loginPage";
        }
        User user = userService.getUserByEmailAndPassword(email, password);

        if (user == null) {
            model.addAttribute("validEmail", validEmail);
            model.addAttribute("validLogin", false);

            return "loginPage";
        }

        setSecurityContext(email, password, request.getSession());

        return "redirect:/home";

    }

}