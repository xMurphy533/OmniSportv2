package pl.omnisport.api.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public Page<PaymentResponse> getPaymentsHistory(LocalDate startDate, LocalDate endDate, Pageable pageable){
        Page<Payment> payments;

        if(startDate != null && endDate != null){
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            payments = paymentRepository.findAllByPaymentDateBetween(start, end, pageable);
        } else {
            payments = paymentRepository.findAll(pageable);
        }
        return payments.map(payment -> new PaymentResponse(
                payment.getId(),
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getMember().getId(),
                payment.getMember().getName(),
                payment.getMember().getSurname()
        ));
    }

}
