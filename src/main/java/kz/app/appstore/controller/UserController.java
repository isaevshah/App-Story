package kz.app.appstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.dto.cart.CartItemResponse;
import kz.app.appstore.dto.order.OrderRequestDto;
import kz.app.appstore.dto.order.OrderResponseDto;
import kz.app.appstore.dto.product.FavoriteProductResponse;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.dto.user.UserInfoDto;
import kz.app.appstore.dto.user.UserUpdateDto;
import kz.app.appstore.exception.InsufficientStockException;
import kz.app.appstore.service.CartService;
import kz.app.appstore.service.CreateOrderService;
import kz.app.appstore.service.FavoriteService;
import kz.app.appstore.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Slf4j
@PreAuthorize(value = "hasAnyRole('ADMIN', 'CUSTOMER')")
@RequiredArgsConstructor
public class UserController {
    private final CartService cartService;
    private final FavoriteService favoriteService;
    private final UserService userService;
    private final CreateOrderService createOrderService;

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

    @Operation(summary = "Обновить количество товара в корзине", security = {@SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/cart/cart-item/{productId}/{quantity}/update")
    public ResponseEntity<?> updateCartItem(@PathVariable Long productId, @PathVariable int quantity) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        try {
            cartService.updateCartItem(productId, username, quantity);
            return ResponseEntity.ok("Product quantity updated successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (InsufficientStockException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    @Operation(summary = "Удалить товар из корзины", security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/cart/cart-item/{productId}/delete")
    public ResponseEntity<?> deleteCartItem(@PathVariable Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        try {
            cartService.removeFromCart(productId, username);
            return ResponseEntity.ok("Product removed from cart successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
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

    @Operation(summary = "Удаление из избранного", security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/remove-favorite/{productId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        try {
            favoriteService.removeFavorite(username, productId);
            return ResponseEntity.ok("Product removed from favorites successfully");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Детали пользователя", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/user-info")
    public UserInfoDto getUserInfo() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userService.getUserInfo(username);
    }

    @Operation(summary = "Заказы пользователя", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/user-orders")
    public List<OrderResponseDto> getUserOrders() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        return userService.getUserOrders(username);
    }

    @Operation(summary = "Заказы пользователя", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/user-order/{id}")
    public OrderResponseDto getUserOrderById(@PathVariable Long id) {
        return userService.getUserOrderById(id);
    }

    @Operation(summary = "Создние товара", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping(value = "/create-order", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void createOrder(@RequestPart("data") OrderRequestDto request,
                            @RequestPart("file") MultipartFile file) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            createOrderService.saveKaspiCheck(request, username, file);
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @Operation(summary = "Обновить профил", security = {@SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/update-profile")
    public void updateProfile(@RequestBody UserUpdateDto requestDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        userService.updateProfile(requestDto, username);
    }
}