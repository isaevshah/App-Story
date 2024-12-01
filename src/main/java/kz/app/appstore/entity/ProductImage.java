package kz.app.appstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "product_images") // Avoid using reserved keywords
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    @Id
    @GeneratedValue
    private Long id;
    private String imageUrl; // Путь к изображению на сервере или URL
    @ManyToOne
    private Product product;
}
