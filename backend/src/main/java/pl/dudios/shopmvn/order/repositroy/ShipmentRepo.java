package pl.dudios.shopmvn.order.repositroy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shopmvn.order.model.Shipment;

@Repository
public interface ShipmentRepo extends JpaRepository<Shipment, Long> {
}
