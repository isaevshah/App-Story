package kz.app.appstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kz.app.appstore.dto.order.OrderResponseDto;
import kz.app.appstore.dto.qr.OrderWithQrDto;
import kz.app.appstore.service.OrderService;
import kz.app.appstore.service.QrService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/worker")
@PreAuthorize(value = "hasAnyRole('WAREHOUSE_WORKER', 'ADMIN')")
@Slf4j
public class WarehouseController {
    private final OrderService orderService;
    private final QrService qrService;

    public WarehouseController(OrderService orderService, QrService qrService) {
        this.orderService = orderService;
        this.qrService = qrService;
    }

    @Operation(summary = "Получить заказы по trackStatus", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/orders/by-track-status")
    Page<OrderResponseDto> getOrderByTrackStatus(@RequestParam(defaultValue = "0") int page,
                                                 @RequestParam(defaultValue = "10") int size,
                                                 @RequestParam String trackStatus) {
        return orderService.getOrderByTrackStatus(page, size, trackStatus);
    }

    @Operation(summary = "Изменить trackStatus заказа", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/update/track-status/{id}")
    public void updateTrackStatus(@PathVariable Long id, @RequestParam String trackStatus) {
        orderService.updateTrackStatus(id, trackStatus);
    }

    @GetMapping("/{orderId}/qrcodes")
    public OrderWithQrDto getOrderQrCodes(@PathVariable Long orderId) throws Exception {
        return qrService.generateOrderQr(orderId);
    }
}
