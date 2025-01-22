package kz.app.appstore.repository;

import kz.app.appstore.entity.CartItem;
import kz.app.appstore.entity.CartItemKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemKey> {

}
