package pl.dudios.shop.admin.product.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
@Getter
@Setter
@Builder
public class AdminProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String fullDescription;
    // Wersja angielska (opcjonalna) — null/pusty = frontend pokazuje polską.
    private String nameEn;
    private String descriptionEn;
    private String fullDescriptionEn;
    private Long categoryId;
    private BigDecimal price;
    @Enumerated(EnumType.STRING)
    private AdminProductCurrency currency;
    private String image;
    private String slug;

}
