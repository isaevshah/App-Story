package kz.app.appstore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.dto.cart.CartItemResponse;
import kz.app.appstore.entity.*;
import kz.app.appstore.repository.CartItemRepository;
import kz.app.appstore.repository.CartRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings("unchecked")
public class CartServiceImpl implements CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ObjectMapper objectMapper;

    public CartServiceImpl(UserRepository userRepository, ProductRepository productRepository,
                           CartRepository cartRepository, CartItemRepository cartItemRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void addToCart(Long productId, String username, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));
        Cart cart = getOrCreateCart(user);
        updateOrCreateCartItem(cart, product, quantity);
        updateCartTotalPrice(cart);
        log.info("Added product {} to cart for user {}", productId, username);
    }

    @Override
    public List<CartItemResponse> getCartList(String username) {
        Cart cart = getCartByUsername(username); // Получаем корзину пользователя
        List<CartItem> cartItems = cart.getCartItems(); // Получаем список товаров в корзине

        // Преобразуем каждый CartItem в CartItemResponse
        return cartItems.stream().map(cartItem -> {
            Product product = cartItem.getProduct();

            // Десериализация specificParams (если это JSON-строка)
            Map<String, Object> specificParams = new HashMap<>();
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                specificParams = objectMapper.readValue(product.getSpecificParams(), Map.class);
            } catch (Exception e) {
                // Логируем ошибку десериализации, если она произошла
                e.printStackTrace();
            }

            // Получение списка URL изображений
            List<String> imageUrls = product.getImages().stream()
                    .map(ProductImage::getImageUrl)
                    .collect(Collectors.toList());

            // Формирование CartItemResponse
            return new CartItemResponse(
                    product.getIndividualCode(),
                    product.getName(),
                    product.getPrice(),
                    cartItem.getPrice(), // Общая стоимость для данного товара
                    cartItem.getQuantity(), // Общее количество
                    product.getDescription(),
                    specificParams,
                    imageUrls,
                    product.getIsDeleted()
            );
        }).collect(Collectors.toList());
    }

    private void updateOrCreateCartItem(Cart cart, Product product, int quantity) {
        CartItem cartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            cartItem = new CartItem();
            cartItem.setId(new CartItemKey(cart.getId(), product.getId()));
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(0);
            cartItem.setPrice(0.0);
            cart.getCartItems().add(cartItem);
        }

        cartItem.setQuantity(quantity);
        cartItem.setPrice(product.getPrice() * quantity);
        cartItemRepository.save(cartItem);
    }

    private void updateCartTotalPrice(Cart cart) {
        double totalPrice = cart.getCartItems().stream()
                .mapToDouble(CartItem::getPrice)
                .sum();
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCart(User user) {
        if (user.getCart() == null) {
            Cart newCart = new Cart();
            newCart.setUser(user);
            newCart.setTotalPrice(0.0);
            newCart = cartRepository.save(newCart);
            user.setCart(newCart);
            userRepository.save(user);
            return newCart;
        }
        return user.getCart();
    }

    private Cart getCartByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return Optional.ofNullable(user.getCart())
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + username));
    }

}
