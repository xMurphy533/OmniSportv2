package pl.omnisport.api.contracts;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.omnisport.api.member.MemberResponse;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PastMenteeResponse {
    private Long id;
    private MemberResponseWithoutCoach memberResponse;
    private LocalDate startDate;
    private LocalDate endDate;
}
