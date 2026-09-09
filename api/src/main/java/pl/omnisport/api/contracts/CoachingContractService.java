package pl.omnisport.api.contracts;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachRepository;
import pl.omnisport.api.coach.CoachResponse;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class CoachingContractService {
    private final MemberRepository memberRepository;
    private final CoachingContractRepository coachingContractRepository;
    private final CoachRepository coachRepository;

    @Transactional
    public Page<OldCoachingContractResponse> getMemberCoachingHistory(Long memberId, Pageable pageable){
        if(!memberRepository.existsById(memberId))
            throw new EntityNotFoundException("Member not found");

        Page<CoachingContract> pastContracts = coachingContractRepository.findOldContractsByMemberId(memberId, pageable);

        return pastContracts.map(contract -> {
            Coach coach = contract.getCoach();
            CoachResponse coachResponse = new CoachResponse();
            coachResponse.setId(coach.getId());
            coachResponse.setName(coach.getName());
            coachResponse.setSurname(coach.getSurname());
            coachResponse.setSpecialization(coach.getSpecialization());
            coachResponse.setActive(coach.getAppUser().isActive());

            OldCoachingContractResponse response = new OldCoachingContractResponse();
            response.setId(contract.getId());
            response.setCoachResponse(coachResponse);
            response.setStartDate(contract.getStartDate());
            response.setEndDate(contract.getEndDate());

            return response;
            }
        );
    }

    @Transactional
    public Page<PastMenteeResponse> getPastCoachMentees(Long coachId, Pageable pageable){
        if(!coachRepository.existsById(coachId))
            throw new EntityNotFoundException("Coach not found");

        Page<CoachingContract> pastContracts = coachingContractRepository.findOldContractsByCoachId(coachId, pageable);

        return pastContracts.map(contract -> {
            Member member = contract.getMember();

            boolean memberActive = member.getAppUser() != null && member.getAppUser().isActive();

            MemberResponseWithoutCoach memberResponse = new MemberResponseWithoutCoach();
            memberResponse.setId(member.getId());
            memberResponse.setName(member.getName());
            memberResponse.setSurname(member.getSurname());
            memberResponse.setSection(member.getSection());
            memberResponse.setActive(memberActive);

            PastMenteeResponse response = new PastMenteeResponse();
            response.setId(contract.getId());
            response.setMemberResponse(memberResponse);
            response.setStartDate(contract.getStartDate());
            response.setEndDate(contract.getEndDate());

            return response;
            }
        );
    }

    @Transactional
    public CoachingContract createContract(Long coachId, Long memberId){
        Coach coach = coachRepository.findById(coachId).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new EntityNotFoundException("Member not found")
        );
        if(member.getContracts().stream().anyMatch(c -> c.isActive() && c.getCoach().getId().equals(coachId)))
            throw new IllegalStateException("Member is already training with this coach");

        CoachingContract coachingContract = new CoachingContract();
        coachingContract.setMember(member);
        coachingContract.setCoach(coach);
        coachingContract.setStartDate(LocalDate.now());
        coachingContract.setActive(true);

        coachingContractRepository.save(coachingContract);
        return coachingContract;
    }
}
