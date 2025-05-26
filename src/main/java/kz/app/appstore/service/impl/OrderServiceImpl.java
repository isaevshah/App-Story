package kz.app.appstore.service.impl;

import kz.app.appstore.dto.order.*;
import kz.app.appstore.entity.*;
import kz.app.appstore.repository.*;
import kz.app.appstore.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Optional;
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
    public Page<OrderResponseDto> getAllOrders(int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(this::mapToOrderResponseDto);
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

    @Override
    public void updateTrackStatus(Long id, String trackStatus) {
        log.info("Got track status update for order id {}", id);
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isPresent()) {
            Order order = optionalOrder.get();
            order.setTrackStatus(trackStatus);
            orderRepository.save(order);
        }
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
