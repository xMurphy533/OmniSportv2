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
import pl.omnisport.api.coach.CoachResponse;
import pl.omnisport.api.contracts.CoachingContract;
import pl.omnisport.api.contracts.CoachingContractRepository;
import pl.omnisport.api.contracts.CoachingContractService;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CoachRepository coachRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoachingContractRepository coachingContractRepository;
    private final CoachingContractService coachingContractService;

    @Transactional
    public void saveNewMember(MemberRegisterRequest request) {
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
        member.setAppUser(appUser);

        memberRepository.save(member);

        if (request.isPassValid()) {
            member.setExpiryDate(LocalDate.now().plusMonths(1));
        } else {
            member.setExpiryDate(null);
        }

        if (request.getCoachId() != null) {
            Coach coach = coachRepository.findById(request.getCoachId()).orElseThrow(
                    () -> new EntityNotFoundException("Coach with this ID doesn't exist")
            );

            CoachingContract contract = coachingContractService.createContract(request.getCoachId(), member.getId());

            coach.getContracts().add(contract);
            member.getContracts().add(contract);
            coachRepository.save(coach);
            coachingContractRepository.save(contract);
        }
    }

    @Transactional(readOnly = true)
    public Page<MemberResponse> getAllMembers(Pageable pageable){
        Page<Member> memberPage = memberRepository.findAll(pageable);
        return memberPage.map(
                member -> {
                    boolean memberActive = false;
                    CoachResponse coachResponse = null;

                    if(member.getAppUser() != null)
                        memberActive = member.getAppUser().isActive();

                    if(member.getCurrentCoach() != null){
                        Coach coach = member.getCurrentCoach();
                        boolean coachActive = false;

                        if(coach.getAppUser() != null)
                            coachActive = coach.getAppUser().isActive();

                        coachResponse = new CoachResponse(
                                coach.getId(),
                                coach.getName(),
                                coach.getSurname(),
                                coach.getSpecialization(),
                                coachActive
                        );
                    }
                    return new MemberResponse(
                            member.getId(),
                            member.getName(),
                            member.getSurname(),
                            member.getSection(),
                            coachResponse,
                            memberActive
                    );
                }
        );
    }

    public MemberResponse getMemberById(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );
        boolean memberActive = false;
        Coach coach = null;
        if(member.getAppUser() != null){
            memberActive = member.getAppUser().isActive();
            coach = member.getCurrentCoach();
        }
        CoachResponse coachResponse = new CoachResponse(
                coach.getId(),
                coach.getName(),
                coach.getSurname(),
                coach.getSpecialization(),
                coach.getAppUser().isActive()
        );

        return new MemberResponse(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getSection(),
                coachResponse,
                memberActive
        );
    }

    @Transactional
    public void updateMembersSection(Long memberId, String newSection){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );
        member.setSection(newSection);
    }

    @Transactional
    public void deactivateMember(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );

        member.getContracts().stream()
                .filter(CoachingContract::isActive)
                .findFirst()
                .ifPresent(activeContract -> {
                    activeContract.setActive(false);
                    activeContract.setEndDate(LocalDate.now());
                });

        AppUser membersUser = member.getAppUser();
        if(membersUser != null) {
            membersUser.setActive(false);
        }
    }

    public void extendPassValidity(Long memberId){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found"));

        LocalDate newDatePassValidity = LocalDate.now().plusMonths(1);
        member.setExpiryDate(newDatePassValidity);
        memberRepository.save(member);
    }

    @Transactional
    public void changeMembersCoach(Long newCoachId, Long memberId){
        Coach newCoach = coachRepository.findById(newCoachId).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        Member mentee = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );

        mentee.getContracts().stream()
                .filter(CoachingContract::isActive)
                .findFirst()
                .ifPresent(activeContract -> {
                    activeContract.setActive(false);
                    activeContract.setEndDate(LocalDate.now());
                });

        CoachingContract newContract = coachingContractService.createContract(newCoachId, memberId);

        mentee.getContracts().add(newContract);
    }
}
