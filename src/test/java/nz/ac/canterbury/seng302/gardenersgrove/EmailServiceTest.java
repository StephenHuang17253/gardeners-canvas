package nz.ac.canterbury.seng302.gardenersgrove;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;

import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.SimpleMailMessage;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class EmailServiceTest {

    private static EmailService emailService;
    private static TemplateEngine templateEngine;

    private static JavaMailSender mailSender;

    public static String toEmail = "test@example.com";

    @BeforeAll
    public static void setup() throws MessagingException {
        mailSender = spy(JavaMailSender.class);
        doNothing().when(mailSender).send(Mockito.any(SimpleMailMessage.class));

        templateEngine = mock(TemplateEngine.class);
        when(templateEngine.process(Mockito.anyString(), Mockito.any(Context.class))).thenReturn("Test Body");

        emailService = spy(new EmailService(mailSender, templateEngine));

        when(emailService.getSenderEmail()).thenReturn("test@email.com");
    }

    @Test
    public void testSendPlaintextEmail() throws MessagingException {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPlaintextEmail(toEmail, subject, body);

        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }

}
