package nz.ac.canterbury.seng302.gardenersgrove.controller;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;

@Controller
public class EmailController {
    Logger logger = LoggerFactory.getLogger(EmailController.class);

    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public EmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    @GetMapping("/email")
    public String sendEmail() throws MessagingException {
        logger.info("GET /email");
        return "emailPage";
    }

    @PostMapping("/email")
    public String sendEmailPost(@RequestParam(name = "firstName") String firstName,
            @RequestParam(name = "lastName") String lastName, @RequestParam(name = "emailAddress") String emailAddress,
            @RequestParam(name = "lifetime") int lifetime, Model model) {
        logger.info("POST /email");

        // None saved in persistence

        User emailUser = new User(firstName, lastName, emailAddress, null);

        Duration lifetimeDuration = Duration.ofMinutes(lifetime);

        Token token = new Token(emailUser, lifetimeDuration);

        String message = "Email sent to " + emailAddress;

        try {
            emailService.sendRegistrationEmail(token);
        } catch (MessagingException e) {
            message = "Failed to send email to" + emailAddress;
        }

        model.addAttribute("message", message);

        return "emailPage";
    }
}
