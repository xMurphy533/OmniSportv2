package pl.omnisport.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.omnisport.api.admin.Admin;
import pl.omnisport.api.admin.AdminRepository;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {
    private final AppUserRepository appUserRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception{
        if(appUserRepository.findByEmail("szef@omnisport.pl").isEmpty()){
            AppUser adminAccount = new AppUser();
            adminAccount.setEmail("szef@omnisport.pl");
            adminAccount.setPassword(passwordEncoder.encode("password123"));
            adminAccount.setRole(Role.ADMIN);
            adminAccount.setActive(true);

            Admin adminProfile = new Admin();
            adminProfile.setName("Main");
            adminProfile.setSurname("Admin");
            adminProfile.setAdminRole(Admin.AdminRole.SUPER_ADMIN);
            adminProfile.setAppUser(adminAccount);
            adminProfile.setActive(true);

            adminRepository.save(adminProfile);
        }
    }
}
