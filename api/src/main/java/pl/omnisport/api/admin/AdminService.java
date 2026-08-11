package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;

    public void registerNewAdmin(Admin admin) {
        admin.setCreatedAt(LocalDate.now());
        admin.setActive(true);
        adminRepository.save(admin);
    }

    public List<Admin> getAllAdmins(){
        return adminRepository.findAll();
    }

    public Optional<Admin> findAdminById(Long id){
        Admin admin = adminRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        return Optional.of(admin);
    }

    public Optional<Admin> findAdminByEmail(String email){
        Admin admin = adminRepository.findByEmail(email).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        return Optional.of(admin);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deactivateAdmin(Long targetId, Long currentAdminId){
        if(targetId.equals(currentAdminId))
            throw new IllegalStateException("You cannot deactivate your account");

        Admin admin = adminRepository.findById(targetId).orElseThrow(
                () -> new EntityNotFoundException("Admin not found")
        );
        admin.setActive(false);
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
        admin.setRole(newRole);
    }
}
