package pl.dudios.shopmvn.common.mail;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@AllArgsConstructor
public class EmailClientService {

    private final Map<String, EmailSender> senderMap;

    public EmailSender getSender(String senderName) {
        return senderMap.get(senderName);
    }

    public EmailSender getSender() {
        return senderMap.get("emailSimpleService");
    }
}
