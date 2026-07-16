package pl.dudios.shop.admin.order.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dudios.shop.admin.order.controller.mapper.AdminOrderMapper;
import pl.dudios.shop.admin.order.model.AdminOrder;
import pl.dudios.shop.admin.order.model.dto.AdminInitDataDto;
import pl.dudios.shop.admin.order.model.dto.AdminOrderDto;
import pl.dudios.shop.admin.order.service.AdminOrderService;
import pl.dudios.shop.common.model.OrderStatus;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/admin")
@AllArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping("/orders")
    public Page<AdminOrderDto> getOrders(Pageable pageable) {
        return AdminOrderMapper.mapToPageDtos(adminOrderService.getOrders(pageable));
    }

    @GetMapping("/order/{id}")
    public AdminOrder getOrders(@PathVariable Long id) {
        return adminOrderService.getOrder(id);
    }

    @PatchMapping("/order/{id}")
    public void updateOrderStatus(@PathVariable Long id, @RequestBody Map<String, String> values) {
        adminOrderService.updateOrderStatus(id, values);
    }

    @GetMapping("/orders/initData")
    public AdminInitDataDto getInitData() {
        return new AdminInitDataDto(createOrderStatusMap());
    }

    private Map<String, String> createOrderStatusMap() {
        return Arrays.stream(OrderStatus.values())
                .collect(Collectors.toMap(OrderStatus::name, OrderStatus::getValue));
    }
}
