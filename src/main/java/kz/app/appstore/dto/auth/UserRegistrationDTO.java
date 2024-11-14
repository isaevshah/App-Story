package kz.app.appstore.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import kz.app.appstore.enums.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationDTO {
    // Общие поля для всех пользователей
    @NotBlank
    @Size(min = 4, max = 20)
    private String username;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Invalid phone number")
    private String phoneNumber;
    @NotBlank
    private UserType userType; // Тип пользователя: Физическое или Юридическое лицо
    // Поля для Юридического лица
    private String firstName; // Имя (только для Юр-лица)
    private String lastName; // Фамилия (только для Юр-лица)
    private String bin; // БИН (только для Юр-лица)
    private String companyName; // Наименование компании (только для Юр-лица)
}