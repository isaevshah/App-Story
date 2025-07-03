package kz.app.appstore.dto.qr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithQrDto {
    private Long orderId;
    private List<OrderItemQrDto> orderItems;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemQrDto {
        private Long productId;
        private String qrCodeBase64;
    }
}