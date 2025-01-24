package kz.app.appstore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.dto.cart.CartItemResponse;
import kz.app.appstore.dto.catalog.CatalogResponse;
import kz.app.appstore.dto.product.ProductResponse;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.exception.InsufficientStockException;
import kz.app.appstore.service.CartService;
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
import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
@PreAuthorize(value = "hasAnyRole('MANAGER', 'ADMIN', 'CUSTOMER')")
public class UserController {
    private final ProductService productService;
    private final CartService cartService;

    public UserController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/all-products/get")
    public ResponseEntity<?> getProductsByCatalogId(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "50", required = false) int size,
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

    @GetMapping("/hot-products")
    public ResponseEntity<?> getProductsByCatalogId(
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size
    ) {
        try {
            Page<ProductResponse> products = productService.getAllHotProducts(page, size);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }
    @GetMapping("/catalogs/{catalogId}/products")
    public ResponseEntity<?> getProductsByCatalogId(
            @PathVariable Long catalogId,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size,
            @RequestParam(defaultValue = "id", required = false) String sortBy,
            @RequestParam(defaultValue = "asc", required = false) String sortDir
    ) {
        try {
            Page<ProductResponse> products = productService.getProductsByCatalogId(catalogId, page, size, sortBy, sortDir);
            return ResponseEntity.ok(products);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @GetMapping("/images/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("/data/uploads").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.IMAGE_JPEG)
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/catalogs/get")
    public List<CatalogResponse> getAllCatalogs() throws JsonProcessingException {
        return productService.getAllCatalogs();
    }

    @GetMapping("/catalogs/{parentCatalogId}/get")
    public ResponseEntity<?> getCatalogsByParentId(@PathVariable Long parentCatalogId) {
        try {
            List<CatalogResponse> catalogs = productService.getAllCatalogsByParentId(parentCatalogId);
            return ResponseEntity.ok(catalogs);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @PostMapping("/cart/cart-item/{productId}/{quantity}/create")
    public ResponseEntity<?> createCartItem(@PathVariable Long productId, @PathVariable int quantity) {
        log.info("Got productId {}", productId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        try {
            cartService.addToCart(productId, username, quantity);
            return ResponseEntity.ok("Product added to cart successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @GetMapping("/cart/by-session/get")
    public ResponseEntity<?> getCartBySession() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        List<CartItemResponse> cart = cartService.getCartList(username);
        try {
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }


    @GetMapping("/liked-products/get")
    public ProductResponse getLikedProducts() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        return new ProductResponse();
    }
}
