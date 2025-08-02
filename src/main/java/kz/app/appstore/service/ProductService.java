package kz.app.appstore.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.app.appstore.dto.catalog.*;
import kz.app.appstore.dto.product.CreateProductRequest;
import kz.app.appstore.dto.product.ProductResponse;
import kz.app.appstore.dto.product.ProductResponseDTO;
import kz.app.appstore.dto.product.UpdateProductRequest;
import kz.app.appstore.dto.search.SearchResponse;
import kz.app.appstore.exception.ProductCreationException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    CatalogResponse createCatalog(CreateCatalogRequest catalogRequest, String username);

    CatalogResponse createUnderCatalog(Long id, CreateCatalogRequest catalogRequest, String username);

    void deleteCatalog(Long id);

    List<CatalogResponse> getAllCatalogs() throws JsonProcessingException;

    List<CatalogResponse> getAllCatalogsByParentId(Long parentCatalogId) throws JsonProcessingException;

    ProductResponseDTO createProduct(Long catalogId, CreateProductRequest request, String username) throws ProductCreationException;

    Page<ProductResponse> getProductsByCatalogId(Long catalogId, int page, int size, String sortBy, String sortDir) throws JsonProcessingException;

    ProductResponse getProductById(String username, Long id);

    Page<ProductResponse> getAllProducts(int page, int size, String sortBy, String sortDir) throws JsonProcessingException;

    Page<ProductResponse> getAllHotProducts(int page, int size) throws JsonProcessingException;

    void updateProduct(Long productId, UpdateProductRequest request, String username) throws ProductCreationException;

    ProductResponse getProductDetails(Long productId);

    void updateCatalog(Long catalogId, CreateCatalogRequest catalogRequest, String username);

    void deleteProduct(Long productId);

    SearchResponse searchAll(String query);
}
