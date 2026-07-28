package member;

import jakarta.persistence.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@Table(name = "members")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    private String surname;

    @NotNull(message = "Age cannot be null")
    @Min(value = 16, message = "Age must be 16 or higher")
    private Integer age;

    @NotBlank(message = "Section cannot be blank")
    private String section;

    @Column(name = "is_pass_valid")
    private boolean isPassValid;

    @NotNull(message = "Expiry date cannot be null")
    private LocalDate expiryDate;
}
