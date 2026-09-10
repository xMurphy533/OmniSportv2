package pl.omnisport.api.admin;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminGetByIdResponse {
    private Long id;

    private String name;

    private String surname;

    private Admin.AdminRole adminRole;

    private LocalDate createdAt;

    private LocalDate lastLoginAt;

    private boolean isActive;
}