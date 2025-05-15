package kz.app.appstore.service.impl;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import com.paypal.orders.Order;
import kz.app.appstore.dto.order.OrderItemRequestDto;
import kz.app.appstore.dto.order.OrderRequestDto;
import kz.app.appstore.dto.paypal.PayPalCaptureResponseDto;
import kz.app.appstore.dto.paypal.PayPalResponseDto;
import kz.app.appstore.entity.*;
import kz.app.appstore.enums.OrderStatus;
import kz.app.appstore.enums.PaymentStatus;
import kz.app.appstore.repository.OrderRepository;
import kz.app.appstore.repository.PaymentRepository;
import kz.app.appstore.repository.ProductRepository;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.PayPalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@Slf4j
public class PayPalServiceImpl implements PayPalService {
    @Autowired
    private PayPalHttpClient payPalHttpClient;

    private final UserRepository userRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    @Value("${paypal.return-url}")
    private String paypalReturnUrl;

    @Value("${paypal.cancel-url}")
    private String paypalCancelUrl;

    public PayPalServiceImpl(UserRepository userRepository, PaymentRepository paymentRepository, ProductRepository productRepository, OrderRepository orderRepository) {
        this.userRepository = userRepository;
        this.paymentRepository = paymentRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @Override
    public PayPalResponseDto createOrder(OrderRequestDto request, String username) throws IOException {
        try {
            User user = getUserByUsername(username);
            kz.app.appstore.entity.Order orderEntity = createOrderEntity(request, user);
            List<OrderItem> orderItems = createOrderItems(request, orderEntity);
            double totalPrice = calculateTotalPrice(orderItems);

            //PAYPAL
            // Создаем запрос на создание заказа
            OrderRequest orderRequest = new OrderRequest();
            orderRequest.checkoutPaymentIntent("CAPTURE");
            // Настраиваем детали покупки
            List<PurchaseUnitRequest> purchaseUnits = new ArrayList<>();
            PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                    .amountWithBreakdown(new AmountWithBreakdown()
                            .currencyCode(request.getCurrencyCode())
                            .value(String.valueOf(request.getTotalPrice())));
            purchaseUnits.add(purchaseUnit);
            orderRequest.purchaseUnits(purchaseUnits);
            // Настраиваем контекст приложения
            ApplicationContext applicationContext = new ApplicationContext()
                    .returnUrl(paypalReturnUrl)
                    .cancelUrl(paypalCancelUrl)
                    .userAction("PAY_NOW")  // Показывает кнопку "Pay Now" вместо "Continue"
                    .shippingPreference("NO_SHIPPING");  // Если доставка не требуется

            orderRequest.applicationContext(applicationContext);
            // Отправляем запрос в PayPal
            OrdersCreateRequest ordersCreateRequest = new OrdersCreateRequest().requestBody(orderRequest);
            HttpResponse<Order> response = payPalHttpClient.execute(ordersCreateRequest);
            Order order = response.result();
            // Создаем ответ
            PayPalResponseDto createOrderResponse = new PayPalResponseDto();
            createOrderResponse.setOrderId(order.id());
            createOrderResponse.setStatus(order.status());
            createOrderResponse.setApprovalUrl(getApprovalUrl(order));

            orderEntity.setOrderItems(orderItems);
            orderEntity.setOrderCode(order.id());
            orderEntity.setTotalPrice(totalPrice);
            orderRepository.save(orderEntity);
            createPaymentRecord(orderEntity, request.getPaymentMethod(), order.status());

            return createOrderResponse;
        } catch (IOException e) {
            throw new RuntimeException("Failed to create PayPal order: " + e.getMessage(), e);
        }
    }

    @Override
    public PayPalCaptureResponseDto captureOrder(String orderId) throws IOException{
        OrdersCaptureRequest request = new OrdersCaptureRequest(orderId);
        request.requestBody(new OrderRequest()); // тело может быть пустым для capture

        HttpResponse<Order> response = payPalHttpClient.execute(request);
        Order capturedOrder = response.result();

        // Обновим заказ в БД, если у тебя есть такая логика
        Optional<kz.app.appstore.entity.Order> optionalOrder = orderRepository.findByOrderCode(orderId);
        if (optionalOrder.isPresent()) {
            kz.app.appstore.entity.Order order = optionalOrder.get();
            Optional<kz.app.appstore.entity.Payment> paymentEntity = paymentRepository.findByOrderId(order.getId());
            if (paymentEntity.isPresent()) {
                kz.app.appstore.entity.Payment payment = paymentEntity.get();
                payment.setStatus(PaymentStatus.PAID);
            }
            order.setStatus(OrderStatus.CONFIRMED); // например, "COMPLETED"
            orderRepository.save(order);
        }

        // Ответ клиенту
        PayPalCaptureResponseDto result = new PayPalCaptureResponseDto();
        result.setOrderId(capturedOrder.id());
        result.setStatus(capturedOrder.status());
        return result;
    }

    private String getApprovalUrl(Order order) {
        for (LinkDescription link : order.links()) {
            if ("approve".equals(link.rel())) {
                return link.href();
            }
        }
        throw new NoSuchElementException("Approval URL not found in PayPal order response");
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
    }

    private kz.app.appstore.entity.Order createOrderEntity(OrderRequestDto request, User user) {
        kz.app.appstore.entity.Order order = new kz.app.appstore.entity.Order();
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

    private double calculateTotalPrice(List<OrderItem> orderItems) {
        return orderItems.stream().mapToDouble(OrderItem::getPrice).sum();
    }

    private void createPaymentRecord(kz.app.appstore.entity.Order order, String paymentMethod, String status) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);
    }

    private Product getProductById(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Товар не найден"));
    }
}
