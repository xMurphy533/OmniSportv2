package pl.omnisport.api.attendance;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Page<Attendance> findAllByCheckInTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
