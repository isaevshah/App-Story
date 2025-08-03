package kz.app.appstore.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String categoryName;
    private String individualCode;
    private String name;
    private String nameInEnglish;
    private Double price;
    private Long quantity;
    private String description;
    private Boolean isHotProduct;
    private Map<String, Object> specificParams;
    private List<String> imageUrls;
    private Boolean isDeleted;
    private Boolean inCart;
    private Boolean isFavorite;
}
