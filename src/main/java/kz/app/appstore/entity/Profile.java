package kz.app.appstore.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "profile")
@Getter
@Setter
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
    @JsonBackReference
    private User user;
}