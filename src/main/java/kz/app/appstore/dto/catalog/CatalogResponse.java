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
    private String nameInEnglish;
    private String description;
    private ParentCatalogResponse parentCatalog;
    private List<CatalogResponse> subCatalogs;
    private String imageName;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CatalogResponse that = (CatalogResponse) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}