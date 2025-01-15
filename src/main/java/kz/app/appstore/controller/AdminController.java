package kz.app.appstore.controller;

import jakarta.validation.Valid;
import kz.app.appstore.dto.admin.AdminUserCreationDTO;
import kz.app.appstore.dto.admin.EmployeesDto;
import kz.app.appstore.dto.error.ErrorResponse;
import kz.app.appstore.service.AdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize(value = "hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/create-employee")
    public ResponseEntity<Void> createManager(@Valid @RequestBody AdminUserCreationDTO userRegistrationDTO){
        adminService.createManager(userRegistrationDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-customer/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        adminService.deleteManager(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all-employees/get")
    public List<EmployeesDto> getAllEmployees() {
        return adminService.getAllEmployees();
    }

    @PostMapping("/employee/{userId}/update")
    public ResponseEntity<?> updateCatalog(@PathVariable Long userId, @RequestBody AdminUserCreationDTO request) {
        try {
            adminService.updateEmployee(userId, request);
            return ResponseEntity.ok("Employee updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse("Internal server error"));
        }
    }
}