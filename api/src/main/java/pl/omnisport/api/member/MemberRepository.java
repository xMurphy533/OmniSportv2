package pl.omnisport.api.member;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Query("SELECT c.member FROM CoachingContract c WHERE c.coach.id = :coachId")
    Page<Member> findAllByCoachId(@Param("coachId") Long coachId, Pageable pageable);
}
