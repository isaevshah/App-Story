package kz.app.appstore.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageConfig;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import kz.app.appstore.dto.order.OrderCreationResponseDto;
import kz.app.appstore.dto.order.OrderItemRequestDto;
import kz.app.appstore.dto.order.OrderRequestDto;
import kz.app.appstore.dto.order.OrderResponseDto;
import kz.app.appstore.entity.*;
import kz.app.appstore.enums.OrderStatus;
import kz.app.appstore.enums.PaymentStatus;
import kz.app.appstore.repository.*;
import kz.app.appstore.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
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
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        // Создание нового заказа
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

        List<OrderItem> orderItems = new ArrayList<>();
        double totalPrice = 0;

        for (OrderItemRequestDto itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Товар не найден"));

            OrderItem orderItem = new OrderItem();
            OrderItemKey orderItemKey = new OrderItemKey(order.getId(), product.getId());

            orderItem.setId(orderItemKey);
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice() * itemRequest.getQuantity());

            orderItems.add(orderItem);
            totalPrice += orderItem.getPrice();
        }

        order.setOrderItems(orderItems);
        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        // Создаём запись об оплате
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        // Возвращаем сокращённый DTO
        return new OrderCreationResponseDto(
                order.getId(),
                order.getTotalPrice(),
                order.getStatus().name(),
                request.getPaymentMethod()
        );
    }

    @Override
    public List<OrderCreationResponseDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> OrderCreationResponseDto.builder()
                        .orderId(order.getId())
                        .totalPrice(order.getTotalPrice())
                        .status(order.getStatus().name())
                        .paymentMethod("-") // Если есть это поле в Order
                        .build())
                .collect(Collectors.toList());
    }
        @Override
        public OrderResponseDto getById(Long orderId) {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new RuntimeException("Заказ не найден"));

            return OrderResponseDto.builder()
                    .firstname(order.getFirstname())
                    .lastname(order.getLastname())
                    .phoneNumber(order.getPhoneNumber())
                    .country(order.getCountry())
                    .city(order.getCity())
                    .point(order.getPoint())
                    .totalPrice(order.getTotalPrice())
                    .user(order.getUser())
                    .items(order.getOrderItems().stream().map(orderItem -> OrderItemRequestDto.builder()
                            .quantity(orderItem.getQuantity())
                            .productId(orderItem.getId().getProductId())
                            .build()).collect(Collectors.toList()))
                    .build();
        }

    @Transactional
    public ResponseEntity<byte[]> approveOrder(Long orderId, String storageOfProduct) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Заказ не найден"));

        order.setStatus(OrderStatus.CONFIRMED);
        order.setStoragePlace(storageOfProduct);
        orderRepository.save(order);

        String qrData = "https://youtu.be/7FEhvFgBSq4";

        byte[] decodedQrCode = generateQRCodeWithLogo(qrData, "src/main/resources/images/logo/standing_cat.jpg");
        return ResponseEntity.ok()
                .header("Content-Type", "image/png")
                .body(decodedQrCode);
    }

    public static byte[] generateQRCodeWithLogo(String text, String logoPath) {
        int size = 400;
        int borderSize = 20;
        int logoSize = size / 4;

        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, size, size);

            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix, new MatrixToImageConfig());

            BufferedImage qrWithBorder = new BufferedImage(size + 2 * borderSize, size + 2 * borderSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = qrWithBorder.createGraphics();
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, qrWithBorder.getWidth(), qrWithBorder.getHeight());
            g.drawImage(qrImage, borderSize, borderSize, null);

            BufferedImage logo = ImageIO.read(new File(logoPath));

            Image scaledLogo = logo.getScaledInstance(logoSize, logoSize, Image.SCALE_SMOOTH);

            int centerX = (qrWithBorder.getWidth() - logoSize) / 2;
            int centerY = (qrWithBorder.getHeight() - logoSize) / 2;
            g.drawImage(scaledLogo, centerX, centerY, null);

            g.dispose();

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            ImageIO.write(qrWithBorder, "PNG", pngOutputStream);
            return pngOutputStream.toByteArray();

        } catch (WriterException | IOException e) {
            throw new RuntimeException("Ошибка при генерации QR-кода", e);
        }
    }
}
