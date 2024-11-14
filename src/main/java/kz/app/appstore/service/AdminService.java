package kz.app.appstore.service;

import kz.app.appstore.dto.auth.AdminUserCreationDTO;

public interface AdminService {
    void createManager(AdminUserCreationDTO userRegistrationDTO);
    void deleteManager(Long id);
}
