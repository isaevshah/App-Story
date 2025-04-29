package kz.app.appstore.dto.paypal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayPalResponseDto {
    private String orderId;
    private String approvalUrl;
    private String status;
}
