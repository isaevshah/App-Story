package kz.app.appstore.dto.paypal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPalCaptureResponseDto {
    private String orderId;
    private String status;
}
