package pl.dudios.shopmvn.admin.order.stats.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dudios.shopmvn.admin.order.model.AdminOrder;
import pl.dudios.shopmvn.admin.order.repository.AdminOrderRepo;
import pl.dudios.shopmvn.admin.order.stats.model.AdminOrderStats;
import pl.dudios.shopmvn.common.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@AllArgsConstructor
public class AdminOrderStatsService {

    private final AdminOrderRepo adminOrderRepo;

    public AdminOrderStats getOrdersStats() {
        LocalDateTime from = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime to = LocalDateTime.now();
        List<AdminOrder> orders = adminOrderRepo.findAllByPlaceDateIsBetweenAndOrderStatus(
                from,
                to,
                OrderStatus.COMPLETED
        );

        Map<Integer, AdminOrderStatsValue> result = new TreeMap<>();

        for (int i = from.getDayOfMonth(); i <= to.getDayOfMonth(); i++) {
            result.put(i, aggregateValue(i, orders));
        }

        return AdminOrderStats.builder()
                .labels(result.keySet().stream().toList())
                .ordersValue(result.values().stream().map(AdminOrderStatsValue::sum).toList())
                .ordersCount(result.values().stream().map(AdminOrderStatsValue::orderCount).toList())
                .build();
    }

    private AdminOrderStatsValue aggregateValue(int i, List<AdminOrder> orders) {
        BigDecimal[] result = orders.stream()
                .filter(order -> order.getPlaceDate().getDayOfMonth() == i)
                .map(order -> new BigDecimal[]{order.getGrossValue(), BigDecimal.ONE})
                .reduce(new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO}, (a, b) -> new BigDecimal[]{a[0].add(b[0]), a[1].add(b[1])});

        return new AdminOrderStatsValue(result[0], result[1].longValue());
    }

    private record AdminOrderStatsValue(BigDecimal sum, Long orderCount) {
    }


}
