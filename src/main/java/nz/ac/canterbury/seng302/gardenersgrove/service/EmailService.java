package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;

import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * This class is a service class for sending emails
 * defined by the {@link Service} annotation.
 */
@Service
public class EmailService {
    Logger logger = LoggerFactory.getLogger(EmailService.class);

    private JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Autowired
    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    /**
     * The email address of the sender retrieved from the email.properties file.
     */
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Sends a plaintext email to the specified email address with the specified
     * subject and body.
     * 
     * @param toEmail the email recipient
     * @param subject the email subject
     * @param body    the email body
     * @throws MailException if the email cannot be sent
     */
    public void sendPlaintextEmail(String toEmail, String subject, String body) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    /**
     * Sends an HTML email to the specified email address with the specified subject
     * 
     * @param toEmail
     * @param subject
     * @param body
     * @throws MessagingException
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
        mailSender.send(message);
    }

    /**
     * Sends a registration email to the user with the token
     * 
     * @param token the token to send information about
     * @throws MessagingException
     */
    public void sendRegistrationEmail(Token token) throws MessagingException {
        String subject = "Welcome to Gardeners Grove!";
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

}
