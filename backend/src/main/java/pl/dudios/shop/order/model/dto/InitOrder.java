package pl.dudios.shop.order.model.dto;

import lombok.Builder;
import lombok.Getter;
import pl.dudios.shop.order.model.Payment;
import pl.dudios.shop.order.model.Shipment;

import java.util.List;

@Getter
@Builder
public class InitOrder {
    private List<Shipment> shipments;
    private List<Payment> payments;
}
