package pl.dudios.shopmvn.admin.order.stats.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shopmvn.admin.order.stats.model.AdminOrderStats;
import pl.dudios.shopmvn.admin.order.stats.service.AdminOrderStatsService;

@RestController
@RequestMapping("/admin/orders")
@AllArgsConstructor
public class AdminOrderStatsController {

    private final AdminOrderStatsService adminOrderStatsService;

    @GetMapping("/stats")
    public AdminOrderStats getOrdersStats() {
        return adminOrderStatsService.getOrdersStats();
    }

}

