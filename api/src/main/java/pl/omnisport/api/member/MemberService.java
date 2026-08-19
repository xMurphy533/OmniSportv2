package pl.omnisport.api.member;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.omnisport.api.admin.MemberMapper;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachRepository;
import pl.omnisport.api.coach.MemberRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CoachRepository coachRepository;
    private final MemberMapper memberMapper;

    public void saveNewMember(MemberRequest request){
        Member member = memberMapper.toEntity(request);
        Coach coach = coachRepository.findById(request.getCoachId()).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        member.setCoach(coach);
        memberRepository.save(member);
    }

    public List<Member> getAllMembers(){
        return memberRepository.findAll();
    }

    public Optional<Member> getMemberById(Long memberId){
        return memberRepository.findById(memberId);
    }

    public Member updateMember(Long memberId, Member member){
        if(!memberRepository.existsById(memberId)){
            throw new EntityNotFoundException("Member not found");
        }

        member.setId(memberId);
        return memberRepository.save(member);
    }

    public void removeMember(Long memberId){
        memberRepository.deleteById(memberId);
    }

    public void extendPassValidity(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found"));

        LocalDate newDatePassValidity = LocalDate.now().plusMonths(1);
        member.setExpiryDate(newDatePassValidity);
        memberRepository.save(member);
    }
}
