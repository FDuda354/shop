package pl.dudios.shop.admin.order.model;

import lombok.Getter;
import pl.dudios.shop.admin.product.model.AdminProduct;
import pl.dudios.shop.order.model.Shipment;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "order_rows")
@Getter
public class AdminOrderRow {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long orderId;
    @OneToOne
    @JoinColumn(name = "productId")
    private AdminProduct product;
    private int quantity;
    private BigDecimal price;
    @OneToOne
    @JoinColumn(name = "shipmentId")
    private Shipment shipment;
}
