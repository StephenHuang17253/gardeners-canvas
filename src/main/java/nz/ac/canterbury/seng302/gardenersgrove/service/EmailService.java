package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;

/**
 * This class is a service class for sending emails defined by the
 * {@link Service} annotation.
 */
@Service
public class EmailService {

    Logger logger = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    /**
     * The email address of the sender retrieved from the email.properties file.
     */
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * The base url of application
     */
    @Value("${spring.base.url}")
    private String baseURL;

    /**
     * Autowired default constructor for Email Service
     * @param mailSender mail sender bean
     * @param templateEngine mail sender bean
     */
    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * <h4 style="color:red;"> CONSTRUCTOR FOR TEST USE ONLY </h4>
     * Overloaded constructor for email service,
     * Overwrites application properties usually defined at runtime in order to make testing this service easier
     * @param mailSender mail sender bean
     * @param templateEngine mail sender bean
     * @param overwrittenBaseUrl overwritten url basis (where emails are sent to)
     * @param overwrittenSenderEmail overwritten sending email address (where emails are sent froim)
     */
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine, String overwrittenSenderEmail, String overwrittenBaseUrl) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.baseURL = overwrittenBaseUrl;
        this.senderEmail = overwrittenSenderEmail;
    }



    public String getBaseURL() {
        return baseURL;
    }

    /**
     * Sends a plaintext email to the specified email address with the specified
     * subject and body.
     * Actual email is sent asynchronously to make the app more responsive.
     * (the send email function takes 5 seconds to run)
     *
     * @param toEmail the email recipient
     * @param subject the email subject
     * @param body the email body
     * @throws MailException if the email cannot be sent
     */
    public void sendPlaintextEmail(String toEmail, String subject, String body) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        Thread asynchronousEmailthread = new Thread(() -> {
            try
            {
                mailSender.send(message);
                logger.info("Email Sent");
            }
            catch (MailException error)
            {
                logger.error("Email could not be sent");
            }
        });
        asynchronousEmailthread.start();
    }

    /**
     * Sends an HTML email to the specified email address with the specified
     * subject. Actual email is sent asynchronously to make the app more responsive.
     * (the send email function takes 5 seconds to run)
     *
     * @param recipientEmail email of person to receive message
     * @param subject subject of email
     * @param template html template to fill for email
     * @throws MessagingException if cannot send email
     */
    public void sendHTMLEmail(String recipientEmail, String subject, String template, Context context)
            throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        String htmlContent = templateEngine.process(template, context);

        helper.setFrom(senderEmail);
        helper.setTo(recipientEmail);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        Thread asynchronousEmailthread = new Thread(() -> {
            try
            {
                mailSender.send(message);
                logger.info("Email Sent");
            }
            catch (MailException error)
            {
                logger.error("Email could not be sent");
            }
        });
        asynchronousEmailthread.start();


    }

    /**
     * Sends a registration email to the user with the token
     *
     * @param token the token to send information about
     * @throws MessagingException
     */
    public void sendRegistrationEmail(Token token) throws MessagingException {
        String subject = "Welcome to Gardener's Grove!";
        String template = "registrationEmail";

        String username = token.getUser().getFirstName() + " " + token.getUser().getLastName();
        String tokenString = token.getTokenString();
        int lifetime = (int) token.getLifetime().toMinutes();

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("tokenString", tokenString);
        context.setVariable("lifetime", lifetime);

        String toEmail = token.getUser().getEmailAddress();
        sendHTMLEmail(toEmail, subject, template, context);
    }

    /**
     * Sends a reset password email to the user with token in reset password
     * link
     *
     * @param token the token to use to reset password
     */
    public void sendResetPasswordEmail(Token token) throws MessagingException {
        logger.info("Sending reset password email to " + token.getUser().getEmailAddress());
        String subject = "Link to Reset Password to Gardener's Grove!";
        String template = "generalEmail";

        String username = token.getUser().getFirstName() + " " + token.getUser().getLastName();
        String tokenString = token.getTokenString();
        int lifetime = (int) token.getLifetime().toMinutes();

        String url = UriComponentsBuilder.fromUriString(getBaseURL())
                .path("/reset-password/{token}")
                .buildAndExpand(tokenString)
                .toUriString();
        String urlText = "RESET PASSWORD";
        String mainBody = String.format("Click the link below to reset your password. \n This link expires in %s minutes.", lifetime);

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("mainBody", mainBody);
        context.setVariable("url", url);
        context.setVariable("urlText", urlText);

        String toEmail = token.getUser().getEmailAddress();
        sendHTMLEmail(toEmail, subject, template, context);
    }

    /**
     * Sends a confirmation of reset password
     *
     * @param currentUser user to send confirmation of password reset to
     */
    public void sendPasswordResetConfirmationEmail(User currentUser) throws MessagingException {
        logger.info("Sending confirmation email to " + currentUser.getEmailAddress());
        String subject = "Your Password Has Been Updated";
        String template = "generalEmail";

        String username = currentUser.getFirstName() + " " + currentUser.getLastName();
        String mainBody = "This email is to confirm that your Gardener's Grove account's password has been updated";

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("mainBody", mainBody);

        String toEmail = currentUser.getEmailAddress();
        sendHTMLEmail(toEmail, subject, template, context);
    }
}
