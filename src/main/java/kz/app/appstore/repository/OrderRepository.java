package kz.app.appstore.repository;

import kz.app.appstore.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderCode(String orderId);
    Page<Order> findByTrackStatusContainingIgnoreCase(String trackStatus, Pageable pageable);

//    @Query("SELECT o FROM Order o WHERE LOWER(o.trackStatus) LIKE LOWER(CONCAT('%', :trackStatus, '%'))")
//    Page<Order> searchByTrackStatus(@Param("trackStatus") String trackStatus, Pageable pageable);

}
