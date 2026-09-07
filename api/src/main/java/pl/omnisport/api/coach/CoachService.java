package pl.omnisport.api.coach;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.omnisport.api.auth.CoachRegisterRequest;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoachService {
    private final CoachRepository coachRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

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

    // tu powinno byc zwracane Page<CoachResponse>, sprawdzić co wypluwa postman
    public Page<Coach> getAllCoaches(Pageable pageable) {
        return coachRepository.findAll(pageable);
    }

    public Optional<Coach> getCoachById(Long id) {
        return coachRepository.findById(id);
    }

    public Page<MenteeResponse> getAllMentees(Long coachId, Pageable pageable) {
        Coach coach = coachRepository.findById(coachId).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        Page<Member> menteesPage = memberRepository.findAllByCoachId(coachId, pageable);

        CoachResponse coachResponse = new CoachResponse(
                coach.getId(),
                coach.getName(),
                coach.getSurname(),
                coach.getSpecialization()
        );

        return menteesPage.map(member -> new MenteeResponse(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getSection()
        ));
    }

    public void updateCoachSpecialization(Long id, String newSpec) {
        Coach coach = getCoachById(id).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        coach.setSpecialization(newSpec);
        coachRepository.save(coach);
    }

    @Transactional
    public void addMenteeToCoach(Long coachId, Long memberId){
        Member mentee = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );
        Coach coach = coachRepository.findById(coachId).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        coach.addMemberToList(mentee);
    }

    public void removeCoach (Long id){
        Coach coach = getCoachById(id).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        coachRepository.delete(coach);
    }
}
