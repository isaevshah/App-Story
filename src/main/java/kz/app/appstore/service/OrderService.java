package kz.app.appstore.service;

import kz.app.appstore.dto.order.OrderCreationResponseDto;
import kz.app.appstore.dto.order.OrderRequestDto;
import kz.app.appstore.dto.order.OrderResponseDto;

import java.util.List;

public interface OrderService {
    OrderCreationResponseDto createOrder(OrderRequestDto request, String username);
    List<OrderResponseDto> getAllOrders();
}
