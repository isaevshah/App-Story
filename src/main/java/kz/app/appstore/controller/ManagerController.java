package kz.app.appstore.controller;

import kz.app.appstore.dto.product.CreateProductRequest;
import kz.app.appstore.dto.product.CreateProductResponse;
import kz.app.appstore.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize(value = "hasRole('MANAGER')")
public class ManagerController {
    private final ProductService productService;

    public ManagerController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/create-product")
    public CreateProductResponse createProduct(@RequestBody CreateProductRequest product) {
        return productService.createProduct(product);
    }

    @PostMapping("/update-product/{id}")
    public ResponseEntity<Void> updateProduct(@PathVariable Long id, @RequestBody CreateProductRequest product) {
        productService.updateProduct(id, product);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-product/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
