package kz.app.appstore.service.impl;

import kz.app.appstore.dto.order.*;
import kz.app.appstore.entity.*;
import kz.app.appstore.repository.*;
import kz.app.appstore.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::mapToOrderResponseDto).collect(Collectors.toList());
    }

    @Override
    public Page<OrderResponseDto> getOrderByTrackStatus(int page, int size, String trackStatus) {
        Pageable pageable = PageRequest.of(page, size);
        String normalizedSearch = trackStatus.trim();
        Page<Order> orderResult = orderRepository.findByTrackStatusContainingIgnoreCase(normalizedSearch, pageable);
        if (orderResult.hasContent()) {
            return orderResult.map(this::mapToOrderResponseDto);
        }
        return new PageImpl<>(Collections.emptyList(), pageable, 0);

    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderDate(),
                order.getPayStatus().name(),
                order.getTotalPrice(),
                order.getFirstname(),
                order.getLastname(),
                order.getPhoneNumber(),
                order.getCity(),
                order.getCountry(),
                order.getPoint(),
                order.getUser().getUsername(),
                order.getOrderItems().stream().map(this::mapToOrderItemDto).collect(Collectors.toList())
        );
    }

    private OrderItemDto mapToOrderItemDto(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }
}
