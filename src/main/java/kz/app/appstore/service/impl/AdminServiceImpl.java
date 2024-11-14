package kz.app.appstore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ValidationException;
import kz.app.appstore.dto.auth.AdminUserCreationDTO;
import kz.app.appstore.entity.Profile;
import kz.app.appstore.entity.User;
import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import kz.app.appstore.repository.UserRepository;
import kz.app.appstore.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.service.spi.ServiceException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AdminServiceImpl implements AdminService {
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final PasswordEncoder passwordEncoder;

    public AdminServiceImpl(UserRepository userRepository, ObjectMapper objectMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void createManager(AdminUserCreationDTO userCreationDTO) {
        try {
            if (userRepository.existsByUsername(userCreationDTO.getUsername())) {
                throw new ValidationException("Username already exists");
            }

            User user = new User();
            user.setUsername(userCreationDTO.getUsername());
            user.setPassword(passwordEncoder.encode(userCreationDTO.getPassword()));
            if (userCreationDTO.getIsManager()) {
                user.setRole(Role.MANAGER);
            } else {
                user.setRole(Role.WAREHOUSE_WORKER);
            }
            user.setUserType(UserType.PHYSICAL);
            user.setActive(true);

            Profile profile = new Profile();
            profile.setPhoneNumber(userCreationDTO.getPhoneNumber());
            profile.setFirstName(userCreationDTO.getFirstName());
            profile.setLastName(userCreationDTO.getLastName());
            user.setProfile(profile);
            profile.setUser(user);

            userRepository.save(user);
            log.info("Created user: {}", objectMapper.writeValueAsString(user));
        } catch (Exception e) {
            log.error("Error creating user", e);
            throw new ServiceException("Failed to create user", e);
        }
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
}
