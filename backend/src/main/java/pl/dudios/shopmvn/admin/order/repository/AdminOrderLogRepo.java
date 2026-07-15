package pl.dudios.shopmvn.admin.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shopmvn.admin.order.model.log.AdminOrderLog;

@Repository
public interface AdminOrderLogRepo extends JpaRepository<AdminOrderLog, Long> {
}
