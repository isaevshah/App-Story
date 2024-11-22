package kz.app.appstore.entity;

import jakarta.persistence.*;
import kz.app.appstore.enums.PurchaseRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "purchase_request") // Avoid using reserved keywords
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

    // Если товар отсутствует в каталоге, product будет null
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = true)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "manager_id", nullable = true)
    private User processedBy; // Менеджер, обработавший запрос

    private String requestedProductName; // Название запрашиваемого товара, если его нет в каталоге

    private String comments; // Дополнительные комментарии

    // Поле для хранения ответа менеджера
    private String managerResponse;
}