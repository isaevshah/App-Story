package kz.app.appstore.service;

import kz.app.appstore.dto.order.OrderResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    List<OrderResponseDto> getAllOrders();
    Page<OrderResponseDto> getOrderByTrackStatus(int page, int size, String trackStatus);
}
