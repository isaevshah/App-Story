package kz.app.appstore.service;

import kz.app.appstore.dto.admin.AdminUserCreationDTO;
import kz.app.appstore.dto.admin.EmployeesDto;
import kz.app.appstore.dto.order.OrderResponseDto;

import java.util.List;

public interface AdminService {
    void createManager(AdminUserCreationDTO userRegistrationDTO, String adminUsername);
    void deleteManager(Long id);
    void updateEmployee(Long userId,AdminUserCreationDTO userRegistrationDTO, String adminUsername);
    List<EmployeesDto> getAllEmployees();
}
