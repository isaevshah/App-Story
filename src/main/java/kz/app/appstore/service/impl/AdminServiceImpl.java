package kz.app.appstore.service.impl;

import jakarta.validation.ValidationException;
import kz.app.appstore.dto.admin.AdminUserCreationDTO;
import kz.app.appstore.dto.admin.EmployeesDto;
import kz.app.appstore.entity.Profile;
import kz.app.appstore.entity.User;
import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
    public void createManager(AdminUserCreationDTO userCreationDTO) {
        if (userRepository.existsByUsername(userCreationDTO.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        User user = buildUserFromDTO(userCreationDTO);

        userRepository.save(user);
        log.info("Created user with username: {}", user.getUsername());
    }

    private User buildUserFromDTO(AdminUserCreationDTO userCreationDTO) {
        User user = new User();
        user.setUsername(userCreationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
        user.setRole(userCreationDTO.getIsManager() ? Role.MANAGER : Role.WAREHOUSE_WORKER);
        user.setUserType(UserType.PHYSICAL);
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
    public List<EmployeesDto> getAllEmployees() {
        List<User> employees = userRepository.getAllEmployees(List.of(Role.MANAGER, Role.WAREHOUSE_WORKER));
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
