package pl.omnisport.api.coach;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

@Service
@RequiredArgsConstructor
public class CoachService {
    private final CoachRepository coachRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final CoachingContractRepository coachingContractRepository;
    private final CoachingContractService coachingContractService;

    public void saveNewCoach(CoachRegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with the address " + request.getEmail() + " already exists in the system");
        }
        AppUser appUser = new AppUser();
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(Role.COACH);
        appUser.setActive(true);

        appUserRepository.save(appUser);

        Coach coach = new Coach();
        coach.setName(request.getName());
        coach.setSurname(request.getSurname());
        coach.setAge(request.getAge());
        coach.setSpecialization(request.getSpecialization());
        coach.setAppUser(appUser);

        coachRepository.save(coach);
    }

    public Page<CoachResponse> getAllCoaches(Pageable pageable) {
        Page<Coach> coachPage = coachRepository.findAll(pageable);
        return coachPage.map(
                coach -> {
                    boolean active = false;
                    if(coach.getAppUser() != null)
                        active = coach.getAppUser().isActive();
                    return new CoachResponse(
                            coach.getId(),
                            coach.getName(),
                            coach.getSurname(),
                            coach.getSpecialization(),
                            active
                    );
                }
        );
    }

    public CoachGetByIdResponse getCoachById(Long id) {
        Coach coach = coachRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        return new CoachGetByIdResponse(
                coach.getId(),
                coach.getName(),
                coach.getSurname(),
                coach.getAge(),
                coach.getSpecialization(),
                coach.getAppUser().isActive()
        );
    }

    public Page<MenteeResponse> getAllMentees(Long coachId, Pageable pageable) {
        if(!coachRepository.existsById(coachId))
            throw new EntityNotFoundException("Coach not found");
        Page<Member> memberPage = coachingContractRepository.findActiveMembersByCoachId(coachId, pageable);
        return memberPage.map(member -> new MenteeResponse(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getSection()
        ));
    }

    @Transactional
    public void updateCoachSpecialization(Long id, String newSpec) {
        Coach coach = coachRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        coach.setSpecialization(newSpec);
    }

    @Transactional
    public void addMenteeToCoach(Long coachId, Long memberId){
        Member mentee = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );
        Coach coach = coachRepository.findById(coachId).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        boolean alreadyTrainsWithThisCoach = mentee.getContracts().stream()
                .anyMatch(contract -> contract.isActive() && contract.getCoach().getId().equals(coachId));

        if(alreadyTrainsWithThisCoach) {
            throw new IllegalStateException("Member is already signed to this coach");
        }

        CoachingContract newContract = coachingContractService.createContract(coachId, memberId);

        coach.getContracts().add(newContract);
        mentee.getContracts().add(newContract);

        coachingContractRepository.save(newContract);
    }

    @Transactional
    public void removeMenteeFromCoach(Long coachId, Long memberId){
        Member mentee = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );

        CoachingContract activeContract = mentee.getContracts().stream()
                .filter(contract -> contract.isActive() && contract.getCoach().getId().equals(coachId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("This member doesn't have an active contract with this coach"));

        activeContract.setActive(false);
        activeContract.setEndDate(LocalDate.now());
    }

    @Transactional
    public void deactivateCoach (Long id){
        Coach coach = coachRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        AppUser coachsUser = coach.getAppUser();
        if(coachsUser != null){
            coachsUser.setActive(false);
        }
    }
}
