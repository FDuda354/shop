package pl.dudios.shop.order.model.dto;

import lombok.Builder;
import pl.dudios.shop.common.model.OrderStatus;
import pl.dudios.shop.order.model.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record OrderSummary(
        Long id,
        LocalDateTime placeDate,
        OrderStatus status,
        BigDecimal grossValue,
        Payment payment
) {
}
