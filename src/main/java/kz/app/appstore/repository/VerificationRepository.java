package kz.app.appstore.repository;

import kz.app.appstore.entity.RegistrationVerification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationRepository extends JpaRepository<RegistrationVerification, String> {
}
