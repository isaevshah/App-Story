package kz.app.appstore.service.impl;

import kz.app.appstore.dto.product.CreateProductRequest;
import kz.app.appstore.dto.product.CreateProductResponse;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public CreateProductResponse createProduct(CreateProductRequest createProductRequest) {
        return null;
    }

    @Override
    public void deleteProduct(Long id) {

    }

    @Override
    public void updateProduct(Long id, CreateProductRequest createProductRequest) {

    }
}