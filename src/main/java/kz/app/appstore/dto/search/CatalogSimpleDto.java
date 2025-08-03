package kz.app.appstore.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogSimpleDto {
    private Long id;
    private String name;
    private String nameInEnglish;
}
