package pl.dudios.shop.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shop.order.model.Shipment;
import pl.dudios.shop.order.repositroy.ShipmentRepo;

import java.util.List;

@Service
@AllArgsConstructor
public class ShipmentService {

    private final ShipmentRepo shipmentRepo;

    public List<Shipment> getShipments() {
        return shipmentRepo.findAll();
    }
}
