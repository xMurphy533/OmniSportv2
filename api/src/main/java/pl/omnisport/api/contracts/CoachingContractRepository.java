package pl.omnisport.api.contracts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.omnisport.api.member.Member;

public interface CoachingContractRepository extends JpaRepository<CoachingContract, Long> {
    @Query("SELECT c.member FROM CoachingContract c JOIN FETCH c.member.appUser WHERE c.coach.id = :coachId AND c.isActive = true")
    Page<Member> findActiveMembersByCoachId(@Param("coachId") Long coachId, Pageable pageable);

    @Query("SELECT c FROM CoachingContract c WHERE c.member.id = :memberId AND c.isActive = false")
    Page<CoachingContract> findOldContractsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT c FROM CoachingContract c WHERE c.coach.id = :coachId AND c.isActive = false")
    Page<CoachingContract> findOldContractsByCoachId(@Param("coachId") Long coachId, Pageable pageable);

    @Query("SELECT c FROM CoachingContract c WHERE c.member.id = :memberId AND c.isActive = true")
    Page<CoachingContract> findAllByMemberIdAndIsActiveTrue(@Param("memberId") Long memberId, Pageable pageable);
}
