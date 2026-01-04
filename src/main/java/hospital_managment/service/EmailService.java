package hospital_managment.service;

import hospital_managment.domain.User;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.Properties;
import javax.mail.*;
import javax.mail.internet.*;

public class EmailService {
    
    private static final Dotenv dotenv = Dotenv.load();
    private static final String SMTP_HOST = dotenv.get("SMTP_HOST", "smtp.gmail.com");
    private static final String SMTP_PORT = dotenv.get("SMTP_PORT", "587");
    private static final String SMTP_USERNAME = dotenv.get("SMTP_USERNAME", "");
    private static final String SMTP_PASSWORD = dotenv.get("SMTP_PASSWORD", "");
    private static final String FROM_EMAIL = dotenv.get("FROM_EMAIL", "noreply@hospital.com");
    private static final String BASE_URL = dotenv.get("BASE_URL", "http://localhost:8080");

    public void sendVerificationEmail(User user, String verificationToken) {
        String verificationLink = buildVerificationLink(verificationToken);
        
        String subject = "Verify your hospital management account";
        String body = "Dear " + user.getName() + ",\n\n" +
                     "Please click the link below to verify your email address:\n" +
                     verificationLink + "\n\n" +
                     "This link will expire in 24 hours.\n\n" +
                     "If you didn't create this account, please ignore this email.\n\n" +
                     "Best regards,\n" +
                     "Hospital Management System";
        
        if (!SMTP_USERNAME.isEmpty() && !SMTP_PASSWORD.isEmpty()) {
            try {
                sendEmail(user.getEmail(), subject, body);
                return;
            } catch (Exception e) {
            }
        }
    }
    
    private void sendEmail(String to, String subject, String body) throws MessagingException {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USERNAME, SMTP_PASSWORD);
            }
        });
        
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(body);
        
        Transport.send(message);
    }

    private String buildVerificationLink(String token) {
        return BASE_URL + "/api/verify-email?token=" + token;
    }

}
