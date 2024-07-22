package nz.ac.canterbury.seng302.gardenersgrove.controller;


import jakarta.mail.MessagingException;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import nz.ac.canterbury.seng302.gardenersgrove.service.TokenService;
import nz.ac.canterbury.seng302.gardenersgrove.service.UserService;
import nz.ac.canterbury.seng302.gardenersgrove.validation.ValidationResult;
import nz.ac.canterbury.seng302.gardenersgrove.validation.inputValidation.InputValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Controller for reset password and lost password forms.
 * Handles resetting password and sending all relevant emails for it.
 */
@Controller
public class ResetPasswordController {
    private static final Logger logger = LoggerFactory.getLogger(ResetPasswordController.class);
    private final UserService userService;
    private EmailService emailService;
    private TokenService tokenService;

    @Autowired
    public ResetPasswordController(UserService userService, TokenService tokenService, EmailService emailService) {
        this.userService = userService;
        this.emailService = emailService;
        this.tokenService = tokenService;
    }

    /**
     * Get form for entering email if user has forgotten their password
     *
     * @return lostPasswordPage
     */
    @GetMapping("/lost-password")
    public String lostPassword(@RequestParam(name = "emailAddress", defaultValue = "") String emailAddress,
                               Model model) {
        logger.info("GET /lost-password");
        model.addAttribute("emailAddress", emailAddress);
        return "lostPasswordPage";
    }

    /**
     * Post form for entering email if user has forgotten their password
     * Checks if input values are valid and then sends reset password link to email entered
     *
     * @param emailAddress input email to send reset password link to
     * @param model        to collect field values and error messages
     * @return lostPasswordPage
     */
    @PostMapping("/lost-password")
    public String emailChecker(@RequestParam("emailAddress") String emailAddress,
                               Model model) {

        logger.info("POST /lost-password");
        boolean isRegistered = userService.emailInUse(emailAddress);
        ValidationResult emailValidation = InputValidator.validateEmail(emailAddress);
        model.addAttribute("emailAddress", emailAddress);
        if (!emailValidation.valid()) {
            model.addAttribute("emailError", emailValidation);
        } else {
            model.addAttribute("message", "An email was sent to the address if it was recognised");
            model.addAttribute("goodMessage", true);
            if (isRegistered) {
                User currentUser = userService.getUserByEmail(emailAddress);
                Token token = new Token(currentUser, null);
                tokenService.addToken(token);
                model.addAttribute("emailSent", "An email was delivered");
                try {
                    emailService.sendResetPasswordEmail(token);
                } catch (MessagingException e) {
                    logger.info("could not send email to " + emailAddress);
                }
            }
        }
        return "lostPasswordPage";
    }


    /**
     * Get form for entering in new passwords (for resetting passwords)
     * Checks token in url is valid
     *
     * @param resetToken         unique temp token for resetting password
     * @param redirectAttributes to add message before redirecting to different page
     * @return form for resetting password
     */
    @GetMapping("/reset-password/{token}")
    public String resetPassword(@PathVariable("token") String resetToken,
                                RedirectAttributes redirectAttributes) {
        logger.info("GET /reset-password");

        Token token = tokenService.getTokenByTokenString(resetToken);
        if (token == null || token.isExpired()) {
            if (token != null) {
                tokenService.deleteToken(token);
            }
            redirectAttributes.addFlashAttribute("message", "Reset password link has expired");
            redirectAttributes.addFlashAttribute("goodMessage", false);
            return "redirect:/login";
        }

        return "resetPasswordPage";
    }

    /**
     * Get form for entering in new passwords (for resetting passwords)
     * Checks if input values are valid and then resets the password
     * Sends confirmation email to user
     * Also deletes the token to ensure it cannot be reused
     *
     * @param resetToken     token to identify user whose password has to be reset
     * @param password       new password
     * @param retypePassword new password
     * @param model          to collect field values and error messages
     * @return resetPasswordPage or loginPage if reset password is successful
     */
    @PostMapping("/reset-password/{token}")
    public String passwordChecker(@PathVariable("token") String resetToken,
                                  @RequestParam("password") String password,
                                  @RequestParam("retypePassword") String retypePassword,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        logger.info("POST /reset-password");

        Token token = tokenService.getTokenByTokenString(resetToken);
        User user = token.getUser();
        String emailAddress = user.getEmailAddress();
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        boolean noLastName = false;
        if (lastName == null) {
            noLastName = true;
        }
        LocalDate dateOfBirth = user.getDateOfBirth();

        List<String> otherFields = new ArrayList<>();
        otherFields.add(firstName);
        if (noLastName == false) {
            otherFields.add(lastName);
        }
        if (!(dateOfBirth == null)) {
            otherFields.add(dateOfBirth.toString());
        }
        otherFields.add(emailAddress);
        ValidationResult passwordValidation = InputValidator.validatePassword(password, otherFields);

        if (!passwordValidation.valid()) {
            model.addAttribute("passwordError", passwordValidation);
            return "resetPasswordPage";
        } else if (!Objects.equals(password, retypePassword)) {
            model.addAttribute("passwordError", "The passwords do not match");
            return "resetPasswordPage";
        } else {
            if (token == null || token.isExpired()) {
                if (token != null) {
                    tokenService.deleteToken(token);
                }
                redirectAttributes.addFlashAttribute("message", "Reset password link has expired");
                redirectAttributes.addFlashAttribute("goodMessage", false);
                return "redirect:/login";
            }
            User currentUser = token.getUser();
            userService.updatePassword(currentUser.getId(), password);
            tokenService.deleteToken(token);
            try {
                emailService.sendPasswordResetConfirmationEmail(currentUser);
            } catch (MessagingException e) {
                logger.error("Password reset confirmation email not sent");
            }
        }
        return "redirect:/login";
    }
}
