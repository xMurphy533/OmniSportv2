package pl.omnisport.api.contracts;

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
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachRepository;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;
import pl.omnisport.api.user.AppUser;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachingContractServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CoachingContractRepository coachingContractRepository;

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private CoachingContractService coachingContractService;

    @Test
    void givenExistingMember_whenGetMemberCoachingHistory_thenMapPastContracts() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);

        AppUser coachUser = new AppUser();
        coachUser.setActive(true);

        Coach coach = new Coach();
        coach.setId(1L);
        coach.setName("Jan");
        coach.setSurname("Kowalski");
        coach.setSpecialization("Strength");
        coach.setAppUser(coachUser);

        CoachingContract contract = new CoachingContract();
        contract.setId(11L);
        contract.setCoach(coach);
        contract.setStartDate(LocalDate.of(2024, 1, 10));
        contract.setEndDate(LocalDate.of(2024, 6, 10));

        when(memberRepository.existsById(2L)).thenReturn(true);
        when(coachingContractRepository.findOldContractsByMemberId(2L, pageable))
                .thenReturn(new PageImpl<>(List.of(contract), pageable, 1));

        // When
        Page<OldCoachingContractResponse> response = coachingContractService.getMemberCoachingHistory(2L, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(11L, response.getContent().get(0).getId());
        assertEquals(1L, response.getContent().get(0).getCoachResponse().getId());
        assertEquals("Jan", response.getContent().get(0).getCoachResponse().getName());
        assertTrue(response.getContent().get(0).getCoachResponse().isActive());
        assertEquals(LocalDate.of(2024, 1, 10), response.getContent().get(0).getStartDate());
        assertEquals(LocalDate.of(2024, 6, 10), response.getContent().get(0).getEndDate());
        verify(memberRepository).existsById(2L);
        verify(coachingContractRepository).findOldContractsByMemberId(2L, pageable);
        verifyNoMoreInteractions(memberRepository, coachingContractRepository, coachRepository);
    }

    @Test
    void givenMissingMember_whenGetMemberCoachingHistory_thenThrowEntityNotFoundException() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        when(memberRepository.existsById(2L)).thenReturn(false);

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachingContractService.getMemberCoachingHistory(2L, pageable));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).existsById(2L);
        verify(coachingContractRepository, never()).findOldContractsByMemberId(any(), any());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenExistingCoach_whenGetPastCoachMentees_thenMapPastContracts() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);

        AppUser memberUser = new AppUser();
        memberUser.setActive(true);

        Member member = new Member();
        member.setId(3L);
        member.setName("Anna");
        member.setSurname("Nowak");
        member.setSection("Gym");
        member.setAppUser(memberUser);

        CoachingContract contract = new CoachingContract();
        contract.setId(12L);
        contract.setMember(member);
        contract.setStartDate(LocalDate.of(2024, 2, 1));
        contract.setEndDate(LocalDate.of(2024, 7, 1));

        when(coachRepository.existsById(4L)).thenReturn(true);
        when(coachingContractRepository.findOldContractsByCoachId(4L, pageable))
                .thenReturn(new PageImpl<>(List.of(contract), pageable, 1));

        // When
        Page<PastMenteeResponse> response = coachingContractService.getPastCoachMentees(4L, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(12L, response.getContent().get(0).getId());
        assertEquals(3L, response.getContent().get(0).getMemberResponse().getId());
        assertEquals("Anna", response.getContent().get(0).getMemberResponse().getName());
        assertTrue(response.getContent().get(0).getMemberResponse().isActive());
        assertEquals(LocalDate.of(2024, 2, 1), response.getContent().get(0).getStartDate());
        assertEquals(LocalDate.of(2024, 7, 1), response.getContent().get(0).getEndDate());
        verify(coachRepository).existsById(4L);
        verify(coachingContractRepository).findOldContractsByCoachId(4L, pageable);
        verifyNoMoreInteractions(coachRepository, coachingContractRepository, memberRepository);
    }

    @Test
    void givenMissingCoach_whenGetPastCoachMentees_thenThrowEntityNotFoundException() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        when(coachRepository.existsById(4L)).thenReturn(false);

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachingContractService.getPastCoachMentees(4L, pageable));

        // Then
        assertEquals("Coach not found", exception.getMessage());
        verify(coachRepository).existsById(4L);
        verify(coachingContractRepository, never()).findOldContractsByCoachId(any(), any());
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenAvailableCoachAndMember_whenCreateContract_thenPersistActiveContract() {
        // Given
        AppUser coachUser = new AppUser();
        Coach coach = new Coach();
        coach.setId(5L);
        coach.setAppUser(coachUser);

        Member member = new Member();
        member.setId(6L);
        member.setContracts(new ArrayList<>());

        when(coachRepository.findById(5L)).thenReturn(Optional.of(coach));
        when(memberRepository.findById(6L)).thenReturn(Optional.of(member));
        when(coachingContractRepository.save(any(CoachingContract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        CoachingContract contract = coachingContractService.createContract(5L, 6L);

        // Then
        assertEquals(coach, contract.getCoach());
        assertEquals(member, contract.getMember());
        assertEquals(LocalDate.now(), contract.getStartDate());
        assertTrue(contract.isActive());
        verify(coachRepository).findById(5L);
        verify(memberRepository).findById(6L);
        verify(coachingContractRepository).save(contract);
        verifyNoMoreInteractions(coachRepository, memberRepository, coachingContractRepository);
    }

    @Test
    void givenMissingCoach_whenCreateContract_thenThrowEntityNotFoundException() {
        // Given
        when(coachRepository.findById(5L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachingContractService.createContract(5L, 6L));

        // Then
        assertEquals("Coach not found", exception.getMessage());
        verify(coachRepository).findById(5L);
        verify(memberRepository, never()).findById(any());
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenMissingMember_whenCreateContract_thenThrowEntityNotFoundException() {
        // Given
        Coach coach = new Coach();
        when(coachRepository.findById(5L)).thenReturn(Optional.of(coach));
        when(memberRepository.findById(6L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachingContractService.createContract(5L, 6L));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(coachRepository).findById(5L);
        verify(memberRepository).findById(6L);
        verify(coachingContractRepository, never()).save(any());
        verifyNoMoreInteractions(coachRepository, memberRepository);
    }

    @Test
    void givenActiveContractWithSameCoach_whenCreateContract_thenThrowIllegalStateException() {
        // Given
        Coach coach = new Coach();
        coach.setId(5L);

        CoachingContract activeContract = new CoachingContract();
        activeContract.setActive(true);
        activeContract.setCoach(coach);

        Member member = new Member();
        member.setId(6L);
        member.setContracts(new ArrayList<>(List.of(activeContract)));

        when(coachRepository.findById(5L)).thenReturn(Optional.of(coach));
        when(memberRepository.findById(6L)).thenReturn(Optional.of(member));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> coachingContractService.createContract(5L, 6L));

        // Then
        assertEquals("Member is already training with this coach", exception.getMessage());
        verify(coachRepository).findById(5L);
        verify(memberRepository).findById(6L);
        verify(coachingContractRepository, never()).save(any());
        verifyNoMoreInteractions(coachRepository, memberRepository);
    }
}
