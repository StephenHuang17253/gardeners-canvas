package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailException;
import org.springframework.mail.MailParseException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * This class is a service class for sending emails
 * defined by the {@link Service} annotation.
 */
@Service
public class EmailService {
    Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    /**
     * The email address of the sender retrieved from the email.properties file.
     */
    @Value("${spring.mail.username}")
    private String senderEmail;

    /**
     * Sends an email to the specified email address with the specified subject and
     * body.
     * 
     * @param toEmail the email recipient
     * @param subject the email subject
     * @param body    the email body
     * @throws MailException if the email cannot be sent
     */
    public void sendEmail(String toEmail, String subject, String body) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        logger.info("Email sent to: " + toEmail);
    }

}
