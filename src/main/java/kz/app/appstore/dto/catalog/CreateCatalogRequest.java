package kz.app.appstore.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateCatalogRequest {
    private String name;
    private String description;
    private MultipartFile image;
}
