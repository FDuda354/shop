package pl.dudios.shopmvn.admin.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shopmvn.admin.order.model.AdminOrder;
import pl.dudios.shopmvn.common.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;


@Repository
public interface AdminOrderRepo extends JpaRepository<AdminOrder, Long> {

    List<AdminOrder> findAllByPlaceDateIsBetweenAndOrderStatus(LocalDateTime from, LocalDateTime to, OrderStatus orderStatus);
}
