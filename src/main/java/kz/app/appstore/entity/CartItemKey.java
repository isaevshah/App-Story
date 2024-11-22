package kz.app.appstore.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "cart_item_key")
public class CartItemKey implements Serializable {
    @Column(name = "cart_id")
    private Long cartId;
    @Column(name = "product_id")
    private Long productId;
    // hashCode и equals методы
}