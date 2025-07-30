package kz.app.appstore.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpVerificationDTO {
    @NotBlank
    private String email;
    @NotBlank
    private String otp;
}
