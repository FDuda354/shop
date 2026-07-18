package pl.dudios.shop.admin.order.model.dto;

import lombok.Builder;
import pl.dudios.shop.common.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record AdminOrderDto(
        Long id,
        LocalDateTime placeDate,
        OrderStatus orderStatus,
        BigDecimal grossValue
) {
}
