package pl.omnisport.api.attendance;

import java.time.LocalDateTime;

public record AttendanceResponse(
        Long id,
        LocalDateTime checkInTime,
        Long memberId,
        String memberName,
        String memberSurname
) {
}
