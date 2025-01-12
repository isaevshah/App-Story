package kz.app.appstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.app.appstore.dto.catalog.*;
import kz.app.appstore.exception.ProductCreationException;
import org.springframework.data.domain.Page;
import java.util.List;

public interface ProductService {
    CatalogResponse createCatalog(CreateCatalogRequest catalogRequest);
    CatalogResponse createUnderCatalog(Long id, CreateCatalogRequest catalogRequest);
    void deleteCatalog(Long id);
    List<CatalogResponse> getAllCatalogs() throws JsonProcessingException;
    List<CatalogResponse> getAllCatalogsByParentId(Long parentCatalogId) throws JsonProcessingException;
    ProductResponseDTO createProduct(Long catalogId, CreateProductRequest request) throws ProductCreationException;
    Page<ProductResponse> getProductsByCatalogId(Long catalogId, int page, int size, String sortBy, String sortDir) throws JsonProcessingException;
    List<ProductResponse> getLikedProducts();
}
