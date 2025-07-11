package kz.app.appstore.service;

// UserService.java
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import kz.app.appstore.dto.auth.UserRegistrationDTO;
import kz.app.appstore.dto.order.OrderItemDto;
import kz.app.appstore.dto.order.OrderResponseDto;
import kz.app.appstore.dto.user.UserInfoDto;
import kz.app.appstore.entity.Order;
import kz.app.appstore.entity.OrderItem;
import kz.app.appstore.entity.Profile;
import kz.app.appstore.entity.User;
import kz.app.appstore.enums.Role;
import kz.app.appstore.enums.UserType;
import kz.app.appstore.repository.OrderRepository;
import kz.app.appstore.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private OrderRepository orderRepository;

    public void registerUser(UserRegistrationDTO registrationDTO) throws Exception {
        // Проверяем, существует ли пользователь
        if (userRepository.existsByUsername(registrationDTO.getUsername())) {
            throw new ValidationException("Username already exists");
        }

        User user = new User();
        user.setUsername(registrationDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(Role.CUSTOMER);
        user.setRegistrationAt(LocalDateTime.now());
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

    public UserInfoDto getUserInfo(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        Profile profile = user.getProfile();

        return new UserInfoDto(
                user.getUsername(),
                profile.getFirstName(),
                profile.getLastName(),
                profile.getPhoneNumber(),
                profile.getBin(),
                profile.getCompanyName(),
                user.getRole(),
                user.getUserType(),
                user.isActive()
        );
    }

    public OrderResponseDto getUserOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToOrderResponseDto)
                .orElseThrow(() -> new EntityNotFoundException("Order with id " + id + " not found"));
    }


    public List<OrderResponseDto> getUserOrders(String username){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        List<Order> getUserOrders = orderRepository.findOrdersByUserId(user.getId());
        if(getUserOrders.isEmpty()){
            throw new NoSuchElementException("User not found");
        }
        return getUserOrders.stream().map(this::mapToOrderResponseDto).toList();
    }

    private OrderResponseDto mapToOrderResponseDto(Order order) {
        return new OrderResponseDto(
                order.getId(),
                order.getOrderDate(),
                order.getPayStatus().name(),
                order.getTrackStatus(),
                order.getTotalPrice(),
                order.getFirstname(),
                order.getLastname(),
                order.getPhoneNumber(),
                order.getCity(),
                order.getCountry(),
                order.getPoint(),
                order.getUser().getUsername(),
                order.getKaspiCheckPath(),
                order.getOrderItems().stream().map(this::mapToOrderItemDto).collect(Collectors.toList())
        );
    }

    private OrderItemDto mapToOrderItemDto(OrderItem orderItem) {
        return new OrderItemDto(
                orderItem.getProduct().getId(),
                orderItem.getProduct().getName(),
                orderItem.getQuantity(),
                orderItem.getPrice()
        );
    }

}

