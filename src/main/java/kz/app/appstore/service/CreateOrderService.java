package kz.app.appstore.service;

import kz.app.appstore.dto.order.OrderRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CreateOrderService {
    void saveKaspiCheck(OrderRequestDto request, String username, MultipartFile file) throws IOException;
}
