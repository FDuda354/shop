package pl.dudios.shopmvn.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.order.model.Payment;
import pl.dudios.shopmvn.order.repositroy.PaymentRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentRepo paymentRepo;

    public List<Payment> getPayments() {
        return paymentRepo.findAll();
    }
}
