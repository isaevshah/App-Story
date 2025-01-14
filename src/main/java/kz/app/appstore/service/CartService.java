package kz.app.appstore.service;

public interface CartService {
    void addToCart(Long productId, String username, Long quantity);
}
