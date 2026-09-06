package pl.omnisport.api.coach;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.omnisport.api.admin.CoachMapper;
import pl.omnisport.api.member.Member;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {
    private final CoachService coachService;
    private final CoachMapper coachMapper;

    //CREATE
    @PostMapping
    public void addNewCoach(@Valid @RequestBody CoachRequest request){
        Coach coach = coachMapper.toEntity(request);
        coachService.saveNewCoach(coach);
    }

    //READ
    @GetMapping
    public ResponseEntity<Page<CoachResponse>> getAllCoaches(Pageable pageable){
        Page<Coach> coachPage = coachService.getAllCoaches(pageable);
        Page<CoachResponse> responsePage = coachPage.map(coachMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/{id}")
    public Optional<CoachResponse> getCoachById(@PathVariable Long id) throws EntityNotFoundException{
        return coachService.getCoachById(id).map(coachMapper::toResponse);
    }

    //UPDATE
    @PatchMapping("/{id}/specialization")
    public ResponseEntity<Void> updateSpecialization(@PathVariable Long id, @RequestBody UpdateSpecializationRequest request){
        coachService.updateCoachSpecialization(id, request.newSpecialization());
        return ResponseEntity.ok().build();
    }

    //DELETE
    @DeleteMapping("/{id}")
    public void deleteCoach(@PathVariable Long id){
        coachService.removeCoach(id);
    }

}
