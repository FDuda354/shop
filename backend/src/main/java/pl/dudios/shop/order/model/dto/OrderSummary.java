package pl.dudios.shop.order.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.dudios.shop.common.model.OrderStatus;
import pl.dudios.shop.order.model.Payment;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderSummary {
    private Long id;
    private LocalDateTime placeDate;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private BigDecimal grossValue;
    private Payment payment;

}
