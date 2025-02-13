package kz.app.appstore.service;

import kz.app.appstore.dto.product.FavoriteProductResponse;

public interface FavoriteService {
    void addFavorite(String username, Long productId);
    FavoriteProductResponse getFavorites(String username);
    void removeFavorite(String username, Long productId);
}