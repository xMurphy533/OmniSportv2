package pl.omnisport.api.contracts;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name="Coaching contract menagement")
public class CoachingContractController {
    private final CoachingContractService coachingContractService;

    @Operation(summary = "Get member past coaches")
    @GetMapping("/member/{memberId}/pastCoaches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OldCoachingContractResponse>> getMemberPastCoaches(
            @PathVariable Long memberId,
            @ParameterObject Pageable pageable){
        return ResponseEntity.ok(coachingContractService.getMemberCoachingHistory(memberId, pageable));
    }

    @Operation(summary = "Get coach past mentees")
    @GetMapping("/coach/{coachId}/pastMentees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PastMenteeResponse>> getCoachPastMentees(
            @PathVariable Long coachId,
            @ParameterObject Pageable pageable){
        return ResponseEntity.ok(coachingContractService.getPastCoachMentees(coachId, pageable));
    }
}
