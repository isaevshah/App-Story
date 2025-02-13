package kz.app.appstore.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private LocalDateTime orderDate;
    private String status;
    private Double totalPrice;
    private String firstname;
    private String lastname;
    private String phoneNumber;
    private String city;
    private String country;
    private String point;
    private String username;
    private List<OrderItemDto> items;
}
