package kz.app.appstore.entity;

import jakarta.persistence.*;
import kz.app.appstore.enums.PurchaseRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date requestDate;

    @Enumerated(EnumType.STRING)
    private PurchaseRequestStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Может быть null, если товар отсутствует в каталоге
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    private String requestedProductName; // Если товар отсутствует в каталоге

}