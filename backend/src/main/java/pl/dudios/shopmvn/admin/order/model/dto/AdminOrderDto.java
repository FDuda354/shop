package pl.dudios.shopmvn.admin.order.model.dto;

import lombok.Builder;
import lombok.Getter;
import pl.dudios.shopmvn.common.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class AdminOrderDto {
    private Long id;
    private LocalDateTime placeDate;
    private OrderStatus orderStatus;
    private BigDecimal grossValue;
}
