package pl.omnisport.api.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminResponse {
    private Long id;

    private String name;

    private String surname;

    private Admin.AdminRole role;
}
