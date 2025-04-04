package kz.app.appstore.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {
    private String name;
    private Double price;
    private String description;
    private Long quantity;
    private String specificParams;
    private Boolean isHotProduct = false;
    private MultipartFile[] images;
}
