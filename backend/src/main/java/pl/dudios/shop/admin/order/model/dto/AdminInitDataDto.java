package pl.dudios.shop.admin.order.model.dto;

import java.util.Map;

public record AdminInitDataDto(Map<String, String> orderStatuses) {
}
