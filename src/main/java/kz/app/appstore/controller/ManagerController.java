package kz.app.appstore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import kz.app.appstore.dto.catalog.CreateCatalogRequest;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Product;
import kz.app.appstore.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize(value = "hasAnyRole('MANAGER', 'ADMIN')")
public class ManagerController {
    private final ProductService productService;

    public ManagerController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/catalogs/create")
    public ResponseEntity<Catalog> createCatalog(@RequestBody CreateCatalogRequest request) {
        return ResponseEntity.ok(productService.createCatalog(request));
    }

    @PostMapping("/under-catalogs/{parentCatalogId}/create")
    public ResponseEntity<Catalog> createUnderCatalog(@PathVariable Long parentCatalogId, @RequestBody CreateCatalogRequest request) {
        return ResponseEntity.ok(productService.createUnderCatalog(parentCatalogId, request));
    }

    @DeleteMapping("/catalogs/{catalogId}/delete")
    public ResponseEntity<Void> deleteCatalog(@PathVariable Long catalogId) {
        productService.deleteCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/catalogs/get")
    public List<Catalog> getAllCatalogs() throws JsonProcessingException {
        return productService.getAllCatalogs();
    }

    @GetMapping("/catalogs/{parentCatalogId}/get")
    public List<Catalog> getCatalogsByParentId(@PathVariable Long parentCatalogId) throws JsonProcessingException {
        return productService.getAllCatalogsByParentId(parentCatalogId);
    }

    @PostMapping("/catalogs/{catalogId}/products/create")
    public ResponseEntity<Product> createProduct(@PathVariable Long catalogId, @RequestBody Map<String, Object> requestData) {
        try {
            Product product = productService.createProduct(catalogId, requestData);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }



}
