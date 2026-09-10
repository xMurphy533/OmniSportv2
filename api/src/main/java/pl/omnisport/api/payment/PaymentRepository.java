package pl.omnisport.api.payment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Page<Payment> findAllByPaymentDateBetween(LocalDateTime startDate, LocalDateTime endTime, Pageable pageable);
}
