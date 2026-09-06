package pl.omnisport.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.omnisport.api.admin.AdminService;
import pl.omnisport.api.auth.CoachRegisterRequest;
import pl.omnisport.api.coach.CoachService;

@RestController
@RequestMapping("/api/staff")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class StaffControler {
    private final CoachService coachService;

    @PostMapping("/coaches")
    public ResponseEntity<?> createCoach(@RequestBody CoachRegisterRequest request){
        coachService.saveNewCoach(request);
        return ResponseEntity.ok("Coach added");
    }
}
