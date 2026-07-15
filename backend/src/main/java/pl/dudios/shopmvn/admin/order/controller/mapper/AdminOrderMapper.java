package pl.dudios.shopmvn.admin.order.controller.mapper;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import pl.dudios.shopmvn.admin.order.model.AdminOrder;
import pl.dudios.shopmvn.admin.order.model.dto.AdminOrderDto;

import java.util.List;

public class AdminOrderMapper {

    private AdminOrderMapper() {
    }

    public static Page<AdminOrderDto> mapToPageDtos(Page<AdminOrder> orders) {
        return new PageImpl<>(mapToDtoList(orders.getContent()), orders.getPageable(), orders.getTotalElements());
    }

    private static List<AdminOrderDto> mapToDtoList(List<AdminOrder> content) {
        return content.stream()
                .map(AdminOrderMapper::mapToAdminOrderDto)
                .toList();
    }

    private static AdminOrderDto mapToAdminOrderDto(AdminOrder adminOrder) {
        return AdminOrderDto.builder()
                .id(adminOrder.getId())
                .orderStatus(adminOrder.getOrderStatus())
                .placeDate(adminOrder.getPlaceDate())
                .grossValue(adminOrder.getGrossValue())
                .build();
    }
}
