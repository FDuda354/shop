package pl.dudios.shop.admin.order.stats.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record AdminOrderStats(
        List<Integer> labels,
        List<BigDecimal> ordersValue,
        List<Long> ordersCount
) {
}
