package pl.dudios.shopmvn.common.mail;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FakeEmailService implements EmailSender {

    @Async
    @Override
    public void sendEmail(String to, String subject, String content) {
        log.info("Email sent");
        log.info("Sending email to: " + to);
        log.info("Subject: " + subject);
        log.info("Content: " + content);
    }
}
