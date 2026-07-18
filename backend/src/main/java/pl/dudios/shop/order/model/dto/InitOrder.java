package pl.dudios.shop.order.model.dto;

import pl.dudios.shop.order.model.Payment;
import pl.dudios.shop.order.model.Shipment;

import java.util.List;

public record InitOrder(List<Shipment> shipments, List<Payment> payments) {
}
