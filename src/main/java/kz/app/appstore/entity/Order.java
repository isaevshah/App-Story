package kz.app.appstore.entity;
import jakarta.persistence.*;
import kz.app.appstore.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders") // "Order" может быть зарезервированным словом в SQL
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime orderDate;
    @Enumerated(EnumType.STRING)
    private PaymentStatus payStatus;
    private Double totalPrice;
    private String firstname;
    private String lastname;
    private String country;
    private String city;
    private String phoneNumber;
    private String point;
    private String orderCode;
    private String trackStatus;
    private LocalDateTime createDate;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    @Column(name = "kaspi_check_path")
    private String kaspiCheckPath;
}