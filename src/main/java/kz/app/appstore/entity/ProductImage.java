package kz.app.appstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue
    private Long id;
    private String imageUrl;
    @ManyToOne
    private Product product;

    public void setProduct(Product product) {
        if (this.product != null) {
            this.product.getImages().remove(this);
        }
        this.product = product;
        if (product != null) {
            product.getImages().add(this);
        }
    }
}
