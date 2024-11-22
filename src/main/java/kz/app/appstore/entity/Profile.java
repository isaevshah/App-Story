package kz.app.appstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "profile") // Avoid using reserved keywords
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    private Long id;
    private String phoneNumber;
    private String firstName;
    private String lastName;
    // Поля для Юр-лица
    private String bin;
    private String companyName;
    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}