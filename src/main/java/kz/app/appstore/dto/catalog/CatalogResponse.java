package kz.app.appstore.dto.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogResponse {
    private Long id;
    private String name;
    private String description;
    private ParentCatalogResponse parentCatalog;
    private List<CatalogResponse> subCatalogs;
    private String imageBase64;
}