package kz.app.appstore.dto.paypal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPalRequestDto {
    private String currencyCode;
    private String value;
    private String returnUrl;
    private String cancelUrl;
}
