package kz.app.appstore.dto.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {
    private String name;
    private Double price;
    private Long quantity;
    private String specificParams;
    private Boolean isHotProduct;
    private Boolean isDeleted;
    private MultipartFile[] images;
    private List<String> existingImageUrls;
    private List<String> deletedImageUrls;
}
