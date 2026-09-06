package pl.omnisport.api.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CoachRegisterRequest {
    private String name;
    private String surname;
    private String email;
    private String password;
    private String specialization;
}
