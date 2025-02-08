package kz.app.appstore.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreationResponseDto {
    private Long orderId;
    private Double totalPrice;
    private String status;
    private String paymentMethod;
}

