package kz.app.appstore.service;

import kz.app.appstore.dto.product.FavoriteProductResponse;

import java.util.List;

public interface FavoriteService {
    void addFavorite(String username, Long productId);
    FavoriteProductResponse getFavorites(String username);
}