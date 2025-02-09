package kz.app.appstore.dto.order;

import kz.app.appstore.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private String firstname;
    private String lastname;
    private String paymentMethod; // KASPI_QR, CARD, CASH
    private String phoneNumber;
    private String country;
    private String city;
    private String point;
    private Double totalPrice;
    private List<OrderItemRequestDto> items;
    private User user;
}
