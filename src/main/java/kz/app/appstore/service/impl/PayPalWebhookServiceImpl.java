package kz.app.appstore.service.impl;

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
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Value("${paypal.webhook-id}")
    private String webhookId;

    public PayPalWebhookServiceImpl(OrderRepository orderRepository, PaymentRepository paymentRepository) {
        this.orderRepository = orderRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    public ResponseEntity<String> handleWebhook(String body, String transmissionId, String transmissionTime, String transmissionSig, String certUrl, String authAlgo, String webhookIdFromHeader) {
        try {
            if (!webhookId.equals(webhookIdFromHeader)) {
                log.warn("Webhook ID не совпадает!");
                return ResponseEntity.status(400).body("Invalid webhook ID");
            }

            // Собираем и проверяем подпись
            String expectedSignatureData = transmissionId + "|" + transmissionTime + "|" + webhookId + "|" + body;
            PublicKey publicKey = getPaypalPublicKey(certUrl);
            Signature signature = Signature.getInstance(authAlgo);
            signature.initVerify(publicKey);
            signature.update(expectedSignatureData.getBytes(StandardCharsets.UTF_8));
            boolean isValid = signature.verify(Base64.getDecoder().decode(transmissionSig));

            if (!isValid) {
                log.warn("Неверная подпись вебхука!");
                return ResponseEntity.status(403).body("Signature invalid");
            }

            // Обновим заказ в БД, если у тебя есть такая логика
            Optional<Order> optionalOrder = orderRepository.findByOrderCode("4BR71398140424314");
            if (optionalOrder.isPresent()) {
                kz.app.appstore.entity.Order order = optionalOrder.get();
                Optional<kz.app.appstore.entity.Payment> paymentEntity = paymentRepository.findByOrderId(order.getId());
                if (paymentEntity.isPresent()) {
                    kz.app.appstore.entity.Payment payment = paymentEntity.get();
                    payment.setStatus(PaymentStatus.FAILED);
                }
                order.setStatus(OrderStatus.DELIVERED); // например, "COMPLETED"
                orderRepository.save(order);
            }

//            // ✅ Тут логика обновления статуса заказа
//            JsonNode jsonNode = objectMapper.readTree(body);
//            String eventType = jsonNode.get("event_type").asText();
//            String orderId = jsonNode.get("resource").get("id").asText();
//            String newStatus = jsonNode.get("resource").get("status").asText();
            log.info("Webhook прошёл верификацию: {}", body);
            return ResponseEntity.ok("Webhook verified");

        } catch (Exception e) {
            log.error("Ошибка обработки вебхука", e);
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
