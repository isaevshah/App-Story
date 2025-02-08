package kz.app.appstore.service;

import kz.app.appstore.dto.order.OrderCreationResponseDto;
import kz.app.appstore.dto.order.OrderRequestDto;

public interface OrderService {
    OrderCreationResponseDto createOrder(OrderRequestDto request, String username);
}
