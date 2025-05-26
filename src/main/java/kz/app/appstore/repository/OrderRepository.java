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
}
