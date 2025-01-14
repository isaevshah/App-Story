package kz.app.appstore.service.impl;

import jakarta.persistence.EntityNotFoundException;
import kz.app.appstore.entity.*;
import kz.app.appstore.exception.InsufficientStockException;
import kz.app.appstore.repository.CartItemRepository;
import kz.app.appstore.repository.CartRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ConcurrentModificationException;

@Service
@Slf4j
@Transactional
public class CartServiceImpl implements CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(UserRepository userRepository, ProductRepository productRepository,
                           CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public void addToCart(Long productId, String username, int quantity) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new EntityNotFoundException("Product not found: " + productId));

            Cart cart = getOrCreateCart(user);

            if (!checkProductQuantity(productId, quantity)) {
                throw new InsufficientStockException("Insufficient stock for product: " + productId);
            }

            updateOrCreateCartItem(cart, product, quantity);

            updateCartTotalPrice(cart);

            log.info("Added product {} to cart for user {}", productId, username);
        } catch (OptimisticLockingFailureException e) {
            log.warn("Concurrent modification detected while adding to cart. Retrying operation.");
            // Здесь можно реализовать механизм повторных попыток
            throw new ConcurrentModificationException("Unable to update cart due to concurrent modification");
        }
    }

    @Override
    public void removeFromCart(Long productId, String username, int quantity) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Cart cart = user.getCart();
        if (cart == null) {
            throw new EntityNotFoundException("Cart not found for user: " + username);
        }

        CartItemKey cartItemKey = new CartItemKey(cart.getId(), productId);
        cartItemRepository.findById(cartItemKey).ifPresent(cartItem -> {
            cart.getCartItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
            updateCartTotalPrice(cart);
        });
    }

    @Override
    public void clearCart(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        Cart cart = user.getCart();
        if (cart != null) {
            cart.getCartItems().clear();
            cart.setTotalPrice(0.0);
            cartRepository.save(cart);
        }
    }

    @Override
    public Cart getCartByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return user.getCart();
    }

    @Override
    public boolean checkProductQuantity(Long productId, int requestQuantity) {
        return productRepository.findById(productId)
                .map(product -> product.getQuantity() >= requestQuantity)
                .orElse(false);
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

        int newQuantity = cartItem.getQuantity() + quantity;
        if (newQuantity > product.getQuantity()) {
            throw new InsufficientStockException("Total requested quantity exceeds available stock");
        }

        cartItem.setQuantity(newQuantity);
        cartItem.setPrice(product.getPrice() * newQuantity);
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
}
