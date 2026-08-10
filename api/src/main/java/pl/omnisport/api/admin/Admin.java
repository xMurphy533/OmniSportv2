package pl.omnisport.api.admin;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "admins")
@ToString
public class Admin {

    public enum AdminRole {
    SUPER_ADMIN,
    MODERATOR
}
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @NotBlank(message = "Name cannot be blank")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    @Column(nullable = false)
    private String surname;

    @Column(unique = true, nullable = false)
    @NotBlank(message = "E-mail cannot be blank")
    @Email(message = "Please put correct e-mail pattern")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Column(nullable = false)
    private String password;

    @NotNull(message = "Role cannot be null")
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AdminRole role;

    @Column(nullable = false)
    private boolean isActive;

    @NotNull
    @Column(nullable = false)
    private LocalDate createdAt;

    @NotNull
    @Column(nullable = false)
    private LocalDate lastLoginAt;
}
