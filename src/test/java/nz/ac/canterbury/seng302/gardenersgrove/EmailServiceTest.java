package nz.ac.canterbury.seng302.gardenersgrove;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;

import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class EmailServiceTest {

    private static EmailService emailService;

    private static JavaMailSender mailSender;

    @BeforeAll
    public static void setup() {
        mailSender = spy(JavaMailSender.class);
        doNothing().when(mailSender).send(Mockito.any(SimpleMailMessage.class));
        emailService = new EmailService(mailSender);
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

    @Test
    public void testSendHTMLEmail() throws MessagingException {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "<html><body><h1>Test Body</h1></body></html>";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendHTMLEmail(toEmail, subject, body);

        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }

}
