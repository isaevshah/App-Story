package kz.app.appstore.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<ProductSimpleDto> matchedProducts;
    private List<CatalogSimpleDto> matchedCatalogs;
    private ProductSimpleDto productByCode; // может быть null
}
