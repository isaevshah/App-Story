package kz.app.appstore.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminUserCreationDTO {
    @NotBlank
    @Size(min = 4, max = 20)
    private String username;
    @NotBlank
    @Size(min = 6)
    private String password;
    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{10,13}$", message = "Invalid phone number")
    private String phoneNumber;
    private String firstName;
    private String lastName;
    private Boolean isManager = false;
    private Boolean isWorker = false;
}
