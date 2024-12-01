package kz.app.appstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.app.appstore.dto.catalog.CreateCatalogRequest;
import kz.app.appstore.dto.catalog.CreateProductRequest;
import kz.app.appstore.dto.catalog.ProductResponse;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ProductService {
    Catalog createCatalog(CreateCatalogRequest catalogRequest);
    Catalog createUnderCatalog(Long id,CreateCatalogRequest catalogRequest);
    void deleteCatalog(Long id);
    List<Catalog> getAllCatalogs() throws JsonProcessingException;
    List<Catalog> getAllCatalogsByParentId(Long parentCatalogId) throws JsonProcessingException;
    Product createProduct(Long catalogId, CreateProductRequest requestData) throws IOException;
    Page<ProductResponse> getProductsByCatalogId(Long catalogId, int page, int size, String sortBy, String sortDir) throws JsonProcessingException;
}
