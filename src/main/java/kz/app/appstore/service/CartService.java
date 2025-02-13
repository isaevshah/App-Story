package kz.app.appstore.service;

import kz.app.appstore.dto.cart.CartItemResponse;

import java.util.List;

public interface CartService {
    void addToCart(Long productId, String username, int quantity);
    void removeFromCart(Long productId, String username);
    List<CartItemResponse> getCartList(String username);
}
