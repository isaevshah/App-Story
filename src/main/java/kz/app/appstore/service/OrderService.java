package kz.app.appstore.service;

import kz.app.appstore.dto.order.OrderResponseDto;
import org.springframework.data.domain.Page;

public interface OrderService {
    Page<OrderResponseDto> getAllOrders(int page, int size);

    Page<OrderResponseDto> getOrderByTrackStatus(int page, int size, String trackStatus);

    void updateTrackStatus(Long id, String trackStatus);
}
