package kz.app.appstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import kz.app.appstore.dto.admin.AdminUserCreationDTO;
import kz.app.appstore.dto.admin.EmployeesDto;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.dto.order.OrderResponseDto;
import kz.app.appstore.service.AdminService;
import kz.app.appstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize(value = "hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final OrderService orderService;

    @Operation(summary = "Создание продукта", security = {@SecurityRequirement(name = "bearerAuth")})
    @PostMapping("/create-employee")
    public ResponseEntity<Void> createManager(@Valid @RequestBody AdminUserCreationDTO userRegistrationDTO){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String adminUsername = auth.getName();
        adminService.createManager(userRegistrationDTO, adminUsername);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Удаление сотрудника", security = {@SecurityRequirement(name = "bearerAuth")})
    @DeleteMapping("/delete-customer/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        adminService.deleteManager(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получение всех сотрудников", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/all-employees/get")
    public List<EmployeesDto> getAllEmployees() {
        return adminService.getAllEmployees();
    }

    @Operation(summary = "Получить все заказы", security = {@SecurityRequirement(name = "bearerAuth")})
    @GetMapping("/orders/all")
    public ResponseEntity<List<OrderResponseDto>> getAllOrders() {
        List<OrderResponseDto> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }


    @Operation(summary = "Обновление сотрудника", security = {@SecurityRequirement(name = "bearerAuth")})
    @PutMapping("/employee/{userId}/update")
    public ResponseEntity<?> updateEmployee(@PathVariable Long userId, @RequestBody AdminUserCreationDTO request) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String adminUsername = auth.getName();
            adminService.updateEmployee(userId, request, adminUsername);
            return ResponseEntity.ok("Employee updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }
}