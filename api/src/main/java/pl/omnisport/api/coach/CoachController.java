package pl.omnisport.api.coach;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import pl.omnisport.api.auth.CoachRegisterRequest;

import java.util.Optional;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {
    private final CoachService coachService;
    private final CoachMapper coachMapper;

    //CREATE
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> addNewCoach(@Valid @RequestBody CoachRegisterRequest request){
        coachService.saveNewCoach(request);
        return ResponseEntity.ok("Coach added successfully");
    }

    //READ
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<CoachResponse>> getAllCoaches(Pageable pageable){
        return ResponseEntity.ok(coachService.getAllCoaches(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CoachResponse getCoachById(@PathVariable Long id) throws EntityNotFoundException{
        return coachService.getCoachById(id);
    }
    @GetMapping("/{coachId}/mentees")
    @PreAuthorize("hasAnyRole('ADMIN', 'COACH')")
    public ResponseEntity<Page<MenteeResponse>> getCoachMentees(@PathVariable Long coachId, Pageable pageable) throws EntityNotFoundException{
        Page<MenteeResponse> menteesPage = coachService.getAllMentees(coachId, pageable);
        return ResponseEntity.ok(menteesPage);
    }

    //UPDATE
    @PatchMapping("/{id}/specialization")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateSpecialization(@PathVariable Long id, @RequestBody UpdateSpecializationRequest request){
        coachService.updateCoachSpecialization(id, request.newSpecialization());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{coachId}/{memberId}/mentees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> addMentee(@PathVariable Long coachId, @PathVariable Long memberId){
        coachService.addMenteeToCoach(coachId, memberId);
        return ResponseEntity.ok().build();
    }

    //DELETE
    @DeleteMapping("/{coachId}/{memberId}/mentees")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeMentee(@PathVariable Long coachId, @PathVariable Long memberId){
        coachService.removeMenteeFromCoach(coachId, memberId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCoach(@PathVariable Long id){
        coachService.removeCoach(id);
    }

}
