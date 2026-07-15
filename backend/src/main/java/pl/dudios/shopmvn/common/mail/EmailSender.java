package pl.dudios.shopmvn.common.mail;

public interface EmailSender {
    void sendEmail(String to, String subject, String content);
}
