package kz.app.appstore.repository;

import kz.app.appstore.entity.User;
import kz.app.appstore.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);

    void deleteById(Long id);

    @Query("SELECT u FROM User u WHERE u.role IN (:roles)")
    List<User> getAllEmployees(@Param("roles") List<Role> roles);

    boolean existsByEmail(String email);

    // Проверка номера в профиле пользователя
    @Query("SELECT COUNT(u) > 0 FROM User u WHERE u.profile.phoneNumber = :phone")
    boolean existsByProfilePhoneNumber(@Param("phone") String phone);

}
