package kz.app.appstore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.dto.catalog.*;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.dto.product.CreateProductRequest;
import kz.app.appstore.dto.product.ProductResponse;
import kz.app.appstore.dto.product.ProductResponseDTO;
import kz.app.appstore.dto.product.UpdateProductRequest;
import kz.app.appstore.exception.ProductCreationException;
import kz.app.appstore.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Operation(summary = "Создание каталога", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(value = "/catalogs/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CatalogResponse> createCatalog(@ModelAttribute CreateCatalogRequest request) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        CatalogResponse catalogResponse = productService.createCatalog(request, username);
        log.info("Got createCatalog response {}", objectMapper.writeValueAsString(catalogResponse));
        return ResponseEntity.ok(catalogResponse);
    }

    @Operation(summary = "Создание под каталога", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/under-catalogs/{parentCatalogId}/create")
    public ResponseEntity<CatalogResponse> createUnderCatalog(@PathVariable Long parentCatalogId, @ModelAttribute CreateCatalogRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return ResponseEntity.ok(productService.createUnderCatalog(parentCatalogId, request, username));
    }

    @Operation(summary = "Удаление каталога", security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/catalogs/{catalogId}/delete")
    public ResponseEntity<Void> deleteCatalog(@PathVariable Long catalogId) {
        productService.deleteCatalog(catalogId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Создание продукта", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(value = "/catalogs/{catalogId}/product-create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createProduct(
            @PathVariable Long catalogId,
            @ModelAttribute CreateProductRequest request
    ) throws JsonProcessingException {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            ProductResponseDTO product = productService.createProduct(catalogId, request, username);
            log.info("Got createProduct response {}", objectMapper.writeValueAsString(product));
            return ResponseEntity.status(HttpStatus.CREATED).body(product);
        } catch (ProductCreationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(e.getMessage()));
        }
    }

    @Operation(summary = "Обновление продукта", security = {@SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/product/{productId}/update")
    public ResponseEntity<?> updateProduct(@PathVariable Long productId, @ModelAttribute UpdateProductRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            productService.updateProduct(productId, request, username);
            return ResponseEntity.ok("Product updated successfully");
        } catch (EntityNotFoundException | ProductCreationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Product not found");
        }
    }

    @Operation(summary = "Получение детали продукта по айди", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/product-details/{productId}/get")
    public ResponseEntity<?> getProductDetailsByProductId(@PathVariable Long productId) {
        try {
            ProductResponse productResponse = productService.getProductDetails(productId);
            return ResponseEntity.ok(productResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @Operation(summary = "Обновление каталога", security = {@SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/catalog/{catalogId}/update")
    public ResponseEntity<?> updateCatalog(@PathVariable Long catalogId, @ModelAttribute CreateCatalogRequest request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            productService.updateCatalog(catalogId, request, username);
            return ResponseEntity.ok("Catalog updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @Operation(summary = "Удаление продукта", security = {@SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/delete-product/{productId}")
    public ResponseEntity<?> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.ok("Product successfully deleted");
    }

}