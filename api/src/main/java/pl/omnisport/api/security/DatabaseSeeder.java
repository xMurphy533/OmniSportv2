package pl.omnisport.api.security;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import pl.omnisport.api.admin.Admin;
import pl.omnisport.api.admin.AdminRepository;
import pl.omnisport.api.coach.Coach;
import pl.omnisport.api.coach.CoachRepository;
import pl.omnisport.api.contracts.CoachingContract;
import pl.omnisport.api.member.Member;
import pl.omnisport.api.member.MemberRepository;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DatabaseSeeder implements CommandLineRunner {
    private final AppUserRepository appUserRepository;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final CoachRepository coachRepository;
    private final MemberRepository memberRepository;

    @Override
    public void run(String... args) throws Exception{
        if(appUserRepository.findByEmail("szef@omnisport.pl").isEmpty()) {
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

            adminRepository.save(adminProfile);

            AppUser coachAccount = new AppUser();
            coachAccount.setEmail("kaiser@omnisport.pl");
            coachAccount.setPassword(passwordEncoder.encode("barcza12"));
            coachAccount.setRole(Role.COACH);
            coachAccount.setActive(true);

            AppUser memberAccount = new AppUser();
            memberAccount.setEmail("kaczmarczyk21@omnisport.pl");
            memberAccount.setPassword(passwordEncoder.encode("niko05"));
            memberAccount.setRole(Role.MEMBER);
            memberAccount.setActive(true);

            Coach coachProfile = new Coach();
            coachProfile.setName("Przemysław");
            coachProfile.setSurname("Zbiciak");
            coachProfile.setAge(37);
            coachProfile.setSpecialization("MMA, BJJ, Kickboxing");
            coachProfile.setAppUser(coachAccount);

            Member memberProfile = new Member();
            memberProfile.setName("Nikodem");
            memberProfile.setSurname("Kaczmarczyk");
            memberProfile.setAge(21);
            memberProfile.setSection("Kickboxing");
            memberProfile.setPassValid(true);
            memberProfile.setExpiryDate(LocalDate.now().plusMonths(1));
            memberProfile.setAppUser(memberAccount);

            CoachingContract contract = new CoachingContract();
            contract.setMember(memberProfile);
            contract.setCoach(coachProfile);
            contract.setStartDate(LocalDate.now());
            contract.setActive(true);

            memberProfile.getContracts().add(contract);

            coachRepository.save(coachProfile);
            memberRepository.save(memberProfile);
        }
    }
}
