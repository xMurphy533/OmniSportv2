package pl.omnisport.api.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.omnisport.api.admin.Admin.AdminRole;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminRegisterRequest {
    private String name;
    private String surname;
    private String email;
    private String password;
    private AdminRole adminRole;
}
