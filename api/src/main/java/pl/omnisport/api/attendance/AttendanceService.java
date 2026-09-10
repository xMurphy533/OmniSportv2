package pl.omnisport.api.attendance;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final MemberRepository memberRepository;
    private final AttendanceRepository attendanceRepository;

    @Transactional
    public void registerCheckIn(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );

        if(member.getExpiryDate() == null || member.getExpiryDate().isBefore(LocalDate.now())){
            member.setPassValid(false);
            memberRepository.save(member);
            throw new IllegalStateException("Your pass has expired");
        }

        if(!member.isPassValid()){
            throw new IllegalStateException("Your pass is invalid");
        }

        Attendance attendance = new Attendance();
        attendance.setMember(member);
        attendance.setCheckInTime(LocalDateTime.now());

        attendanceRepository.save(attendance);
    }

    public Page<AttendanceResponse> getAttendanceHistory(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Page<Attendance> attendances;

        if (startDate != null && endDate != null) {
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(java.time.LocalTime.MAX);
            attendances = attendanceRepository.findAllByCheckInTimeBetween(start, end, pageable);
        } else {
            attendances = attendanceRepository.findAll(pageable);
        }

        return attendances.map(attendance -> new AttendanceResponse(
                attendance.getId(),
                attendance.getCheckInTime(),
                attendance.getMember().getId(),
                attendance.getMember().getName(),
                attendance.getMember().getSurname()
        ));
    }
}
