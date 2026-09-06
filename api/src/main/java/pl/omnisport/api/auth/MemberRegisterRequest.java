package pl.omnisport.api.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberRegisterRequest {
    private String name;
    private String surname;
    private Integer age;
    private String email;
    private String password;
    private String section;
    private Long coachId;
    private boolean isPassValid;
}
