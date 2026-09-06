package pl.omnisport.api.member;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString

public class MemberRequest {
    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    private String surname;

    @NotNull(message = "Age cannot be null")
    private Integer age;

    @NotBlank(message = "E-mail cannot be blank")
    @Email(message = "Please put correct e-mail pattern")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    private String password;

    @NotBlank(message = "Section cannot be blank")
    private String section;

    @NotNull(message = "Member must have coach")
    private Long coachId;
}
