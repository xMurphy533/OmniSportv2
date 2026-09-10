package pl.omnisport.api.member;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.omnisport.api.auth.MemberRegisterRequest;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachMapper;
import pl.omnisport.api.coach.CoachRepository;

import org.springframework.data.domain.Pageable;
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

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final CoachRepository coachRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoachingContractRepository coachingContractRepository;
    private final CoachingContractService coachingContractService;
    private final CoachMapper coachMapper;
    private final PaymentRepository paymentRepository;

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

    public MemberGetByIdResponse getMemberById(Long memberId){
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

        return new MemberGetByIdResponse(
                member.getId(),
                member.getName(),
                member.getSurname(),
                member.getAge(),
                member.getSection(),
                coachResponse,
                member.isPassValid(),
                member.getExpiryDate(),
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

    public void extendPassValidity(Long memberId, BigDecimal amount){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found"));

        Payment payment = new Payment();
        payment.setMember(member);
        payment.setAmount(amount);
        payment.setPaymentDate(LocalDateTime.now());
        paymentRepository.save(payment);

        LocalDate newDatePassValidity = member.getExpiryDate().isAfter(LocalDate.now()) ? member.getExpiryDate().plusMonths(1) : LocalDate.now().plusMonths(1);
        member.setExpiryDate(newDatePassValidity);
        member.setPassValid(true);
        memberRepository.save(member);
    }

    public Page<OldCoachingContractResponse> getCurrentMemberContracts(Long memberId, Pageable pageable){
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );
        Page<CoachingContract> memberContractPage = coachingContractRepository.findAllByMemberIdAndIsActiveTrue(memberId, pageable);

        return memberContractPage.map(
                memberContract -> {
                    CoachResponse coachResponse = new CoachResponse(
                            memberContract.getCoach().getId(),
                            memberContract.getCoach().getName(),
                            memberContract.getCoach().getSurname(),
                            memberContract.getCoach().getSpecialization(),
                            memberContract.getCoach().getAppUser().isActive()
                    );

                    return new OldCoachingContractResponse(
                            memberContract.getId(),
                            coachResponse,
                            memberContract.getStartDate(),
                            null
                    );
                }
        );
    }
}
