package kz.app.appstore.service;

import kz.app.appstore.dto.order.OrderRequestDto;
import kz.app.appstore.dto.paypal.PayPalResponseDto;

import java.io.IOException;

public interface PayPalService {
    PayPalResponseDto createOrder(OrderRequestDto request, String username) throws IOException;
}
