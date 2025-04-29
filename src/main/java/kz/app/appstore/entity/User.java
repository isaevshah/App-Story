package kz.app.appstore.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users") // Avoid using reserved keywords
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime registrationAt;
    private LocalDateTime updatedAt;
    @Enumerated(EnumType.STRING)
    private Role role;
    @Enumerated(EnumType.STRING)
    private UserType userType;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    @JsonManagedReference
    private Profile profile;
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Order> orders;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<PurchaseRequest> purchaseRequests;
    private boolean isActive;
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Favorite> favorites;
}