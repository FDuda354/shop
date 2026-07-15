package pl.dudios.shopmvn.common.mail;

import lombok.AllArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class EmailSimpleService implements EmailSender {

    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("DUDIOS Shop <postmaster@dudios.pl>");
        message.setReplyTo("DUDIOS Shop <filip@dudios.pl>");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(content);

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Error while sending email: " + e.getMessage());
        }

    }
}
