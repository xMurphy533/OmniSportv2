package pl.omnisport.api.coach;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.omnisport.api.auth.CoachRegisterRequest;
import pl.omnisport.api.contracts.CoachingContract;
import pl.omnisport.api.contracts.CoachingContractRepository;
import pl.omnisport.api.contracts.CoachingContractService;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CoachingContractRepository coachingContractRepository;

    @Mock
    private CoachingContractService coachingContractService;

    @InjectMocks
    private CoachService coachService;

    @Test
    void givenNewCoachRequest_whenSaveNewCoach_thenPersistUserAndCoach() {
        // Given
        CoachRegisterRequest request = CoachRegisterRequest.builder()
                .name("Jan")
                .surname("Kowalski")
                .age(32)
                .email("jan.kowalski@example.com")
                .password("secret")
                .specialization("Strength")
                .build();
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-secret");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(coachRepository.save(any(Coach.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        coachService.saveNewCoach(request);

        // Then
        ArgumentCaptor<AppUser> appUserCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(appUserCaptor.capture());
        AppUser savedUser = appUserCaptor.getValue();
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals("encoded-secret", savedUser.getPassword());
        assertEquals(Role.COACH, savedUser.getRole());
        assertTrue(savedUser.isActive());

        ArgumentCaptor<Coach> coachCaptor = ArgumentCaptor.forClass(Coach.class);
        verify(coachRepository).save(coachCaptor.capture());
        Coach savedCoach = coachCaptor.getValue();
        assertEquals(request.getName(), savedCoach.getName());
        assertEquals(request.getSurname(), savedCoach.getSurname());
        assertEquals(request.getAge(), savedCoach.getAge());
        assertEquals(request.getSpecialization(), savedCoach.getSpecialization());
        assertEquals(savedUser, savedCoach.getAppUser());
        verify(appUserRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verifyNoMoreInteractions(appUserRepository, passwordEncoder, coachRepository);
    }

    @Test
    void givenExistingEmail_whenSaveNewCoach_thenThrowIllegalArgumentException() {
        // Given
        CoachRegisterRequest request = CoachRegisterRequest.builder()
                .name("Jan")
                .surname("Kowalski")
                .age(32)
                .email("jan.kowalski@example.com")
                .password("secret")
                .specialization("Strength")
                .build();
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> coachService.saveNewCoach(request));

        // Then
        assertEquals(
                "An account with the address jan.kowalski@example.com already exists in the system",
                exception.getMessage()
        );
        verify(appUserRepository).existsByEmail(request.getEmail());
        verifyNoMoreInteractions(appUserRepository);
        verifyNoMoreInteractions(passwordEncoder, coachRepository);
    }

    @Test
    void givenCoachesPage_whenGetAllCoaches_thenMapToResponsePage() {
        // Given
        AppUser activeUser = new AppUser();
        activeUser.setActive(true);

        Coach activeCoach = new Coach();
        activeCoach.setId(1L);
        activeCoach.setName("Jan");
        activeCoach.setSurname("Kowalski");
        activeCoach.setSpecialization("Strength");
        activeCoach.setAppUser(activeUser);

        Coach inactiveCoach = new Coach();
        inactiveCoach.setId(2L);
        inactiveCoach.setName("Anna");
        inactiveCoach.setSurname("Nowak");
        inactiveCoach.setSpecialization("Mobility");
        inactiveCoach.setAppUser(null);

        PageRequest pageable = PageRequest.of(0, 10);
        when(coachRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(activeCoach, inactiveCoach), pageable, 2));

        // When
        Page<CoachResponse> response = coachService.getAllCoaches(pageable);

        // Then
        assertEquals(2, response.getTotalElements());
        assertEquals(1L, response.getContent().get(0).getId());
        assertEquals("Jan", response.getContent().get(0).getName());
        assertTrue(response.getContent().get(0).isActive());
        assertEquals(2L, response.getContent().get(1).getId());
        assertEquals("Anna", response.getContent().get(1).getName());
        assertFalse(response.getContent().get(1).isActive());
        verify(coachRepository).findAll(pageable);
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenExistingCoachId_whenGetCoachById_thenReturnCoachDetails() {
        // Given
        AppUser appUser = new AppUser();
        appUser.setActive(true);

        Coach coach = new Coach();
        coach.setId(10L);
        coach.setName("Jan");
        coach.setSurname("Kowalski");
        coach.setAge(35);
        coach.setSpecialization("Strength");
        coach.setAppUser(appUser);
        when(coachRepository.findById(10L)).thenReturn(Optional.of(coach));

        // When
        CoachGetByIdResponse response = coachService.getCoachById(10L);

        // Then
        assertEquals(10L, response.getId());
        assertEquals("Jan", response.getName());
        assertEquals("Kowalski", response.getSurname());
        assertEquals(35, response.getAge());
        assertEquals("Strength", response.getSpecialization());
        assertTrue(response.isActive());
        verify(coachRepository).findById(10L);
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenMissingCoachId_whenGetCoachById_thenThrowEntityNotFoundException() {
        // Given
        when(coachRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachService.getCoachById(10L));

        // Then
        assertEquals("Coach not found", exception.getMessage());
        verify(coachRepository).findById(10L);
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenExistingCoachId_whenGetAllMentees_thenReturnMappedPage() {
        // Given
        Member member = new Member();
        member.setId(1L);
        member.setName("Marek");
        member.setSurname("Nowak");
        member.setSection("Gym");

        PageRequest pageable = PageRequest.of(0, 10);
        when(coachRepository.existsById(5L)).thenReturn(true);
        when(coachingContractRepository.findActiveMembersByCoachId(5L, pageable))
                .thenReturn(new PageImpl<>(List.of(member), pageable, 1));

        // When
        Page<MenteeResponse> response = coachService.getAllMentees(5L, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(1L, response.getContent().get(0).getId());
        assertEquals("Marek", response.getContent().get(0).getName());
        assertEquals("Nowak", response.getContent().get(0).getSurname());
        assertEquals("Gym", response.getContent().get(0).getSection());
        verify(coachRepository).existsById(5L);
        verify(coachingContractRepository).findActiveMembersByCoachId(5L, pageable);
        verifyNoMoreInteractions(coachRepository, coachingContractRepository);
    }

    @Test
    void givenMissingCoachId_whenGetAllMentees_thenThrowEntityNotFoundException() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        when(coachRepository.existsById(5L)).thenReturn(false);

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachService.getAllMentees(5L, pageable));

        // Then
        assertEquals("Coach not found", exception.getMessage());
        verify(coachRepository).existsById(5L);
        verify(coachingContractRepository, never()).findActiveMembersByCoachId(any(), any());
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenExistingCoach_whenUpdateCoachSpecialization_thenChangeSpecialization() {
        // Given
        Coach coach = new Coach();
        coach.setSpecialization("Strength");
        when(coachRepository.findById(7L)).thenReturn(Optional.of(coach));

        // When
        coachService.updateCoachSpecialization(7L, "Mobility");

        // Then
        assertEquals("Mobility", coach.getSpecialization());
        verify(coachRepository).findById(7L);
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenMissingCoach_whenUpdateCoachSpecialization_thenThrowEntityNotFoundException() {
        // Given
        when(coachRepository.findById(7L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachService.updateCoachSpecialization(7L, "Mobility"));

        // Then
        assertEquals("Coach not found", exception.getMessage());
        verify(coachRepository).findById(7L);
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenAvailableCoachAndMember_whenAddMenteeToCoach_thenCreateAndPersistContract() {
        // Given
        Member member = new Member();
        member.setId(2L);
        member.setContracts(new ArrayList<>());

        Coach coach = new Coach();
        coach.setId(1L);
        coach.setContracts(new ArrayList<>());

        CoachingContract contract = new CoachingContract();
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(coachRepository.findById(1L)).thenReturn(Optional.of(coach));
        when(coachingContractService.createContract(1L, 2L)).thenReturn(contract);
        when(coachingContractRepository.save(contract)).thenReturn(contract);

        // When
        coachService.addMenteeToCoach(1L, 2L);

        // Then
        assertTrue(coach.getContracts().contains(contract));
        assertTrue(member.getContracts().contains(contract));
        verify(memberRepository).findById(2L);
        verify(coachRepository).findById(1L);
        verify(coachingContractService).createContract(1L, 2L);
        verify(coachingContractRepository).save(contract);
        verifyNoMoreInteractions(memberRepository, coachRepository, coachingContractService, coachingContractRepository);
    }

    @Test
    void givenMissingMember_whenAddMenteeToCoach_thenThrowEntityNotFoundException() {
        // Given
        when(memberRepository.findById(2L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachService.addMenteeToCoach(1L, 2L));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(2L);
        verify(coachRepository, never()).findById(any());
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMissingCoach_whenAddMenteeToCoach_thenThrowEntityNotFoundException() {
        // Given
        Member member = new Member();
        member.setId(2L);
        member.setContracts(new ArrayList<>());
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(coachRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachService.addMenteeToCoach(1L, 2L));

        // Then
        assertEquals("Coach not found", exception.getMessage());
        verify(memberRepository).findById(2L);
        verify(coachRepository).findById(1L);
        verify(coachingContractService, never()).createContract(any(), any());
        verifyNoMoreInteractions(memberRepository, coachRepository);
    }

    @Test
    void givenActiveContractWithCoach_whenAddMenteeToCoach_thenThrowIllegalStateException() {
        // Given
        Coach existingCoach = new Coach();
        existingCoach.setId(1L);

        CoachingContract activeContract = new CoachingContract();
        activeContract.setActive(true);
        activeContract.setCoach(existingCoach);

        Member member = new Member();
        member.setId(2L);
        member.setContracts(new ArrayList<>(List.of(activeContract)));

        Coach coach = new Coach();
        coach.setId(1L);
        coach.setContracts(new ArrayList<>());

        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));
        when(coachRepository.findById(1L)).thenReturn(Optional.of(coach));

        // When
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> coachService.addMenteeToCoach(1L, 2L));

        // Then
        assertEquals("Member is already signed to this coach", exception.getMessage());
        verify(memberRepository).findById(2L);
        verify(coachRepository).findById(1L);
        verify(coachingContractService, never()).createContract(any(), any());
        verify(coachingContractRepository, never()).save(any());
        verifyNoMoreInteractions(memberRepository, coachRepository);
    }

    @Test
    void givenActiveContract_whenRemoveMenteeFromCoach_thenDeactivateContract() {
        // Given
        Coach coach = new Coach();
        coach.setId(1L);

        CoachingContract activeContract = new CoachingContract();
        activeContract.setActive(true);
        activeContract.setCoach(coach);

        Member member = new Member();
        member.setId(2L);
        member.setContracts(new ArrayList<>(List.of(activeContract)));

        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

        // When
        coachService.removeMenteeFromCoach(1L, 2L);

        // Then
        assertFalse(activeContract.isActive());
        assertEquals(LocalDate.now(), activeContract.getEndDate());
        verify(memberRepository).findById(2L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMissingMember_whenRemoveMenteeFromCoach_thenThrowEntityNotFoundException() {
        // Given
        when(memberRepository.findById(2L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachService.removeMenteeFromCoach(1L, 2L));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(2L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMissingActiveContract_whenRemoveMenteeFromCoach_thenThrowIllegalArgumentException() {
        // Given
        Coach coach = new Coach();
        coach.setId(1L);

        CoachingContract inactiveContract = new CoachingContract();
        inactiveContract.setActive(false);
        inactiveContract.setCoach(coach);

        Member member = new Member();
        member.setId(2L);
        member.setContracts(new ArrayList<>(List.of(inactiveContract)));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(member));

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> coachService.removeMenteeFromCoach(1L, 2L));

        // Then
        assertEquals("This member doesn't have an active contract with this coach", exception.getMessage());
        verify(memberRepository).findById(2L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenExistingCoach_whenDeactivateCoach_thenDisableUser() {
        // Given
        AppUser user = new AppUser();
        user.setActive(true);

        Coach coach = new Coach();
        coach.setAppUser(user);
        when(coachRepository.findById(3L)).thenReturn(Optional.of(coach));

        // When
        coachService.deactivateCoach(3L);

        // Then
        assertFalse(user.isActive());
        verify(coachRepository).findById(3L);
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenCoachWithoutUser_whenDeactivateCoach_thenDoNothingBeyondLookup() {
        // Given
        Coach coach = new Coach();
        coach.setAppUser(null);
        when(coachRepository.findById(3L)).thenReturn(Optional.of(coach));

        // When
        coachService.deactivateCoach(3L);

        // Then
        verify(coachRepository).findById(3L);
        verifyNoMoreInteractions(coachRepository);
    }

    @Test
    void givenMissingCoach_whenDeactivateCoach_thenThrowEntityNotFoundException() {
        // Given
        when(coachRepository.findById(3L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> coachService.deactivateCoach(3L));

        // Then
        assertEquals("Coach not found", exception.getMessage());
        verify(coachRepository).findById(3L);
        verifyNoMoreInteractions(coachRepository);
    }
}
