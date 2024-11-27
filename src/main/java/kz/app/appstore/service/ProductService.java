package kz.app.appstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.app.appstore.dto.catalog.CreateCatalogRequest;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Product;

import java.util.List;
import java.util.Map;

public interface ProductService {
    Catalog createCatalog(CreateCatalogRequest catalogRequest);
    Catalog createUnderCatalog(Long id,CreateCatalogRequest catalogRequest);
    void deleteCatalog(Long id);
    List<Catalog> getAllCatalogs() throws JsonProcessingException;
    List<Catalog> getAllCatalogsByParentId(Long parentCatalogId) throws JsonProcessingException;
    Product createProduct(Long catalogId, Map<String, Object> requestData) throws JsonProcessingException;
}
