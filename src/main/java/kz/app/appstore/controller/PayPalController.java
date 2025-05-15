package kz.app.appstore.controller;

import kz.app.appstore.dto.order.OrderRequestDto;
import kz.app.appstore.dto.paypal.PayPalCaptureResponseDto;
import kz.app.appstore.dto.paypal.PayPalResponseDto;
import kz.app.appstore.service.PayPalService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paypal")
@Slf4j
@PreAuthorize(value = "hasAnyRole('MANAGER', 'ADMIN', 'CUSTOMER')")
public class PayPalController {
    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<PayPalResponseDto> createOrder(@RequestBody OrderRequestDto request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();
            PayPalResponseDto response = payPalService.createOrder(request, username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/success")
    public ResponseEntity<String> handlePaypalSuccess(@RequestParam("token") String orderId) {
        try {
            PayPalCaptureResponseDto response = payPalService.captureOrder(orderId);
            return ResponseEntity.ok("Оплата прошла успешно! Статус: " + response.getStatus());
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при захвате оплаты: " + e.getMessage());
        }
    }
}