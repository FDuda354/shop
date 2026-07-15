package pl.dudios.shopmvn.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shopmvn.common.mail.EmailClientService;
import pl.dudios.shopmvn.common.model.Basket;
import pl.dudios.shopmvn.common.repository.BasketItemRepo;
import pl.dudios.shopmvn.common.repository.BasketRepo;
import pl.dudios.shopmvn.order.model.Order;
import pl.dudios.shopmvn.order.model.OrderRow;
import pl.dudios.shopmvn.order.model.Payment;
import pl.dudios.shopmvn.order.model.Shipment;
import pl.dudios.shopmvn.order.model.dto.OrderDto;
import pl.dudios.shopmvn.order.model.dto.OrderDtoForUser;
import pl.dudios.shopmvn.order.model.dto.OrderSummary;
import pl.dudios.shopmvn.order.repositroy.OrderRepo;
import pl.dudios.shopmvn.order.repositroy.OrderRowRepo;
import pl.dudios.shopmvn.order.repositroy.PaymentRepo;
import pl.dudios.shopmvn.order.repositroy.ShipmentRepo;

import java.util.List;

import static pl.dudios.shopmvn.order.service.mapper.OrderEmailMessageMapper.createEmailMessage;
import static pl.dudios.shopmvn.order.service.mapper.OrderMapper.createNewOrder;
import static pl.dudios.shopmvn.order.service.mapper.OrderMapper.createOrderListDtoForUser;
import static pl.dudios.shopmvn.order.service.mapper.OrderMapper.createOrderSummary;
import static pl.dudios.shopmvn.order.service.mapper.OrderMapper.mapToOrderRow;
import static pl.dudios.shopmvn.order.service.mapper.OrderMapper.mapToOrderRowWithQuantity;

@Service
@AllArgsConstructor
public class OrderService {
    private final OrderRepo orderRepo;
    private final OrderRowRepo orderRowRepo;
    private final BasketRepo basketRepo;
    private final BasketItemRepo basketItemRepo;
    private final ShipmentRepo shipmentRepo;
    private final PaymentRepo paymentRepo;
    private final EmailClientService emailClientService;

    @Transactional
    public OrderSummary createOrder(OrderDto orderDto, Long userId) {
        Basket basket = basketRepo.findById(orderDto.getBasketId()).orElseThrow();
        Shipment shipment = shipmentRepo.findById(orderDto.getShipmentId()).orElseThrow();
        Payment payment = paymentRepo.findById(orderDto.getPaymentId()).orElseThrow();
        Order newOrder = orderRepo.save(createNewOrder(orderDto, basket, shipment, payment, userId));

        saveOrderRows(basket, newOrder.getId(), shipment);
        clearBasket(orderDto);

        sendConfirmEmail(newOrder);

        return createOrderSummary(newOrder);
    }

    private void sendConfirmEmail(Order newOrder) {
        emailClientService.getSender()
                .sendEmail(newOrder.getEmail(), "Order confirmation", createEmailMessage(newOrder));
    }

    private void clearBasket(OrderDto orderDto) {
        basketItemRepo.deleteByBasketId(orderDto.getBasketId());
        basketRepo.deleteBasketById(orderDto.getBasketId());
    }


    private void saveOrderRows(Basket basket, Long orderId, Shipment shipment) {
        saveProductRows(basket, orderId);
        saveShipmentRow(orderId, shipment);
    }

    private void saveShipmentRow(Long orderId, Shipment shipment) {
        orderRowRepo.save(mapToOrderRow(orderId, shipment));
    }

    private void saveProductRows(Basket basket, Long orderId) {
        List<OrderRow> orderRows = basket.getItems().stream()
                .map(item -> mapToOrderRowWithQuantity(orderId, item))
                .peek(orderRowRepo::save)
                .toList();
    }

    public List<OrderDtoForUser> getOrdersFromUser(Long userId) {
        return createOrderListDtoForUser(orderRepo.findAllByUserId(userId));
    }

}
