package kz.app.appstore.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import kz.app.appstore.dto.catalog.*;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.exception.ProductCreationException;
import kz.app.appstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/manager")
@PreAuthorize(value = "hasAnyRole('MANAGER', 'ADMIN')")
@Slf4j
public class ManagerController {
    private final ProductService productService;
    private final ObjectMapper objectMapper;

    public ManagerController(ProductService productService, ObjectMapper objectMapper) {
        this.productService = productService;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/catalogs/create")
    public ResponseEntity<CatalogResponse> createCatalog(@RequestBody CreateCatalogRequest request) {
        log.info("Got createCatalog request {}", objectMapper.valueToTree(request));
        CatalogResponse catalogResponse = productService.createCatalog(request);
        log.info("Got createCatalog response {}", objectMapper.valueToTree(catalogResponse));
        return ResponseEntity.ok(catalogResponse);
    }

    @PostMapping("/under-catalogs/{parentCatalogId}/create")
    public ResponseEntity<CatalogResponse> createUnderCatalog(@PathVariable Long parentCatalogId, @RequestBody CreateCatalogRequest request) {
        return ResponseEntity.ok(productService.createUnderCatalog(parentCatalogId, request));
    }

    @DeleteMapping("/catalogs/{catalogId}/delete")
    public ResponseEntity<Void> deleteCatalog(@PathVariable Long catalogId) {
        productService.deleteCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/catalogs/{catalogId}/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @PathVariable Long catalogId,
            @ModelAttribute CreateProductRequest request
    ) {
        try {
            ProductResponseDTO product = productService.createProduct(catalogId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (ProductCreationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/all-products/get")
    public ResponseEntity<?> getProductsByCatalogId(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "asc", required = false) String sortDir
    ) {
        try {
            Page<ProductResponse> products = productService.getAllProducts(page, size, sortBy, sortDir);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }
}