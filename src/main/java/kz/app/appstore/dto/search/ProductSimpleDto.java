package kz.app.appstore.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductSimpleDto {
    private Long id;
    private String name;
    private Double price;
    private String description;
    private String imageUrl;
}
