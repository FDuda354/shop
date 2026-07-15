package pl.dudios.shopmvn.admin.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.admin.order.model.AdminOrder;
import pl.dudios.shopmvn.common.mail.EmailClientService;
import pl.dudios.shopmvn.common.model.OrderStatus;


@Service
@AllArgsConstructor
public class AdminOrderEmailMessage {

    private final EmailClientService emailClientService;

    public static String createMessage(AdminOrder order, OrderStatus newStatus) {
        return "Hi " + order.getFirstName() + " " + order.getLastName() + ","
                + "Your order " + order.getId() + " has been " + newStatus.name().toLowerCase() + "."
                + "Greetings";
    }

    public void notifyClient(OrderStatus newStatus, AdminOrder order) {
        if (newStatus == OrderStatus.PROCESSING) {
            sendEmail(order.getEmail(), "Change your order status with ID: " + order.getId() + "to" + newStatus.getValue(), createMessage(order, newStatus));
        } else if (newStatus == OrderStatus.WAITING_FOR_DELIVERY) {
            sendEmail(order.getEmail(), "Change your order status with ID: " + order.getId() + "to" + newStatus.getValue(), createMessage(order, newStatus));
        } else if (newStatus == OrderStatus.COMPLETED) {
            sendEmail(order.getEmail(), "Change your order status with ID: " + order.getId() + "to" + newStatus.getValue(), createMessage(order, newStatus));
        } else if (newStatus == OrderStatus.REFUND) {
            sendEmail(order.getEmail(), "Change your order status with ID: " + order.getId() + "to" + newStatus.getValue(), createMessage(order, newStatus));
        }
    }

    private void sendEmail(String email, String subject, String content) {
        emailClientService.getSender().sendEmail(email, subject, content);
    }
}
