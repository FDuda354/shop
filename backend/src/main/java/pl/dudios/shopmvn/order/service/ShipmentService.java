package pl.dudios.shopmvn.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.order.model.Shipment;
import pl.dudios.shopmvn.order.repositroy.ShipmentRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class ShipmentService {

    private final ShipmentRepo shipmentRepo;

    public List<Shipment> getShipments() {
        return shipmentRepo.findAll();
    }
}
