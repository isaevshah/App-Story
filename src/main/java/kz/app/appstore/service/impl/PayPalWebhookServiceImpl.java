package kz.app.appstore.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kz.app.appstore.entity.Order;
import kz.app.appstore.enums.OrderStatus;
import kz.app.appstore.enums.PaymentStatus;
import kz.app.appstore.repository.OrderRepository;
import kz.app.appstore.repository.PaymentRepository;
import kz.app.appstore.service.PayPalWebhookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Optional;

@Service
@Slf4j
public class PayPalWebhookServiceImpl implements PayPalWebhookService {
    private final ObjectMapper objectMapper;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${paypal.webhook-id}")
    private String webhookId;

    public PayPalWebhookServiceImpl(ObjectMapper objectMapper, OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ResponseEntity<String> handleWebhook(String body,
                                                String transmissionId,
                                                String transmissionTime,
                                                String transmissionSig,
                                                String certUrl,
                                                String authAlgo) {
        try {
            // 🔐 Проверка подписи
            String expectedSignatureData = transmissionId + "|" + transmissionTime + "|" + webhookId + "|" + body;

            PublicKey publicKey = getPaypalPublicKey(certUrl);
            Signature signature = Signature.getInstance(authAlgo);
            signature.initVerify(publicKey);
            signature.update(expectedSignatureData.getBytes(StandardCharsets.UTF_8));
            boolean isValid = signature.verify(Base64.getDecoder().decode(transmissionSig));

            if (!isValid) {
                log.warn("❌ Подпись недействительна");
                return ResponseEntity.status(403).body("Invalid signature");
            }

            // ✅ Парсим тело и получаем orderId
            JsonNode jsonNode = objectMapper.readTree(body);
            String eventType = jsonNode.get("event_type").asText(); // например: CHECKOUT.ORDER.SAVED
            String paypalOrderId = jsonNode.get("resource").get("id").asText(); // напр: 9NK63094MA726991F

            // 🔎 Ищем заказ по коду (если ты сохранял orderCode = paypalOrderId)
            Optional<Order> optionalOrder = orderRepository.findByOrderCode(paypalOrderId);
            if (optionalOrder.isEmpty()) {
                log.warn("Заказ не найден: {}", paypalOrderId);
                return ResponseEntity.ok("No action needed");
            }

            Order order = optionalOrder.get();

            // 🧾 Обновим статус
            switch (eventType) {
//                case "CHECKOUT.ORDER.SAVED" -> order.setStatus(OrderStatus.CREATED);
                case "CHECKOUT.ORDER.APPROVED" -> order.setPayStatus(OrderStatus.CONFIRMED);
                case "PAYMENT.CAPTURE.COMPLETED" -> order.setPayStatus(OrderStatus.DELIVERED);
                case "PAYMENT.CAPTURE.DENIED" -> order.setPayStatus(OrderStatus.CANCELLED);
                default -> log.info("Необработанный тип события: {}", eventType);
            }

            orderRepository.save(order);
            log.info("✅ Вебхук обработан: заказ {} теперь со статусом {}", paypalOrderId, order.getPayStatus());
            return ResponseEntity.ok("Webhook processed");

        } catch (Exception e) {
            log.error("Ошибка при обработке вебхука", e);
            return ResponseEntity.status(500).body("Internal error");
        }
    }


    private PublicKey getPaypalPublicKey(String certUrl) throws Exception {
        URL url = new URL(certUrl);
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        try (InputStream in = url.openStream()) {
            X509Certificate certificate = (X509Certificate) factory.generateCertificate(in);
            return certificate.getPublicKey();
        }
    }
}
