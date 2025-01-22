package kz.app.appstore.service.impl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import kz.app.appstore.dto.admin.AdminUserCreationDTO;
import kz.app.appstore.dto.admin.EmployeesDto;
import kz.app.appstore.entity.Catalog;
import kz.app.appstore.entity.Profile;
import kz.app.appstore.entity.User;
import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createManager(AdminUserCreationDTO userCreationDTO, String adminUsername) {
        if (userRepository.existsByUsername(userCreationDTO.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        User user = buildUserFromDTO(userCreationDTO, adminUsername);

        userRepository.save(user);
        log.info("Created user with username: {}", user.getUsername());
    }

    private User buildUserFromDTO(AdminUserCreationDTO userCreationDTO, String adminUsername) {
        User user = new User();
        user.setUsername(userCreationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
        user.setRole(userCreationDTO.getIsManager() ? Role.MANAGER : Role.WAREHOUSE_WORKER);
        user.setUserType(UserType.PHYSICAL);
        user.setRegistrationAt(LocalDateTime.now());
        user.setCreatedBy(adminUsername);
        user.setActive(true);

        Profile profile = new Profile();
        profile.setPhoneNumber(userCreationDTO.getPhoneNumber());
        profile.setFirstName(userCreationDTO.getFirstName());
        profile.setLastName(userCreationDTO.getLastName());
        profile.setUser(user);

        user.setProfile(profile);

        return user;
    }


    @Override
    public void deleteManager(Long id) {
        try {
            userRepository.deleteById(id);
            log.info("Deleted user: {}", id);
        } catch (Exception e) {
            log.error("Error deleting user", e);
        }
    }

    @Override
    public void updateEmployee(Long userId, AdminUserCreationDTO userRegistrationDTO, String adminUsername) {
        User employee = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found: " + userId));
        employee.setUsername(userRegistrationDTO.getUsername());
        employee.setPassword(passwordEncoder.encode(userRegistrationDTO.getPassword()));
        employee.setRole(userRegistrationDTO.getIsManager() ? Role.MANAGER : Role.WAREHOUSE_WORKER);
        employee.setUserType(UserType.PHYSICAL);
        employee.setActive(true);
        employee.setUpdatedAt(LocalDateTime.now());
        employee.setUpdatedBy(adminUsername);


        Profile profile = employee.getProfile();
        profile.setPhoneNumber(userRegistrationDTO.getPhoneNumber());
        profile.setFirstName(userRegistrationDTO.getFirstName());
        profile.setLastName(userRegistrationDTO.getLastName());
        profile.setUser(employee);

        employee.setProfile(profile);
        userRepository.save(employee);
    }

    @Override
    public List<EmployeesDto> getAllEmployees() {
        List<User> employees = userRepository.getAllEmployees(List.of(Role.MANAGER, Role.WAREHOUSE_WORKER, Role.ADMIN));
        return employees.stream()
                .map(user -> {
                    Profile profile = user.getProfile();
                    return new EmployeesDto(
                            user.getId(),
                            user.getUsername(),
                            user.getRole(),
                            profile != null ? profile.getFirstName() : null,
                            profile != null ? profile.getLastName() : null
                    );
                })
                .collect(Collectors.toList());
    }

}
