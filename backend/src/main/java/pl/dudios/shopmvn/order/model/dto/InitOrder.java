package pl.dudios.shopmvn.order.model.dto;

import lombok.Builder;
import lombok.Getter;
import pl.dudios.shopmvn.order.model.Payment;
import pl.dudios.shopmvn.order.model.Shipment;

import java.util.List;

@Getter
@Builder
public class InitOrder {
    private List<Shipment> shipments;
    private List<Payment> payments;
}
