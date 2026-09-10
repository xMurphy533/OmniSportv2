package pl.omnisport.api.attendance;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AttendanceRepository attendanceRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void givenValidMember_whenRegisterCheckIn_thenSaveAttendance() {
        // Given
        Member member = new Member();
        member.setId(1L);
        member.setName("Jan");
        member.setSurname("Kowalski");
        member.setPassValid(true);
        member.setExpiryDate(LocalDate.now().plusDays(10));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        attendanceService.registerCheckIn(1L);

        // Then
        ArgumentCaptor<Attendance> attendanceCaptor = ArgumentCaptor.forClass(Attendance.class);
        verify(attendanceRepository).save(attendanceCaptor.capture());
        Attendance savedAttendance = attendanceCaptor.getValue();
        assertEquals(member, savedAttendance.getMember());
        assertNotNull(savedAttendance.getCheckInTime());
        verify(memberRepository).findById(1L);
        verifyNoMoreInteractions(memberRepository, attendanceRepository);
    }

    @Test
    void givenMissingMember_whenRegisterCheckIn_thenThrowEntityNotFoundException() {
        // Given
        when(memberRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> attendanceService.registerCheckIn(1L));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(1L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMemberWithNullExpiryDate_whenRegisterCheckIn_thenInvalidatePassAndThrowException() {
        // Given
        Member member = new Member();
        member.setPassValid(true);
        member.setExpiryDate(null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> attendanceService.registerCheckIn(1L));

        // Then
        assertEquals("Your pass has expired", exception.getMessage());
        assertFalse(member.isPassValid());
        verify(memberRepository).findById(1L);
        verify(memberRepository).save(member);
        verify(attendanceRepository, never()).save(any(Attendance.class));
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMemberWithExpiredPass_whenRegisterCheckIn_thenInvalidatePassAndThrowException() {
        // Given
        Member member = new Member();
        member.setPassValid(true);
        member.setExpiryDate(LocalDate.now().minusDays(1));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> attendanceService.registerCheckIn(1L));

        // Then
        assertEquals("Your pass has expired", exception.getMessage());
        assertFalse(member.isPassValid());
        verify(memberRepository).findById(1L);
        verify(memberRepository).save(member);
        verify(attendanceRepository, never()).save(any(Attendance.class));
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMemberWithInvalidPass_whenRegisterCheckIn_thenThrowIllegalStateException() {
        // Given
        Member member = new Member();
        member.setPassValid(false);
        member.setExpiryDate(LocalDate.now().plusDays(10));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> attendanceService.registerCheckIn(1L));

        // Then
        assertEquals("Your pass is invalid", exception.getMessage());
        assertFalse(member.isPassValid());
        verify(memberRepository).findById(1L);
        verify(memberRepository, never()).save(any(Member.class));
        verify(attendanceRepository, never()).save(any(Attendance.class));
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenDateRange_whenGetAttendanceHistory_thenQueryBetweenDates() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Attendance attendance = new Attendance();
        attendance.setId(5L);
        attendance.setCheckInTime(LocalDateTime.of(2024, 5, 15, 10, 30));

        Member member = new Member();
        member.setId(1L);
        member.setName("Jan");
        member.setSurname("Kowalski");
        attendance.setMember(member);

        LocalDate startDate = LocalDate.of(2024, 5, 1);
        LocalDate endDate = LocalDate.of(2024, 5, 31);
        when(attendanceRepository.findAllByCheckInTimeBetween(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                pageable
        )).thenReturn(new PageImpl<>(List.of(attendance), pageable, 1));

        // When
        Page<AttendanceResponse> response = attendanceService.getAttendanceHistory(startDate, endDate, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(5L, response.getContent().get(0).id());
        assertEquals(1L, response.getContent().get(0).memberId());
        assertEquals("Jan", response.getContent().get(0).memberName());
        assertEquals("Kowalski", response.getContent().get(0).memberSurname());
        verify(attendanceRepository).findAllByCheckInTimeBetween(
                startDate.atStartOfDay(),
                endDate.atTime(LocalTime.MAX),
                pageable
        );
        verifyNoMoreInteractions(attendanceRepository);
    }

    @Test
    void givenNoDateRange_whenGetAttendanceHistory_thenQueryAllAttendances() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        Attendance attendance = new Attendance();
        attendance.setId(7L);
        attendance.setCheckInTime(LocalDateTime.of(2024, 6, 1, 8, 15));

        Member member = new Member();
        member.setId(2L);
        member.setName("Anna");
        member.setSurname("Nowak");
        attendance.setMember(member);

        when(attendanceRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(attendance), pageable, 1));

        // When
        Page<AttendanceResponse> response = attendanceService.getAttendanceHistory(null, null, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(7L, response.getContent().get(0).id());
        assertEquals(2L, response.getContent().get(0).memberId());
        assertEquals("Anna", response.getContent().get(0).memberName());
        assertEquals("Nowak", response.getContent().get(0).memberSurname());
        verify(attendanceRepository).findAll(pageable);
        verifyNoMoreInteractions(attendanceRepository);
    }
}
