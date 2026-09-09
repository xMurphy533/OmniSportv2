package pl.omnisport.api.contracts;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/contracts")
@RequiredArgsConstructor
public class CoachingContractController {
    private final CoachingContractService coachingContractService;

    @GetMapping("/member/{memberId}/pastCoaches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OldCoachingContractResponse>> getMemberPastCoaches(@PathVariable Long memberId, Pageable pageable){
        return ResponseEntity.ok(coachingContractService.getMemberCoachingHistory(memberId, pageable));
    }

    @GetMapping("/coach/{coachId}/pastMentees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PastMenteeResponse>> getCoachPastMentees(@PathVariable Long coachId, Pageable pageable){
        return ResponseEntity.ok(coachingContractService.getPastCoachMentees(coachId, pageable));
    }
}
