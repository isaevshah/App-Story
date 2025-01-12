package kz.app.appstore.service;

import kz.app.appstore.dto.admin.AdminUserCreationDTO;
import kz.app.appstore.dto.admin.EmployeesDto;

import java.util.List;

public interface AdminService {
    void createManager(AdminUserCreationDTO userRegistrationDTO);
    void deleteManager(Long id);
    List<EmployeesDto> getAllEmployees();
}
