package kz.app.appstore.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.dto.cart.CartItemResponse;
import kz.app.appstore.dto.catalog.CatalogResponse;
import kz.app.appstore.dto.product.FavoriteProductResponse;
import kz.app.appstore.dto.product.ProductResponse;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.exception.InsufficientStockException;
import kz.app.appstore.service.CartService;
import kz.app.appstore.service.FavoriteService;
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

import java.nio.file.Files;
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
    private final FavoriteService favoriteService;

    public UserController(ProductService productService, CartService cartService, FavoriteService favoriteService) {
        this.productService = productService;
        this.cartService = cartService;
        this.favoriteService = favoriteService;
    }

    @Operation(summary = "Получение всех продуктов", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/all-products/get")
    public ResponseEntity<?> getAllProducts(
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

    @Operation(summary = "Получение hot-продукты", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/hot-products")
    public ResponseEntity<?> getHotProducts(
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

    @Operation(summary = "Получение продуктов по айди каталога", security = {@SecurityRequirement(name = "bearerAuth")})
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

    @Operation(summary = "Получение фото", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/images/{fileName}")
    public ResponseEntity<Resource> serveFile(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get("/data/uploads").resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Определение типа файла динамически
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Получение всех каталогов", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/categories")
    public List<CatalogResponse> getAllCatalogs() throws JsonProcessingException {
        return productService.getAllCatalogs();
    }

    @Operation(summary = "Получение всех под каталогов по айди каталога", security = {@SecurityRequirement(name = "bearerAuth")})
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

    @Operation(summary = "Добавить товар в корзинку", security = {@SecurityRequirement(name = "bearerAuth")})
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

    @Operation(summary = "Получение корзинок", security = {@SecurityRequirement(name = "bearerAuth")})
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

    @Operation(summary = "Добавление в избранное", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/add-favorite/{productId}")
    void addFavorite(@PathVariable Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        favoriteService.addFavorite(username, productId);
    }

    @Operation(summary = "Получить избранное", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/favorite-products")
    public ResponseEntity<?> getFavoriteProducts() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        try {
            FavoriteProductResponse favoriteProductResponse = favoriteService.getFavorites(username);
            return ResponseEntity.ok(favoriteProductResponse);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }
}
