package pl.omnisport.api.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name="Payments", description = "Payment management")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "Get payments history")
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> getPaymentsHistory(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @PageableDefault(sort = "paymentDate", direction = Sort.Direction.DESC, size = 20) Pageable pageable
    ) {
        Page<PaymentResponse> history = paymentService.getPaymentsHistory(startDate, endDate, pageable);
        return ResponseEntity.ok(history);
    }
}