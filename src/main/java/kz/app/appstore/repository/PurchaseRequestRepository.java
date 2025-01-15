package kz.app.appstore.repository;

import kz.app.appstore.entity.Product;
import kz.app.appstore.entity.PurchaseRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
    @Modifying
    @Query("UPDATE PurchaseRequest p SET p.product = null WHERE p.product = :product")
    void setProductToNullInPurchaseRequest(@Param("product") Product product);

}