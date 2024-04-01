package nz.ac.canterbury.seng302.gardenersgrove.controller;

import nz.ac.canterbury.seng302.gardenersgrove.service.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
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
    private final SecurityService securityService;

    /**
     * Constructor for the LoginPageController with {@link Autowired} to connect this
     * controller with other services
     * 
     * @param userService to use for checking persistence to validate email and password
     * @param securityService to login user after registration
     */
    @Autowired
    public LoginPageController(UserService userService, SecurityService securityService) {
        this.userService = userService;
        this.securityService = securityService;
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

        ValidationResult validEmail = InputValidator.validateEmail(email);

        boolean validLogin = userService.getUserByEmailAndPassword(email, password) != null;

        if (!validEmail.valid() || !validLogin) {
            model.addAttribute("emailError", validEmail);
            model.addAttribute("loginError", "The email address is unknown, or the password is invalid");

            return "loginPage";
        }
        User user = userService.getUserByEmailAndPassword(email, password);

        if (user == null) {
            model.addAttribute("validEmail", validEmail);
            model.addAttribute("validLogin", false);

            return "loginPage";
        }

        securityService.setSecurityContext(email, password, request.getSession());

        return "redirect:/home";

    }

}