package pl.dudios.shop.order.repositroy;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dudios.shop.order.model.Shipment;

@Repository
public interface ShipmentRepo extends JpaRepository<Shipment, Long> {
}
