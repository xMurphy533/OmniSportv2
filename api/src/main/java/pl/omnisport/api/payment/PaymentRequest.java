package pl.omnisport.api.payment;

import java.math.BigDecimal;

public record PaymentRequest(BigDecimal amount) {
}
