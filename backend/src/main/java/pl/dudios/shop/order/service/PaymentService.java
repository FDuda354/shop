package pl.dudios.shop.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shop.order.model.Payment;
import pl.dudios.shop.order.repositroy.PaymentRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;

    public List<Payment> getPayments() {
        return paymentRepo.findAll();
    }
}
