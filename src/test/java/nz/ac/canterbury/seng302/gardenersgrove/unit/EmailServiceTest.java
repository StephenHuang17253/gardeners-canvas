package nz.ac.canterbury.seng302.gardenersgrove.unit;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import nz.ac.canterbury.seng302.gardenersgrove.entity.Token;
import nz.ac.canterbury.seng302.gardenersgrove.entity.User;
import nz.ac.canterbury.seng302.gardenersgrove.service.EmailService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.IOException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(properties = { "spring.mail.username=test@sender.com" })
class EmailServiceTest {

    private EmailService emailService;
    private TemplateEngine templateEngine;
    private JavaMailSender mailSender;

    public static String toEmail = "test@example.com";

    @BeforeEach
    public void setup() throws MessagingException {
        mailSender = spy(JavaMailSenderImpl.class);
        doNothing().when(mailSender).send(Mockito.any(SimpleMailMessage.class));
        doNothing().when(mailSender).send(Mockito.any(MimeMessage.class));

        templateEngine = mock(TemplateEngine.class);
        when(templateEngine.process(Mockito.anyString(), Mockito.any(Context.class))).thenReturn("Test Body");

        emailService = spy(new EmailService(mailSender, templateEngine,"test@sender.com","http://test/"));

    }

    @Test
    void testSendPlaintextEmail() throws MessagingException {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendPlaintextEmail(toEmail, subject, body);

        verify(mailSender,  timeout(1000).times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertEquals(toEmail, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(body, sentMessage.getText());
    }

    @Test
    void testSendHTMLEmail() throws MessagingException {
        String toEmail = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);

        String template = "registrationEmail";

        Context context = new Context();
        context.setVariable("username", "TestUserName");
        context.setVariable("tokenString", "testTokenString");
        context.setVariable("lifetime", "testLifeTime");

        emailService.sendHTMLEmail(toEmail, subject,  template, context);

        verify(mailSender, timeout(1000).times(1)).send(captor.capture());
        // A wanted but not invoked error here usually means that the asynchronous thread sending the email did not
        // do so in time before it was expected


        MimeMessage sentMessage = captor.getValue();
        assertEquals(toEmail, sentMessage.getAllRecipients()[0].toString());
        assertEquals("test@sender.com", sentMessage.getFrom()[0].toString());
        assertEquals(subject, sentMessage.getSubject());
    }

    @Test
    void testSendPasswordResetEmail() throws MessagingException {

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);

        doNothing().when(emailService).sendHTMLEmail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(), Mockito.any());
        Token mockToken = Mockito.mock(Token.class);
        User mockUser = Mockito.mock(User.class);
        Duration mockDuration = Mockito.mock(Duration.class);

        doReturn(mockUser).when(mockToken).getUser();
        doReturn("TestTokenString").when(mockToken).getTokenString();
        doReturn("recipient@test.com").when(mockUser).getEmailAddress();
        doReturn("Test").when(mockUser).getFirstName();
        doReturn("User").when(mockUser).getLastName();
        doReturn(mockDuration).when(mockToken).getLifetime();
        doReturn(10L).when(mockDuration).toMinutes();

        emailService.sendResetPasswordEmail(mockToken);

        verify(emailService, times(1)).sendHTMLEmail(eq("recipient@test.com"),eq("Link to Reset Password to Gardener's Grove!"),
                eq("generalEmail"),captor.capture());

        Context capturedContex = captor.getValue();

        assertEquals("Test User", capturedContex.getVariable("username"));
        assertEquals("Click the link below to reset your password. \n This link expires in 10 minutes.", capturedContex.getVariable("mainBody"));
        assertEquals("http://test/reset-password/TestTokenString", capturedContex.getVariable("url"));
        assertEquals("RESET PASSWORD", capturedContex.getVariable("urlText"));

    }

    @Test
    void testSendPasswordResetConfirmationEmail() throws MessagingException {

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);

        doNothing().when(emailService).sendHTMLEmail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(), Mockito.any());
        User mockUser = Mockito.mock(User.class);
        Duration mockDuration = Mockito.mock(Duration.class);

        doReturn("recipient@test.com").when(mockUser).getEmailAddress();
        doReturn("Test").when(mockUser).getFirstName();
        doReturn("User").when(mockUser).getLastName();
        doReturn(10L).when(mockDuration).toMinutes();

        emailService.sendPasswordResetConfirmationEmail(mockUser);

        verify(emailService, times(1)).sendHTMLEmail(eq("recipient@test.com"),eq("Your Password Has Been Updated"),
                eq("generalEmail"),captor.capture());

        Context capturedContex = captor.getValue();

        assertEquals("Test User", capturedContex.getVariable("username"));
        assertEquals("This email is to confirm that your Gardener's Grove account's password has been updated", capturedContex.getVariable("mainBody"));

    }

    @Test
    void testSendRegistrationEmail() throws MessagingException {

        ArgumentCaptor<Context> captor = ArgumentCaptor.forClass(Context.class);

        doNothing().when(emailService).sendHTMLEmail(Mockito.anyString(),Mockito.anyString(),Mockito.anyString(), Mockito.any());
        Token mockToken = Mockito.mock(Token.class);
        User mockUser = Mockito.mock(User.class);
        Duration mockDuration = Mockito.mock(Duration.class);

        doReturn(mockUser).when(mockToken).getUser();
        doReturn("TestTokenString").when(mockToken).getTokenString();
        doReturn("recipient@test.com").when(mockUser).getEmailAddress();
        doReturn("Test").when(mockUser).getFirstName();
        doReturn("User").when(mockUser).getLastName();
        doReturn(mockDuration).when(mockToken).getLifetime();
        doReturn(10L).when(mockDuration).toMinutes();

        emailService.sendRegistrationEmail(mockToken);

        verify(emailService, times(1)).sendHTMLEmail(eq("recipient@test.com"),eq("Welcome to Gardener's Grove!"),
                eq("registrationEmail"),captor.capture());

        Context capturedContex = captor.getValue();

        assertEquals("Test User", capturedContex.getVariable("username"));
        assertEquals(10, capturedContex.getVariable("lifetime"));
        assertEquals("TestTokenString", capturedContex.getVariable("tokenString"));

    }

}
