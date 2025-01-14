package kz.app.appstore.dto.cart;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponse {
    private String individualCode;
    private String name;
    private Double price;
    private Double totalPrice;
    private int totalQuantity;
    private String description;
    private Map<String, Object> specificParams;
    private List<String> imageUrls;
}
