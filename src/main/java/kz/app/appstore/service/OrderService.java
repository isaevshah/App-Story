package kz.app.appstore.service;

import kz.app.appstore.dto.order.OrderResponseDto;

import java.util.List;

public interface OrderService {
    List<OrderResponseDto> getAllOrders();
}
