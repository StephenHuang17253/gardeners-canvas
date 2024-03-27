package nz.ac.canterbury.seng302.gardenersgrove.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Service
public class EmailService {
    Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendPlainTextEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(senderEmail);
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        logger.info("Email sent to: " + toEmail);
    }

    public void sendHTMLEmail(String toEmail, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
        helper.setFrom(senderEmail);
        helper.setTo(toEmail);
        helper.setSubject(subject);
        helper.setText(body, true); // Set the second parameter to true to indicate the text is HTML
        mailSender.send(message);
        logger.info("Email sent to: " + toEmail);
    }

    public void sendRegistrationEmail(RegistrationToken token) {
        String subject = "Welcome to Gardeners Grove!";
        String body = "<html><body><p>Thank you for registering with Gardeners Grove!</p><p>Here is your signup code: "
                + token.getTokenString() + "</p></body></html>";
        sendHTMLEmail(token.getEmail(), subject, body);
    }

}
