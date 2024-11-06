package kz.app.appstore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Profile {
    @Id
    private Long id; // Используем тот же ID, что и в User

    private String phoneNumber;
    private String email;

    // Поля для Юр-лица
    private String firstName;
    private String lastName;
    private String BIN; // Бизнес-идентификационный номер
    private String companyName;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;
}