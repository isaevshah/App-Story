package kz.app.appstore.service;

import kz.app.appstore.dto.cart.CartItemResponse;
import kz.app.appstore.entity.Cart;

import java.util.List;

public interface CartService {
    void addToCart(Long productId, String username, int quantity);
    void removeFromCart(Long productId, String username, int quantity);
    void clearCart(String username);
    boolean checkProductQuantity(Long productId, int requestQuantity);
    List<CartItemResponse> getCartList(String username);
}
