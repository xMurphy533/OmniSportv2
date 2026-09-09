package pl.omnisport.api.contracts;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseWithoutCoach {
    private Long id;
    private String name;
    private String surname;
    private String section;
    private boolean isActive;
}

