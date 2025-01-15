package kz.app.appstore.repository;

import kz.app.appstore.entity.OrderItem;
import kz.app.appstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Modifying
    @Query("UPDATE OrderItem o SET o.product = null WHERE o.product = :product")
    void setProductToNullInOrderItem(@Param("product") Product product);

}
