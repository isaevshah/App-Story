package kz.app.appstore.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.dto.product.FavoriteProductResponse;
import kz.app.appstore.dto.product.ProductResponse;
import kz.app.appstore.entity.Favorite;
import kz.app.appstore.entity.Product;
import kz.app.appstore.entity.ProductImage;
import kz.app.appstore.entity.User;
import kz.app.appstore.repository.CartItemRepository;
import kz.app.appstore.repository.FavoriteRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.FavoriteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartItemRepository cartItemRepository;

    public FavoriteServiceImpl(FavoriteRepository favoriteRepository, ProductRepository productRepository, UserRepository userRepository, CartItemRepository cartItemRepository) {
        this.favoriteRepository = favoriteRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public void addFavorite(String username, Long productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));
        if (favoriteRepository.existsByUserIdAndProductId(user.getId(), productId)) {
            throw new IllegalArgumentException("Product is already in the user's favorites.");
        }

        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setProduct(product);
        favoriteRepository.save(favorite);
    }

    @Override
    public FavoriteProductResponse getFavorites(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        List<Favorite> favorites = favoriteRepository.findAllByUserId(user.getId());
        FavoriteProductResponse totalProducts = new FavoriteProductResponse();
        List<ProductResponse> productResponses = new ArrayList<>();
        for (Favorite favorite : favorites) {
            Product product = productRepository.getReferenceById(favorite.getProduct().getId());
            ProductResponse productResponse = convertToProductResponse(product);
            productResponses.add(productResponse);
        }
        totalProducts.setProductResponse(productResponses);
        return totalProducts;
    }

    @Override
    @Transactional
    public void removeFavorite(String username, Long productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));

        Favorite favorite = favoriteRepository.findByUserIdAndProductId(user.getId(), productId)
                .orElseThrow(() -> new EntityNotFoundException("Product is not in the user's favorites."));

        favoriteRepository.delete(favorite);
    }


    private ProductResponse convertToProductResponse(Product product) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> specificParams = new HashMap<>();
        try {
            specificParams = objectMapper.readValue(product.getSpecificParams(), Map.class);
        } catch (JsonProcessingException e) {
            log.error("Ошибка при разборе JSON для specificParams у продукта с ID: {}", product.getId(), e);
        }
        List<String> imageUrls = product.getImages().stream()
                .map(ProductImage::getImageUrl) // Генерация полного URL
                .collect(Collectors.toList());

        return new ProductResponse(
                product.getId(),
                product.getCatalog().getName(),
                product.getIndividualCode(),
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getDescription(),
                product.getIsHotProduct(),
                specificParams,
                imageUrls,
                product.getIsDeleted(),
                exist(product.getId()).get("inCart"),
                exist(product.getId()).get("isFavorite")
        );
    }

    public Map<String, Boolean> exist(Long productId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Boolean inCart = cartItemRepository.existsByCartIdAndProductId(user.getId(), productId);
        Boolean isFavorite = favoriteRepository.existsByUserIdAndProductId(user.getId(), productId);
        Map<String, Boolean> exist = new HashMap<>();
        exist.put("inCart", inCart);
        exist.put("isFavorite", isFavorite);
        return exist;
    }
}
