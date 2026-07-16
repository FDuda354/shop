package pl.dudios.shop.order.repositroy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shop.order.model.Payment;

@Repository
public interface PaymentRepo extends JpaRepository<Payment, Long> {
}
