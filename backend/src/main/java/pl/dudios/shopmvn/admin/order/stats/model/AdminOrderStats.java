package pl.dudios.shopmvn.admin.order.stats.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class AdminOrderStats {

    private List<Integer> labels;
    private List<BigDecimal> ordersValue;
    private List<Long> ordersCount;
}
