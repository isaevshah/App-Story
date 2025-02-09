package kz.app.appstore.controller;
/*
* складчик
* */

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.dto.order.OrderCreationResponseDto;
import kz.app.appstore.dto.order.OrderResponseDto;
import kz.app.appstore.dto.product.ProductResponse;
import kz.app.appstore.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/worker")
@AllArgsConstructor
//@PreAuthorize(value = "hasRole('WAREHOUSE')")
public class WarehouseManController {

    private final OrderService orderService;

    @Operation(summary = "Получение всех заказов")
    @GetMapping("/all-orders")
    public ResponseEntity<?> getAllOrders() {
        try {
            List<OrderCreationResponseDto> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @Operation(summary = "Открыть детали заказа")
    @GetMapping("/{orderId}")
    public ResponseEntity<?> getByOrderId(@PathVariable Long orderId) {
        try {
            OrderResponseDto orders = orderService.getById(orderId);
            return ResponseEntity.ok(orders);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }

    @Operation(summary = "Подтвердить и сгенерировать QR")
    @GetMapping("/orders/{orderId}/approve")
    public ResponseEntity<byte[]> approveOrder(@PathVariable Long orderId,
                                               @RequestParam String storageOfProduct) {
        return orderService.approveOrder(orderId, storageOfProduct);
    }
}
