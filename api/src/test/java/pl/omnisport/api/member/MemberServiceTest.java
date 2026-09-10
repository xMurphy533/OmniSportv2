package pl.omnisport.api.member;

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
import pl.omnisport.api.auth.MemberRegisterRequest;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachMapper;
import pl.omnisport.api.coach.CoachRepository;
import pl.omnisport.api.coach.CoachResponse;
import pl.omnisport.api.contracts.CoachingContract;
import pl.omnisport.api.contracts.CoachingContractRepository;
import pl.omnisport.api.contracts.CoachingContractService;
import pl.omnisport.api.contracts.OldCoachingContractResponse;
import pl.omnisport.api.payment.Payment;
import pl.omnisport.api.payment.PaymentRepository;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CoachingContractRepository coachingContractRepository;

    @Mock
    private CoachingContractService coachingContractService;

    @Mock
    private CoachMapper coachMapper;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private MemberService memberService;

    @Test
    void givenNewMemberRequestWithoutCoach_whenSaveNewMember_thenPersistUserAndMember() {
        // Given
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .name("Jan")
                .surname("Kowalski")
                .age(21)
                .email("jan.kowalski@example.com")
                .password("secret")
                .section("Gym")
                .coachId(null)
                .isPassValid(true)
                .build();
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-secret");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        memberService.saveNewMember(request);

        // Then
        ArgumentCaptor<AppUser> appUserCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(appUserCaptor.capture());
        AppUser savedUser = appUserCaptor.getValue();
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals("encoded-secret", savedUser.getPassword());
        assertEquals(Role.MEMBER, savedUser.getRole());
        assertTrue(savedUser.isActive());

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertEquals(request.getName(), savedMember.getName());
        assertEquals(request.getSurname(), savedMember.getSurname());
        assertEquals(request.getAge(), savedMember.getAge());
        assertEquals(request.getSection(), savedMember.getSection());
        assertTrue(savedMember.isPassValid());
        assertNotNull(savedMember.getExpiryDate());
        assertEquals(savedUser, savedMember.getAppUser());
        verifyNoMoreInteractions(appUserRepository, passwordEncoder, memberRepository, coachRepository, coachingContractService, coachingContractRepository, paymentRepository, coachMapper);
    }

    @Test
    void givenNewMemberWithCoach_whenSaveNewMember_thenCreateAndLinkContract() {
        // Given
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .name("Anna")
                .surname("Nowak")
                .age(19)
                .email("anna.nowak@example.com")
                .password("secret")
                .section("Yoga")
                .coachId(5L)
                .isPassValid(false)
                .build();

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-secret");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member member = invocation.getArgument(0);
            member.setId(99L);
            return member;
        });

        Coach coach = new Coach();
        coach.setId(5L);
        coach.setContracts(new ArrayList<>());
        when(coachRepository.findById(5L)).thenReturn(Optional.of(coach));

        CoachingContract contract = new CoachingContract();
        when(coachingContractService.createContract(5L, 99L)).thenReturn(contract);

        // When
        memberService.saveNewMember(request);

        // Then
        verify(appUserRepository).save(any(AppUser.class));
        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());
        Member savedMember = memberCaptor.getValue();
        assertEquals(99L, savedMember.getId());
        assertNull(savedMember.getExpiryDate());
        assertTrue(coach.getContracts().contains(contract));
        assertTrue(savedMember.getContracts().contains(contract));
        verify(coachingContractService).createContract(5L, 99L);
        verify(coachRepository).save(coach);
        verify(coachingContractRepository).save(contract);
        verifyNoMoreInteractions(appUserRepository, passwordEncoder, memberRepository, coachRepository, coachingContractService, coachingContractRepository, paymentRepository, coachMapper);
    }

    @Test
    void givenExistingEmail_whenSaveNewMember_thenThrowIllegalArgumentException() {
        // Given
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .email("jan.kowalski@example.com")
                .password("secret")
                .build();
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> memberService.saveNewMember(request));

        // Then
        assertEquals(
                "An account with the address jan.kowalski@example.com already exists in the system",
                exception.getMessage()
        );
        verify(appUserRepository).existsByEmail(request.getEmail());
        verifyNoMoreInteractions(appUserRepository);
        verifyNoMoreInteractions(passwordEncoder, memberRepository, coachRepository, coachingContractService, coachingContractRepository, paymentRepository, coachMapper);
    }

    @Test
    void givenMissingCoach_whenSaveNewMemberWithCoach_thenThrowEntityNotFoundException() {
        // Given
        MemberRegisterRequest request = MemberRegisterRequest.builder()
                .name("Anna")
                .surname("Nowak")
                .age(19)
                .email("anna.nowak@example.com")
                .password("secret")
                .section("Yoga")
                .coachId(5L)
                .isPassValid(true)
                .build();

        AppUser savedUser = new AppUser();
        savedUser.setId(1L);
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-secret");
        when(appUserRepository.save(any(AppUser.class))).thenReturn(savedUser);

        Member savedMember = new Member();
        savedMember.setId(99L);
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);
        when(coachRepository.findById(5L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.saveNewMember(request));

        // Then
        assertEquals("Coach with this ID doesn't exist", exception.getMessage());
        verify(appUserRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(appUserRepository).save(any(AppUser.class));
        verify(memberRepository).save(any(Member.class));
        verify(coachRepository).findById(5L);
        verify(coachingContractService, never()).createContract(any(), any());
        verifyNoMoreInteractions(appUserRepository, passwordEncoder, memberRepository, coachRepository);
    }

    @Test
    void givenMembersPage_whenGetAllMembers_thenMapMemberAndCoachData() {
        // Given
        AppUser memberUser = new AppUser();
        memberUser.setActive(true);

        AppUser coachUser = new AppUser();
        coachUser.setActive(false);

        Coach coach = new Coach();
        coach.setId(7L);
        coach.setName("Piotr");
        coach.setSurname("Zieliński");
        coach.setSpecialization("Strength");
        coach.setAppUser(coachUser);

        CoachingContract activeContract = new CoachingContract();
        activeContract.setActive(true);
        activeContract.setCoach(coach);

        Member memberWithCoach = new Member();
        memberWithCoach.setId(1L);
        memberWithCoach.setName("Jan");
        memberWithCoach.setSurname("Kowalski");
        memberWithCoach.setSection("Gym");
        memberWithCoach.setAppUser(memberUser);
        memberWithCoach.setContracts(new ArrayList<>(List.of(activeContract)));

        Member memberWithoutCoach = new Member();
        memberWithoutCoach.setId(2L);
        memberWithoutCoach.setName("Anna");
        memberWithoutCoach.setSurname("Nowak");
        memberWithoutCoach.setSection("Yoga");
        memberWithoutCoach.setContracts(new ArrayList<>());

        PageRequest pageable = PageRequest.of(0, 10);
        when(memberRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(memberWithCoach, memberWithoutCoach), pageable, 2));

        // When
        Page<MemberResponse> response = memberService.getAllMembers(pageable);

        // Then
        assertEquals(2, response.getTotalElements());
        assertEquals(1L, response.getContent().get(0).getId());
        assertTrue(response.getContent().get(0).isActive());
        assertEquals(7L, response.getContent().get(0).getCoach().getId());
        assertFalse(response.getContent().get(1).isActive());
        assertNull(response.getContent().get(1).getCoach());
        verify(memberRepository).findAll(pageable);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenExistingMember_whenGetMemberById_thenReturnMemberDetails() {
        // Given
        AppUser memberUser = new AppUser();
        memberUser.setActive(true);

        AppUser coachUser = new AppUser();
        coachUser.setActive(true);

        Coach coach = new Coach();
        coach.setId(3L);
        coach.setName("Piotr");
        coach.setSurname("Zieliński");
        coach.setSpecialization("Strength");
        coach.setAppUser(coachUser);

        CoachingContract activeContract = new CoachingContract();
        activeContract.setActive(true);
        activeContract.setCoach(coach);

        Member member = new Member();
        member.setId(10L);
        member.setName("Jan");
        member.setSurname("Kowalski");
        member.setAge(21);
        member.setSection("Gym");
        member.setPassValid(true);
        member.setExpiryDate(LocalDate.of(2024, 12, 31));
        member.setAppUser(memberUser);
        member.setContracts(new ArrayList<>(List.of(activeContract)));
        when(memberRepository.findById(10L)).thenReturn(Optional.of(member));

        // When
        MemberGetByIdResponse response = memberService.getMemberById(10L);

        // Then
        assertEquals(10L, response.getId());
        assertEquals("Jan", response.getName());
        assertEquals("Kowalski", response.getSurname());
        assertEquals(21, response.getAge());
        assertEquals("Gym", response.getSection());
        assertEquals(3L, response.getCoach().getId());
        assertTrue(response.isPassValid());
        assertEquals(LocalDate.of(2024, 12, 31), response.getExpiryDate());
        assertTrue(response.isActive());
        verify(memberRepository).findById(10L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMissingMember_whenGetMemberById_thenThrowEntityNotFoundException() {
        // Given
        when(memberRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.getMemberById(10L));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(10L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenExistingMember_whenUpdateMembersSection_thenChangeSection() {
        // Given
        Member member = new Member();
        member.setSection("Gym");
        when(memberRepository.findById(11L)).thenReturn(Optional.of(member));

        // When
        memberService.updateMembersSection(11L, "Yoga");

        // Then
        assertEquals("Yoga", member.getSection());
        verify(memberRepository).findById(11L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMissingMember_whenUpdateMembersSection_thenThrowEntityNotFoundException() {
        // Given
        when(memberRepository.findById(11L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.updateMembersSection(11L, "Yoga"));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(11L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenActiveContractAndUser_whenDeactivateMember_thenDisableBoth() {
        // Given
        AppUser user = new AppUser();
        user.setActive(true);

        Coach coach = new Coach();
        coach.setId(8L);

        CoachingContract activeContract = new CoachingContract();
        activeContract.setActive(true);
        activeContract.setCoach(coach);

        Member member = new Member();
        member.setAppUser(user);
        member.setContracts(new ArrayList<>(List.of(activeContract)));
        when(memberRepository.findById(12L)).thenReturn(Optional.of(member));

        // When
        memberService.deactivateMember(12L);

        // Then
        assertFalse(user.isActive());
        assertFalse(activeContract.isActive());
        assertEquals(LocalDate.now(), activeContract.getEndDate());
        verify(memberRepository).findById(12L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenMissingMember_whenDeactivateMember_thenThrowEntityNotFoundException() {
        // Given
        when(memberRepository.findById(12L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.deactivateMember(12L));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(12L);
        verifyNoMoreInteractions(memberRepository);
    }

    @Test
    void givenValidExpiry_whenExtendPassValidity_thenPersistPaymentAndExtendByOneMonth() {
        // Given
        Member member = new Member();
        member.setExpiryDate(LocalDate.now().plusDays(10));
        when(memberRepository.findById(13L)).thenReturn(Optional.of(member));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        memberService.extendPassValidity(13L, new BigDecimal("120.00"));

        // Then
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        assertEquals(new BigDecimal("120.00"), paymentCaptor.getValue().getAmount());
        assertEquals(member, paymentCaptor.getValue().getMember());
        assertNotNull(paymentCaptor.getValue().getPaymentDate());
        assertEquals(LocalDate.now().plusDays(10).plusMonths(1), member.getExpiryDate());
        assertTrue(member.isPassValid());
        verify(memberRepository).findById(13L);
        verify(memberRepository).save(member);
        verifyNoMoreInteractions(memberRepository, paymentRepository);
    }

    @Test
    void givenExpiredPass_whenExtendPassValidity_thenSetExpiryFromToday() {
        // Given
        Member member = new Member();
        member.setExpiryDate(LocalDate.now().minusDays(1));
        when(memberRepository.findById(13L)).thenReturn(Optional.of(member));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        memberService.extendPassValidity(13L, new BigDecimal("120.00"));

        // Then
        assertEquals(LocalDate.now().plusMonths(1), member.getExpiryDate());
        assertTrue(member.isPassValid());
        verify(paymentRepository).save(any(Payment.class));
        verify(memberRepository).save(member);
        verifyNoMoreInteractions(memberRepository, paymentRepository);
    }

    @Test
    void givenMissingMember_whenExtendPassValidity_thenThrowEntityNotFoundException() {
        // Given
        when(memberRepository.findById(13L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.extendPassValidity(13L, new BigDecimal("120.00")));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(13L);
        verifyNoMoreInteractions(memberRepository);
        verifyNoMoreInteractions(paymentRepository);
    }

    @Test
    void givenExistingMember_whenGetCurrentMemberContracts_thenMapActiveContracts() {
        // Given
        AppUser coachUser = new AppUser();
        coachUser.setActive(true);

        Coach coach = new Coach();
        coach.setId(15L);
        coach.setName("Jan");
        coach.setSurname("Kowalski");
        coach.setSpecialization("Strength");
        coach.setAppUser(coachUser);

        CoachingContract contract = new CoachingContract();
        contract.setId(20L);
        contract.setCoach(coach);
        contract.setStartDate(LocalDate.of(2024, 3, 1));
        contract.setActive(true);

        PageRequest pageable = PageRequest.of(0, 10);
        when(memberRepository.findById(14L)).thenReturn(Optional.of(new Member()));
        when(coachingContractRepository.findAllByMemberIdAndIsActiveTrue(14L, pageable))
                .thenReturn(new PageImpl<>(List.of(contract), pageable, 1));

        // When
        Page<OldCoachingContractResponse> response = memberService.getCurrentMemberContracts(14L, pageable);

        // Then
        assertEquals(1, response.getTotalElements());
        assertEquals(20L, response.getContent().get(0).getId());
        assertEquals(15L, response.getContent().get(0).getCoachResponse().getId());
        assertEquals(LocalDate.of(2024, 3, 1), response.getContent().get(0).getStartDate());
        assertNull(response.getContent().get(0).getEndDate());
        verify(memberRepository).findById(14L);
        verify(coachingContractRepository).findAllByMemberIdAndIsActiveTrue(14L, pageable);
        verifyNoMoreInteractions(memberRepository, coachingContractRepository);
    }

    @Test
    void givenMissingMember_whenGetCurrentMemberContracts_thenThrowEntityNotFoundException() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        when(memberRepository.findById(14L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> memberService.getCurrentMemberContracts(14L, pageable));

        // Then
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository).findById(14L);
        verify(coachingContractRepository, never()).findAllByMemberIdAndIsActiveTrue(any(), any());
        verifyNoMoreInteractions(memberRepository);
    }
}
