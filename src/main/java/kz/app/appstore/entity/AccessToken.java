package kz.app.appstore.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Data
public class AccessToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Date expiresAt;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}