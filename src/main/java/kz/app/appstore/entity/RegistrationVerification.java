package kz.app.appstore.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registr_verification") // Avoid using reserved keywords
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationVerification {
    @Id
    private String email;
    private String otp;
    private LocalDateTime expiresAt;

    @Lob
    private String payloadJson; // сериализованные регистрационные данные

    private boolean verified;
}