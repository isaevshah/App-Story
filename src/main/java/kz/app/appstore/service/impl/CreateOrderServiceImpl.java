package kz.app.appstore.service.impl;

import kz.app.appstore.dto.order.OrderItemRequestDto;
import kz.app.appstore.dto.order.OrderRequestDto;
import kz.app.appstore.entity.*;
import kz.app.appstore.enums.PaymentStatus;
import kz.app.appstore.enums.TrackStatus;
import kz.app.appstore.repository.OrderRepository;
import kz.app.appstore.repository.PaymentRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.CreateOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class CreateOrderServiceImpl implements CreateOrderService {
    @Value("${upload.path}")
    private String uploadPath;

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;

    public CreateOrderServiceImpl(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public void saveKaspiCheck(OrderRequestDto request, String username, MultipartFile file) throws IOException {
        if (!file.getOriginalFilename().endsWith(".pdf")) {
            throw new IllegalArgumentException("Файл должен быть в формате PDF");
        }
        try {
            User user = getUserByUsername(username);
            kz.app.appstore.entity.Order orderEntity = createOrderEntity(request, user, file);
            List<OrderItem> orderItems = createOrderItems(request, orderEntity);
            double totalPrice = calculateTotalPrice(orderItems);

            orderEntity.setOrderItems(orderItems);
            orderEntity.setOrderCode(String.valueOf(UUID.randomUUID()));
            orderEntity.setTotalPrice(totalPrice);
            orderRepository.save(orderEntity);
            createPaymentRecord(orderEntity, request.getPaymentMethod());
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }

    }

    private String savePdfFile(MultipartFile file) throws IOException {
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path filePath = uploadDir.resolve(fileName);
        Files.write(filePath, file.getBytes());
        return fileName;
    }

    private kz.app.appstore.entity.Order createOrderEntity(OrderRequestDto request, User user, MultipartFile file) throws IOException {
        kz.app.appstore.entity.Order order = new kz.app.appstore.entity.Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setPayStatus(PaymentStatus.WAITING_CONFIRMATION);
        order.setTrackStatus(TrackStatus.PENDING.name());
        order.setFirstname(request.getFirstname());
        order.setLastname(request.getLastname());
        order.setCountry(request.getCountry());
        order.setCity(request.getCity());
        order.setPhoneNumber(request.getPhoneNumber());
        order.setPoint(request.getPoint());
        String fileName = savePdfFile(file);
        order.setKaspiCheckPath(fileName); // добавь это поле в сущность Order
        return order;
    }

    private List<OrderItem> createOrderItems(OrderRequestDto request, kz.app.appstore.entity.Order order) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemRequestDto itemRequest : request.getItems()) {
            Product product = getProductById(itemRequest.getProductId());
            OrderItem orderItem = createOrderItem(order, product, itemRequest.getQuantity());
            orderItems.add(orderItem);
        }
        return orderItems;
    }

    private OrderItem createOrderItem(kz.app.appstore.entity.Order order, Product product, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(new OrderItemKey(order.getId(), product.getId()));
        orderItem.setOrder(order);
        orderItem.setProduct(product);
        orderItem.setQuantity(quantity);
        orderItem.setPrice(product.getPrice() * quantity);
        return orderItem;
    }

    private void createPaymentRecord(kz.app.appstore.entity.Order order, String paymentMethod) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.WAITING_CONFIRMATION);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private double calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

}
