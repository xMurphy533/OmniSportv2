package pl.omnisport.api.coach;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import pl.omnisport.api.member.Member;

import java.util.List;

@Entity
@Table(name = "coaches")
@Getter
@Setter
@NoArgsConstructor
@ToString

public class Coach {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "name cannot be blank")
    private String name;

    @NotBlank(message = "surname cannot be blank")
    private String surname;

    @NotNull(message = "age cannot be null")
    @Min(value = 18, message = "age must be 18 or higher")
    private Integer age;

    @NotBlank(message = "specialization cannot be blank")
    private String specialization;

    @OneToMany(mappedBy = "coach")
    @ToString.Exclude //to po to żeby uniknąć nieskończonej pętli
    private List<Member> mentees;
}
