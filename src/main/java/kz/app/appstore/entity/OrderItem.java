package kz.app.appstore.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "order_item")
public class OrderItem {
    @EmbeddedId
    private OrderItemKey id;
    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id")
    private Order order;
    @ManyToOne
    @MapsId("productId")
    @JoinColumn(name = "product_id")
    private Product product;
    private Integer quantity;
    private Double price;
}