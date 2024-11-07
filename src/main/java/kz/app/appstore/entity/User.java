package kz.app.appstore.entity;

import jakarta.persistence.*;
import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    // Роль пользователя: ADMIN, MANAGER, WAREHOUSE_WORKER, CUSTOMER
    @Enumerated(EnumType.STRING)
    private Role role;
    // Тип пользователя: JURIDICAL (Юр-лицо), PHYSICAL (Физ-лицо)
    @Enumerated(EnumType.STRING)
    private UserType userType;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Profile profile;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;
    @OneToMany(mappedBy = "user")
    private List<Order> orders;
    @OneToMany(mappedBy = "user")
    private List<PurchaseRequest> purchaseRequests;
    private boolean isActive;

}