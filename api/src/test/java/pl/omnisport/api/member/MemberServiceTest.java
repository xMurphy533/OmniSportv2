package pl.omnisport.api.member;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.omnisport.api.member.MemberMapper;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachRepository;
import pl.omnisport.api.member.MemberRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private MemberMapper memberMapper;

    @InjectMocks
    private MemberService memberService;

    @Test
    void shouldSaveNewMember() {
        // GIVEN
        MemberRequest request = createMemberRequest(1L, "Jan", "Kowalski", 21, "K1");
        Member member = createMember(1L, "Jan", "Kowalski", 21, "K1", false, LocalDate.now());
        Coach coach = createCoach(1L, "Adam", "Nowak", 35, "BJJ");

        when(memberMapper.toEntity(request)).thenReturn(member);
        when(coachRepository.findById(request.getCoachId())).thenReturn(Optional.of(coach));

        // WHEN
        memberService.saveNewMember(request);

        // THEN
        assertEquals(coach, member.getCoach(), "Coach should be assigned to member");
        verify(memberMapper).toEntity(request);
        verify(coachRepository).findById(request.getCoachId());
        verify(memberRepository).save(member);
    }

    @Test
    void shouldThrowExceptionWhenSavingMemberWithoutCoach() {
        // GIVEN
        MemberRequest request = createMemberRequest(999L, "Jan", "Kowalski", 21, "Gym");
        Member member = createMember(1L, "Jan", "Kowalski", 21, "Gym", false, LocalDate.now());

        when(memberMapper.toEntity(request)).thenReturn(member);
        when(coachRepository.findById(request.getCoachId())).thenReturn(Optional.empty());

        // WHEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> memberService.saveNewMember(request),
                "Should throw EntityNotFoundException when coach is missing"
        );

        // THEN
        assertEquals("Coach not found", exception.getMessage(), "Exception message should match");
        verify(memberMapper).toEntity(request);
        verify(coachRepository).findById(request.getCoachId());
    }

    @Test
    void shouldGetAllMembers() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Member> members = createMembersList(3);
        Page<Member> expectedPage = new PageImpl<>(members, pageable, 3);

        when(memberRepository.findAll(pageable)).thenReturn(expectedPage);

        // WHEN
        Page<Member> result = memberService.getAllMembers(pageable);

        // THEN
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.getContent().size(), "Page should contain 3 members");
        assertEquals(3, result.getTotalElements(), "Total elements should be 3");
        assertEquals(1, result.getTotalPages(), "Total pages should be 1");
        assertTrue(result.isFirst(), "Should be first page");
        assertTrue(result.isLast(), "Should be last page");
        verify(memberRepository).findAll(pageable);
    }

    @Test
    void shouldRemoveMember() {
        // GIVEN
        Long memberId = 1L;

        // WHEN
        memberService.removeMember(memberId);

        // THEN
        verify(memberRepository).deleteById(memberId);
    }

    @Test
    void shouldExtendPassValidity() {
        // GIVEN
        Long memberId = 1L;
        Member member = createMember(memberId, "Jan", "Kowalski", 21, "Gym", true, LocalDate.now());

        when(memberRepository.findById(memberId)).thenReturn(Optional.of(member));

        // WHEN
        memberService.extendPassValidity(memberId);

        // THEN
        assertEquals(LocalDate.now().plusMonths(1), member.getExpiryDate(), "Expiry date should be extended by one month");
        verify(memberRepository).findById(memberId);
        verify(memberRepository).save(member);
    }

    @Test
    void shouldThrowExceptionWhenExtendingPassForMissingMember() {
        // GIVEN
        Long memberId = 999L;

        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // WHEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> memberService.extendPassValidity(memberId),
                "Should throw EntityNotFoundException when member is missing"
        );

        // THEN
        assertEquals("Member not found", exception.getMessage(), "Exception message should match");
        verify(memberRepository).findById(memberId);
    }

    private Member createMember(Long id, String name, String surname, int age, String section, boolean passValid, LocalDate expiryDate) {
        Member member = new Member();
        member.setId(id);
        member.setName(name);
        member.setSurname(surname);
        member.setAge(age);
        member.setSection(section);
        member.setPassValid(passValid);
        member.setExpiryDate(expiryDate);
        return member;
    }

    private Coach createCoach(Long id, String name, String surname, int age, String specialization) {
        Coach coach = new Coach();
        coach.setId(id);
        coach.setName(name);
        coach.setSurname(surname);
        coach.setAge(age);
        coach.setSpecialization(specialization);
        return coach;
    }

    private List<Member> createMembersList(int count) {
        List<Member> members = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            members.add(createMember((long) i, "Member" + i, "Test" + i, 18 + i, "Section" + i, true, LocalDate.now()));
        }
        return members;
    }

    private MemberRequest createMemberRequest(Long coachId, String name, String surname, int age, String section) {
        return new MemberRequest(name, surname, age, section, coachId);
    }
}
