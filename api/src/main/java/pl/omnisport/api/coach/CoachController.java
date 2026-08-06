package pl.omnisport.api.coach;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/coaches")
@RequiredArgsConstructor
public class CoachController {
    private final CoachService coachService;

    //CREATE
    @PostMapping
    public void addNewCoach(@Valid @RequestBody Coach coach){
        coachService.saveNewCoach(coach);
    }

    //READ
    @GetMapping
    public List<Coach> getAllCoaches(){
        return coachService.getAllCoaches();
    }

    @GetMapping("/{id}")
    public Coach getCoachById(@PathVariable Long id){
        return coachService.getCoachById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Coach not found")
        );
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
