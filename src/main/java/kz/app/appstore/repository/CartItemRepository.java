package kz.app.appstore.repository;

import kz.app.appstore.entity.CartItem;
import kz.app.appstore.entity.CartItemKey;
import kz.app.appstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartItemRepository extends JpaRepository<CartItem, CartItemKey> {
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.product = :product")
    void deleteCartItemsByProduct(@Param("product") Product product);
}
