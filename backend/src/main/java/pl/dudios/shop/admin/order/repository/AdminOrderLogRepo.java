package pl.dudios.shop.admin.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shop.admin.order.model.log.AdminOrderLog;

@Repository
public interface AdminOrderLogRepo extends JpaRepository<AdminOrderLog, Long> {
}
