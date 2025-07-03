package kz.app.appstore.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import kz.app.appstore.dto.qr.OrderWithQrDto;
import kz.app.appstore.entity.Order;
import kz.app.appstore.entity.OrderItem;
import kz.app.appstore.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Service
public class QrService {
    private final OrderRepository orderRepository;

    public QrService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public byte[] generateQrCodeAsBytes(String content, int width, int height) throws WriterException, IOException {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", baos);
        return baos.toByteArray();
    }

    public OrderWithQrDto generateOrderQr(Long orderId) throws IOException, WriterException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        List<OrderWithQrDto.OrderItemQrDto> itemDtos = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();

        for (OrderItem item : order.getOrderItems()) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("orderId", order.getId());
            payload.put("orderItemId", item.getId());
            payload.put("productId", item.getProduct().getId());
            payload.put("productName", item.getProduct().getName());
            payload.put("customerName", order.getFirstname() + " " + order.getLastname());

            String json = mapper.writeValueAsString(payload);
            byte[] qrBytes = generateQrCodeAsBytes(json, 300, 300);
            String base64 = Base64.getEncoder().encodeToString(qrBytes);

            itemDtos.add(new OrderWithQrDto.OrderItemQrDto(item.getProduct().getId(), base64));
        }

        return new OrderWithQrDto(order.getId(), itemDtos);
    }
}