package pl.dudios.shop.order.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shop.common.mail.EmailClientService;
import pl.dudios.shop.common.model.Basket;
import pl.dudios.shop.common.repository.BasketItemRepo;
import pl.dudios.shop.common.repository.BasketRepo;
import pl.dudios.shop.order.model.Order;
import pl.dudios.shop.order.model.OrderRow;
import pl.dudios.shop.order.model.Payment;
import pl.dudios.shop.order.model.Shipment;
import pl.dudios.shop.order.model.dto.OrderDto;
import pl.dudios.shop.order.model.dto.OrderDtoForUser;
import pl.dudios.shop.order.model.dto.OrderSummary;
import pl.dudios.shop.order.repositroy.OrderRepo;
import pl.dudios.shop.order.repositroy.OrderRowRepo;
import pl.dudios.shop.order.repositroy.PaymentRepo;
import pl.dudios.shop.order.repositroy.ShipmentRepo;

import java.util.List;

import static pl.dudios.shop.order.service.mapper.OrderEmailMessageMapper.createEmailMessage;
import static pl.dudios.shop.order.service.mapper.OrderMapper.createNewOrder;
import static pl.dudios.shop.order.service.mapper.OrderMapper.createOrderListDtoForUser;
import static pl.dudios.shop.order.service.mapper.OrderMapper.createOrderSummary;
import static pl.dudios.shop.order.service.mapper.OrderMapper.mapToOrderRow;
import static pl.dudios.shop.order.service.mapper.OrderMapper.mapToOrderRowWithQuantity;

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
        Basket basket = basketRepo.findById(orderDto.basketId()).orElseThrow();
        Shipment shipment = shipmentRepo.findById(orderDto.shipmentId()).orElseThrow();
        Payment payment = paymentRepo.findById(orderDto.paymentId()).orElseThrow();
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
        basketItemRepo.deleteByBasketId(orderDto.basketId());
        basketRepo.deleteBasketById(orderDto.basketId());
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
