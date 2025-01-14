package kz.app.appstore.service.impl;

import kz.app.appstore.entity.Cart;
import kz.app.appstore.entity.Product;
import kz.app.appstore.entity.User;
import kz.app.appstore.repository.CartItemRepository;
import kz.app.appstore.repository.CartRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.CartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    public CartServiceImpl(UserRepository userRepository, ProductRepository productRepository, CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }

    @Override
    public void addToCart(Long productId, String username, Long quantity) {

    }
}
