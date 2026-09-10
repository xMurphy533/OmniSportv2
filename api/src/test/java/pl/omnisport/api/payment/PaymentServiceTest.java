package pl.omnisport.api.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import pl.omnisport.api.member.Member;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void givenDateRange_whenGetPaymentsHistory_thenQueryPaymentsBetweenDates() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setAmount(new BigDecimal("99.99"));
        payment.setPaymentDate(LocalDateTime.of(2024, 5, 15, 10, 30));

        Member member = new Member();
        member.setId(2L);
        member.setName("Jan");
        member.setSurname("Kowalski");
        payment.setMember(member);

        LocalDate startDate = LocalDate.of(2024, 5, 1);
        LocalDate endDate = LocalDate.of(2024, 5, 31);
        when(paymentRepository.findAllByPaymentDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                pageable
        )).thenReturn(new PageImpl<>(List.of(payment), pageable, 1));

        // When
        Page<PaymentResponse> response = paymentService.getPaymentsHistory(startDate, endDate, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(1L, response.getContent().get(0).id());
        assertEquals(new BigDecimal("99.99"), response.getContent().get(0).amount());
        assertEquals(2L, response.getContent().get(0).memberId());
        assertEquals("Jan", response.getContent().get(0).memberName());
        assertEquals("Kowalski", response.getContent().get(0).memberSurname());
        verify(paymentRepository).findAllByPaymentDateBetween(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                pageable
        );
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void givenNoDateRange_whenGetPaymentsHistory_thenQueryAllPayments() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        Payment payment = new Payment();
        payment.setId(3L);
        payment.setAmount(new BigDecimal("120.00"));
        payment.setPaymentDate(LocalDateTime.of(2024, 6, 1, 8, 15));

        Member member = new Member();
        member.setId(4L);
        member.setName("Anna");
        member.setSurname("Nowak");
        payment.setMember(member);

        when(paymentRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(payment), pageable, 1));

        // When
        Page<PaymentResponse> response = paymentService.getPaymentsHistory(null, null, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(3L, response.getContent().get(0).id());
        assertEquals(new BigDecimal("120.00"), response.getContent().get(0).amount());
        assertEquals(4L, response.getContent().get(0).memberId());
        assertEquals("Anna", response.getContent().get(0).memberName());
        assertEquals("Nowak", response.getContent().get(0).memberSurname());
        verify(paymentRepository).findAll(pageable);
        verifyNoMoreInteractions(paymentRepository);
    }
}
