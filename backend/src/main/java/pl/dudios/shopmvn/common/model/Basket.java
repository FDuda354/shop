package pl.dudios.shopmvn.common.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "baskets")
public class Basket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime created;
    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "basketId")
    private List<BasketItem> items;

    //TODO: Upgrade this 1

    public Basket addProduct(BasketItem item) {
        if (items == null) {
            items = new ArrayList<>();
        }
        items.stream()
                .filter(basketItem -> Objects.equals(basketItem.getProduct().getId(), item.getProduct().getId()))
                .findFirst()
                .ifPresentOrElse(basketItem ->
                                basketItem.setQuantity(basketItem.getQuantity() + item.getQuantity()),
                        () -> items.add(item));

        return this;
    }
}
