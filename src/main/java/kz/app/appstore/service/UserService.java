package kz.app.appstore.service;

// UserService.java
import jakarta.validation.ValidationException;
import kz.app.appstore.dto.UserRegistrationDTO;
import kz.app.appstore.entity.Profile;
import kz.app.appstore.entity.User;
import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import kz.app.appstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(UserRegistrationDTO registrationDTO) throws Exception {
        // Проверяем, существует ли пользователь
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(Role.CUSTOMER); // Или любая роль по умолчанию
        user.setUserType(registrationDTO.getUserType());
        user.setActive(true);

        // Установка дополнительных полей в зависимости от UserType
        Profile profile = new Profile();
        profile.setPhoneNumber(registrationDTO.getPhoneNumber());
        profile.setFirstName(registrationDTO.getFirstName());
        profile.setLastName(registrationDTO.getLastName());
        if (registrationDTO.getUserType() == UserType.JURIDICAL) {
            profile.setBin(registrationDTO.getBin());
            profile.setCompanyName(registrationDTO.getCompanyName());
        }
        user.setProfile(profile);
        profile.setUser(user);

        userRepository.save(user);
    }
}

