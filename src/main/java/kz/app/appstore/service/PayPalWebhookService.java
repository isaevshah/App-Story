package kz.app.appstore.service;

import org.springframework.http.ResponseEntity;

public interface PayPalWebhookService {
    ResponseEntity<String> handleWebhook(String body,
                                         String transmissionId,
                                         String transmissionTime,
                                         String transmissionSig,
                                         String certUrl,
                                         String authAlgo);
}
