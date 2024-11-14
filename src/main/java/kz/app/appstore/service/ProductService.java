package kz.app.appstore.service;

import kz.app.appstore.dto.product.CreateProductRequest;
import kz.app.appstore.dto.product.CreateProductResponse;

public interface ProductService {
    CreateProductResponse createProduct(CreateProductRequest createProductRequest);
    void deleteProduct(Long id);
    void updateProduct(Long id, CreateProductRequest createProductRequest);
}
