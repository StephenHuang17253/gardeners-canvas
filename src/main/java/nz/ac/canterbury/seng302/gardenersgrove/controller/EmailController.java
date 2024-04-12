package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.TokenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

@Controller
public class EmailController {
    Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final UserService userService;
    private final TokenService tokenService;

    @Autowired
    public EmailController(EmailService emailService, UserService userService, TokenService tokenService) {
        this.emailService = emailService;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @GetMapping("/email")
    public String sendEmail() {
        logger.info("GET /email");
        return "emailPage";
    }

    @PostMapping("/email")
    public String sendEmailPost(@RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName") String lastName, @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam(name = "lifetime") int lifetime, Model model) {
        logger.info("POST /email");

        User user = new User(firstName, lastName, emailAddress, null);

        userService.addUser(user, "password");

        Duration lifetimeDuration = Duration.ofMinutes(lifetime);

        Token token = new Token(user, lifetimeDuration);

        tokenService.addToken(token);

        try {
            emailService.sendRegistrationEmail(token);
        } catch (MessagingException e) {
            String message = "Failed to send email to" + emailAddress;
            model.addAttribute("message", message);
            return "emailPage";
        }

        return "redirect:/verify/" + emailAddress;
    }

    @GetMapping("/verify/{emailAddress}")
    public String verifyEmail(@PathVariable String emailAddress, Model model) {
        logger.info("GET /verify");

        model.addAttribute("emailAddress", emailAddress);

        return "verificationPage";
    }

    @PostMapping("/verify")
    public String verifyEmailPost(@RequestParam(name = "tokenString") String tokenString,
            @RequestParam(name = "emailAddress") String emailAddress, Model model) {
        logger.info("POST /verify");

        Token token = tokenService.getTokenByTokenString(tokenString);

        if (token == null) {
            model.addAttribute("message", "Invalid token");
            model.addAttribute("emailAddress", emailAddress);
            return "verificationPage";
        }

        if (!token.getUser().getEmailAddress().equals(emailAddress)) {
            model.addAttribute("message", "Incorrect token");
            model.addAttribute("emailAddress", emailAddress);
            return "verificationPage";
        }

        User user = token.getUser();

        userService.verifyUser(user);

        model.addAttribute("message", "User " + user.getEmailAddress() + " has been verified");

        return "verificationPage";
    }
}
