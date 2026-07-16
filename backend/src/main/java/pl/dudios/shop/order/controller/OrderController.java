package pl.dudios.shop.order.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shop.security.user.model.AppUserDetails;
import pl.dudios.shop.order.model.dto.InitOrder;
import pl.dudios.shop.order.model.dto.OrderDto;
import pl.dudios.shop.order.model.dto.OrderDtoForUser;
import pl.dudios.shop.order.model.dto.OrderSummary;
import pl.dudios.shop.order.service.OrderService;
import pl.dudios.shop.order.service.PaymentService;
import pl.dudios.shop.order.service.ShipmentService;

import java.util.List;

@RestController
@AllArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final ShipmentService shipmentService;
    private final PaymentService paymentService;

    @PostMapping("/order")
    public OrderSummary createOrder(@RequestBody OrderDto orderDto, @AuthenticationPrincipal AppUserDetails user) {
        return orderService.createOrder(orderDto, user != null ? user.getId() : null);
    }

    @GetMapping("/order/initOrder")
    public InitOrder initOrder() {
        return InitOrder.builder()
                .shipments(shipmentService.getShipments())
                .payments(paymentService.getPayments())
                .build();
    }

    @GetMapping("/orders")
    public List<OrderDtoForUser> getOrders(@AuthenticationPrincipal AppUserDetails user) {
        if (user == null) {
            throw new IllegalArgumentException("NULL user in getOrders");
        }
        return orderService.getOrdersFromUser(user.getId());
    }
}
