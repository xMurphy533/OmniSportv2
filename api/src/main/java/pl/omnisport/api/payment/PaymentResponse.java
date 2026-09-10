package pl.omnisport.api.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        BigDecimal amount,
        LocalDateTime paymentDate,
        Long memberId,
        String memberName,
        String memberSurname
) {}
