package pl.omnisport.api.coach;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CoachService {
    private final CoachRepository coachRepository;

    public void saveNewCoach(Coach coach){
        coachRepository.save(coach);
    }

    public List<Coach> getAllCoaches(){
        return coachRepository.findAll();
    }

    public Optional<Coach> getCoachById(Long id){
        return coachRepository.findById(id);
    }

    public void updateCoachSpecialization(Long id, String newSpec){
        Coach coach = getCoachById(id).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        coach.setSpecialization(newSpec);
        coachRepository.save(coach);
    }

    public void removeCoach(Long id){
        Coach coach = getCoachById(id).orElseThrow(
                () -> new EntityNotFoundException("Coach not found")
        );
        coachRepository.delete(coach);
    }
}
