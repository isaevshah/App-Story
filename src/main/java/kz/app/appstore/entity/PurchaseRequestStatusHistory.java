package kz.app.appstore.entity;

import jakarta.persistence.*;
import kz.app.appstore.enums.PurchaseRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "purchase_request_status_history") // Avoid using reserved keywords
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequestStatusHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Date changeDate; // Дата изменения статуса

    @Enumerated(EnumType.STRING)
    private PurchaseRequestStatus oldStatus; // Предыдущий статус

    @Enumerated(EnumType.STRING)
    private PurchaseRequestStatus newStatus; // Новый статус

    @ManyToOne
    @JoinColumn(name = "purchase_request_id")
    private PurchaseRequest purchaseRequest; // Связь с запросом на покупку

    @ManyToOne
    @JoinColumn(name = "changed_by")
    private User changedBy; // Пользователь, изменивший статус (обычно менеджер)
}

