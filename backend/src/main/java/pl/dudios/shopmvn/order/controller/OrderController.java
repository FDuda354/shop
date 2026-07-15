package pl.dudios.shopmvn.order.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.order.model.dto.InitOrder;
import pl.dudios.shopmvn.order.model.dto.OrderDto;
import pl.dudios.shopmvn.order.model.dto.OrderDtoForUser;
import pl.dudios.shopmvn.order.model.dto.OrderSummary;
import pl.dudios.shopmvn.order.service.OrderService;
import pl.dudios.shopmvn.order.service.PaymentService;
import pl.dudios.shopmvn.order.service.ShipmentService;

import java.util.List;

@RestController
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ShipmentService shipmentService;
    private final PaymentService paymentService;

    @PostMapping("/order")
    public OrderSummary createOrder(@RequestBody OrderDto orderDto, @AuthenticationPrincipal Long userId) {
        return orderService.createOrder(orderDto, userId);
    }

    @GetMapping("/order/initOrder")
    public InitOrder initOrder() {
        return InitOrder.builder()
                .shipments(shipmentService.getShipments())
                .payments(paymentService.getPayments())
                .build();
    }

    @GetMapping("/orders")
    public List<OrderDtoForUser> getOrders(@AuthenticationPrincipal Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("NULL user in getOrders");
        }
        System.out.println(orderService.getOrdersFromUser(userId));
        return orderService.getOrdersFromUser(userId);
    }
}
