package kz.app.appstore.controller;

import kz.app.appstore.service.PayPalWebhookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/paypal")
@Slf4j
@RequiredArgsConstructor
public class PayPalWebhookController {

    private final PayPalWebhookService webhookService;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String body,
            @RequestHeader("paypal-transmission-id") String transmissionId,
            @RequestHeader("paypal-transmission-time") String transmissionTime,
            @RequestHeader("paypal-transmission-sig") String transmissionSig,
            @RequestHeader("paypal-cert-url") String certUrl,
            @RequestHeader("paypal-auth-algo") String authAlgo,
            @RequestHeader("paypal-webhook-id") String webhookIdFromHeader
    ) {
        return webhookService.handleWebhook(
                body, transmissionId, transmissionTime, transmissionSig, certUrl, authAlgo, webhookIdFromHeader
        );
    }
}
