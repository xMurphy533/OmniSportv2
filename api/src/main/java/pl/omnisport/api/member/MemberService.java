package pl.omnisport.api.member;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.omnisport.api.auth.MemberRegisterRequest;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachRepository;

import org.springframework.data.domain.Pageable;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CoachRepository coachRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void saveNewMember(MemberRegisterRequest request){
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with the address " + request.getEmail() + " already exists in the system");
        }
        AppUser appUser = new AppUser();
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(Role.MEMBER);
        appUser.setActive(true);

        appUserRepository.save(appUser);

        Member member = new Member();
        member.setName(request.getName());
        member.setSurname(request.getSurname());
        member.setAge(request.getAge());
        member.setSection(request.getSection());
        member.setPassValid(request.isPassValid());
        if(request.isPassValid()){
            member.setExpiryDate(LocalDate.now().plusMonths(1));
        } else {
            member.setExpiryDate(null);
        }
        if(request.getCoachId() != null){
            Coach coach = coachRepository.findById(request.getCoachId()).orElseThrow(
                    () -> new EntityNotFoundException("Coach with this ID doesn't exist")
            );
            member.setCoach(coach);
            coach.addMemberToList(member);
        }

        if(member.isPassValid()){
            member.setExpiryDate(LocalDate.now().plusMonths(1));
        }
        else {
            member.setExpiryDate(null);
        }
        member.setAppUser(appUser);

        memberRepository.save(member);
    }

    public Page<Member> getAllMembers(Pageable pageable){
        return memberRepository.findAll(pageable);
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
