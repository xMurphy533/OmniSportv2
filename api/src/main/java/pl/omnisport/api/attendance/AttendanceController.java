package pl.omnisport.api.attendance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendances")
@RequiredArgsConstructor
@Tag(name="Attendances", description = "Attendance manegement")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @Operation(
            summary = "Get attendance history",
            description = "Returns attendance of all members within a specified period of time"
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AttendanceResponse>> getAttendanceHistory(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @ParameterObject @PageableDefault(sort = "checkInTime", direction = Sort.Direction.DESC, size = 20) Pageable pageable
    ) {
        Page<AttendanceResponse> history = attendanceService.getAttendanceHistory(startDate, endDate, pageable);
        return ResponseEntity.ok(history);
    }
}