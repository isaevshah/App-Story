package kz.app.appstore.controller;

import kz.app.appstore.dto.AdminUserCreationDTO;
import kz.app.appstore.service.AdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/create-manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> createManager(@RequestBody AdminUserCreationDTO userRegistrationDTO){
        adminService.createManager(userRegistrationDTO);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete-customer/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        adminService.deleteManager(id);
        return ResponseEntity.ok().build();
    }
}