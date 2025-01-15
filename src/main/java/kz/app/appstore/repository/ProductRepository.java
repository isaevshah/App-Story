package kz.app.appstore.repository;

import jakarta.persistence.LockModeType;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p FROM Product p WHERE p.catalog.id = :catalogId")
    Page<Product> findByCatalogId(@Param("catalogId") Long catalogId, Pageable pageable);

    @Query("SELECT p FROM Product p")
    Page<Product> getAllProducts(Pageable pageable);
}