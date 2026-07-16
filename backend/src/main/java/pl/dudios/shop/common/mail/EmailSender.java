package pl.dudios.shop.common.mail;

public interface EmailSender {
    void sendEmail(String to, String subject, String content);
}
