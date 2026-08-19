package pl.omnisport.api.coach;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoachResponse {
    private Long id;

    private String name;

    private String surname;

    private String specialization;
}
