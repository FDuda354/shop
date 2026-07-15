package pl.dudios.shopmvn.admin.order.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.dudios.shopmvn.admin.order.model.AdminOrder;
import pl.dudios.shopmvn.admin.order.model.log.AdminOrderLog;
import pl.dudios.shopmvn.admin.order.repository.AdminOrderLogRepo;
import pl.dudios.shopmvn.admin.order.repository.AdminOrderRepo;
import pl.dudios.shopmvn.common.model.OrderStatus;

import java.time.LocalDateTime;
import java.util.Map;


@Service
@AllArgsConstructor
public class AdminOrderService {

    private final AdminOrderRepo adminOrderRepo;
    private final AdminOrderLogRepo adminOrderLogRepo;
    private final AdminOrderEmailMessage adminOrderEmailMessage;

    public Page<AdminOrder> getOrders(Pageable pageable) {
        return adminOrderRepo.findAll(
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        Sort.by("id").descending())
        );
    }

    public AdminOrder getOrder(Long id) {
        return adminOrderRepo.findById(id).orElseThrow();
    }

    @Transactional
    public void updateOrderStatus(Long id, Map<String, String> values) {
        AdminOrder order = adminOrderRepo.findById(id).orElseThrow();

        changeOrderStatus(order, values);
    }

    private void changeOrderStatus(AdminOrder order, Map<String, String> values) {
        if (values.containsKey("orderStatus")) {

            OrderStatus oldStatus = order.getOrderStatus();
            OrderStatus newStatus = OrderStatus.valueOf(values.get("orderStatus"));
            System.out.println("====================================");
            System.out.println("oldStatus = " + oldStatus);
            System.out.println("newStatus = " + newStatus);
            System.out.println("===================order=================");

            System.out.println(order);
            if (oldStatus == newStatus)
                return;

            order.setOrderStatus(newStatus);
            System.out.println("===================save=================");
            adminOrderRepo.save(order);

            logStatusChange(order.getId(), oldStatus, newStatus);
            adminOrderEmailMessage.notifyClient(newStatus, order);
        }

    }


    private void logStatusChange(Long orderId, OrderStatus oldStatus, OrderStatus newStatus) {
        adminOrderLogRepo.save(
                AdminOrderLog.builder()
                        .created(LocalDateTime.now())
                        .orderId(orderId)
                        .note("Change of order status from" + oldStatus + " to " + newStatus)
                        .build()
        );
    }
}
