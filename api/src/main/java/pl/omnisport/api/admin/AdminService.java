package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.omnisport.api.auth.AdminRegisterRequest;
import pl.omnisport.api.exception.SelfDeletionNotAllowedException;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerNewAdmin(AdminRegisterRequest request) {
        if (appUserRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("An account with the address " + request.getEmail() + " already exists in the system");
        }
        AppUser appUser = new AppUser();
        appUser.setEmail(request.getEmail());
        appUser.setPassword(passwordEncoder.encode(request.getPassword()));
        appUser.setRole(Role.ADMIN);
        appUser.setActive(true);

        appUserRepository.save(appUser);

        Admin admin = new Admin();
        admin.setName(request.getName());
        admin.setSurname(request.getSurname());
        admin.setAdminRole(request.getAdminRole());
        admin.setCreatedAt(LocalDate.now());
        admin.setLastLoginAt(null);
        admin.setAppUser(appUser);

        adminRepository.save(admin);
    }

    public Page<AdminResponse> getAllAdmins(Pageable pageable){
        Page<Admin> adminPage = adminRepository.findAll(pageable);
        return adminPage.map(
                admin -> {
                    boolean active = false;
                    if(admin.getAppUser() != null){
                        active = admin.getAppUser().isActive();
                    }
                    return new AdminResponse(
                            admin.getId(),
                            admin.getName(),
                            admin.getSurname(),
                            admin.getAdminRole(),
                            active
                    );
                }
        );
    }

    public AdminResponse findAdminById(Long id){
        Admin admin = adminRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        return new AdminResponse(
                admin.getId(),
                admin.getName(),
                admin.getSurname(),
                admin.getAdminRole(),
                admin.getAppUser().isActive()
        );
    }

    public Optional<Admin> findAdminByEmail(String email){
        Admin admin = adminRepository.findByAppUserEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        return Optional.of(admin);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deactivateAdmin(Long targetId, Long currentAdminId){
        if(targetId.equals(currentAdminId))
            throw new SelfDeletionNotAllowedException("You cannot deactivate your account");

        Admin admin = adminRepository.findById(targetId).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        admin.getAppUser().setActive(false);
        adminRepository.save(admin);
    }

    @org.springframework.transaction.annotation.Transactional
    public void recordLogin(Long id){
        Admin admin = adminRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        admin.setLastLoginAt(LocalDate.now());

    }

    @org.springframework.transaction.annotation.Transactional
    public void changeAdminRole(Long id, Admin.AdminRole newRole){
        Admin admin = adminRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        admin.setAdminRole(newRole);
    }
}
