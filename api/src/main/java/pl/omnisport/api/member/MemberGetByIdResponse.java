package pl.omnisport.api.member;

import jakarta.persistence.Column;
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
public class MemberGetByIdResponse {
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private String section;
    private CoachResponse coach;
    private boolean isPassValid;
    private LocalDate expiryDate;
    private boolean isActive;
}
