package kz.app.appstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.app.appstore.dto.catalog.CreateCatalogRequest;
import kz.app.appstore.entity.Catalog;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ProductService {
    Catalog createCatalog(CreateCatalogRequest catalogRequest);
    Catalog createUnderCatalog(Long id,CreateCatalogRequest catalogRequest);
    void deleteCatalog(Long id);
    List<Catalog> getAllCatalogs() throws JsonProcessingException;
    List<Catalog> getAllCatalogsByParentId(Long parentCatalogId) throws JsonProcessingException;
}
