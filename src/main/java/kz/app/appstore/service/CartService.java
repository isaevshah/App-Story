package kz.app.appstore.service;

import kz.app.appstore.entity.Cart;

public interface CartService {
    void addToCart(Long productId, String username, int quantity);
    void removeFromCart(Long productId, String username, int quantity);
    void clearCart(String username);
    Cart getCartByUsername(String username);
    boolean checkProductQuantity(Long productId, int requestQuantity);
}
