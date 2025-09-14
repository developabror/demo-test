package uz.app.demoapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.app.demoapp.entity.SmsAuthentication;

import java.util.Optional;

public interface SmsAuthenticationRepository extends JpaRepository<SmsAuthentication, Long> {
    Optional<SmsAuthentication> findByPhoneNumber(String phoneNumber);
}
