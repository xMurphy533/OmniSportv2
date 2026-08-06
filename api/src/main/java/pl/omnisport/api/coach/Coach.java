package pl.omnisport.api.coach;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    private String surname;

    @NotNull(message = "Age cannot be null")
    @Min(value = 18, message = "Age must be 18 or higher")
    private Integer age;

    @NotBlank(message = "Specialization cannot be blank")
    private String specialization;

    @OneToMany(mappedBy = "coach")
    @JsonIgnore //to po to żeby uniknąć błędu typu 500
    @ToString.Exclude //to po to żeby uniknąć nieskończonej pętli w konsoli
    private List<Member> mentees;
}
