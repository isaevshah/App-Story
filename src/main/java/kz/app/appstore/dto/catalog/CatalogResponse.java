package kz.app.appstore.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponse {
    private Long id;
    private String name;
    private String description;
    private ParentCatalogResponse parentCatalog;
}