package pl.omnisport.api.coach;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.omnisport.api.auth.CoachRegisterRequest;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
@Tag(name="Coaches", description = "Managing coach accounts")
public class CoachController {
    private final CoachService coachService;

    //CREATE
    @Operation(summary = "Add new coach")
    @PostMapping("/add-new-coach")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addNewCoach(
            @Valid @RequestBody CoachRegisterRequest request){
        coachService.saveNewCoach(request);
        return ResponseEntity.ok().build();
    }

    //READ
    @Operation(summary = "Get all coaches")
    @GetMapping("/get-all-coaches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CoachResponse>> getAllCoaches(
            @ParameterObject Pageable pageable){
        return ResponseEntity.ok(coachService.getAllCoaches(pageable));
    }

    @Operation(summary = "Get coach by ID")
    @GetMapping("/{id}/get-coach-by-id")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CoachGetByIdResponse> getCoachById(
            @PathVariable Long id) throws EntityNotFoundException{
        return ResponseEntity.ok(coachService.getCoachById(id));
    }

    @Operation(summary = "Get coach mentees")
    @GetMapping("/{coachId}/get-coach-mentees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MenteeResponse>> getCoachMentees(
            @PathVariable Long coachId,
            @ParameterObject Pageable pageable) throws EntityNotFoundException{
        Page<MenteeResponse> menteesPage = coachService.getAllMentees(coachId, pageable);
        return ResponseEntity.ok(menteesPage);
    }

    //UPDATE
    @Operation(summary = "Update coach specialization")
    @PatchMapping("/{id}/update-specialization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateSpecialization(
            @PathVariable Long id,
            @RequestBody UpdateSpecializationRequest request){
        coachService.updateCoachSpecialization(id, request.newSpecialization());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Add mentee to coach")
    @PatchMapping("/{coachId}/{memberId}/add-mentee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addMentee(
            @PathVariable Long coachId,
            @PathVariable Long memberId){
        coachService.addMenteeToCoach(coachId, memberId);
        return ResponseEntity.ok().build();
    }

    //DELETE
    @Operation(summary = "Remove mentee from coach")
    @DeleteMapping("/{coachId}/{memberId}/remove-mentee")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeMentee(
            @PathVariable Long coachId,
            @PathVariable Long memberId){
        coachService.removeMenteeFromCoach(coachId, memberId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Deactivate coach")
    @PatchMapping("/{id}/deactivate-coach")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deactivateCoach(
            @PathVariable Long id){
        coachService.deactivateCoach(id);
        return ResponseEntity.ok().build();
    }

}
