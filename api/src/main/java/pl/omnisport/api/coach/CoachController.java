package pl.omnisport.api.coach;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import pl.omnisport.api.admin.CoachMapper;

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
    public List<CoachResponse> getAllCoaches(){
        List<Coach> coaches = coachService.getAllCoaches();
        return coachMapper.toResponseList(coaches);
    }

    @GetMapping("/{id}")
    public Optional<CoachResponse> getCoachById(@PathVariable Long id) throws EntityNotFoundException{
        return coachService.getCoachById(id).map(coachMapper::toResponse);
    }

    //UPDATE
    @PatchMapping("/{id}/specialization")
    public void updateCoachSpecialization(@PathVariable Long id, @RequestParam String newSpecialization){
        coachService.updateCoachSpecialization(id, newSpecialization);
    }

    //DELETE
    @DeleteMapping("/{id}")
    public void deleteCoach(@PathVariable Long id){
        coachService.removeCoach(id);
    }

}
