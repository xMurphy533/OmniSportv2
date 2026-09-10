package pl.omnisport.api.coach;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CoachGetByIdResponse {
    private Long id;
    private String name;
    private String surname;
    private Integer age;
    private String specialization;
    private boolean isActive;
}
