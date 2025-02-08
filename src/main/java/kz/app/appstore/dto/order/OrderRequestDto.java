package kz.app.appstore.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private String firstname;
    private String lastname;
    private String companyName;
    private String paymentMethod; // KASPI_QR, CARD, CASH
    private String phoneNumber;
    private String country;
    private String city;
    private String point;
    private Double totalPrice;
    private List<OrderItemRequestDto> items;
}
