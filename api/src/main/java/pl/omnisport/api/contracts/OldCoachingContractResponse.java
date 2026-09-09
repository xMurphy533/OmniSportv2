package pl.omnisport.api.contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.omnisport.api.coach.CoachResponse;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OldCoachingContractResponse {
    private Long id;
    private CoachResponse coachResponse;
    private LocalDate startDate;
    private LocalDate endDate;
}
