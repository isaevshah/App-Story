package kz.app.appstore.service.impl;

import kz.app.appstore.dto.order.*;
import kz.app.appstore.entity.*;
import kz.app.appstore.enums.OrderStatus;
import kz.app.appstore.enums.PaymentStatus;
import kz.app.appstore.repository.*;
import kz.app.appstore.service.OrderService;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public OrderServiceImpl(UserRepository userRepository, ProductRepository productRepository, OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public OrderCreationResponseDto createOrder(OrderRequestDto request, String username) {
        User user = getUserByUsername(username);
        Order order = createOrderEntity(request, user);
        List<OrderItem> orderItems = createOrderItems(request, order);
        double totalPrice = calculateTotalPrice(orderItems);

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        createPaymentRecord(order, request.getPaymentMethod());

        return new OrderCreationResponseDto(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus().name(),
                request.getPaymentMethod()
        );
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    private Order createOrderEntity(OrderRequestDto request, User user) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setFirstname(request.getFirstname());
        order.setLastname(request.getLastname());
        order.setCountry(request.getCountry());
        order.setCity(request.getCity());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setPoint(request.getPoint());
        return order;
    }

    private List<OrderItem> createOrderItems(OrderRequestDto request, Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : request.getItems()) {
            Product product = getProductById(itemRequest.getProductId());
            OrderItem orderItem = createOrderItem(order, product, itemRequest.getQuantity());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
    }

    private OrderItem createOrderItem(Order order, Product product, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(new OrderItemKey(order.getId(), product.getId()));
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice() * quantity);
        return orderItem;
    }

    private double calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
    }

    private void createPaymentRecord(Order order, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponseDto> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        return orders.stream().map(this::mapToOrderResponseDto).collect(Collectors.toList());
    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderDate(),
                order.getStatus().name(),
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
