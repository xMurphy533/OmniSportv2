package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private final AdminService adminService;

    //CREATE
    @PostMapping
    public void addNewAdmin(Admin admin){
        adminService.registerNewAdmin(admin);
    }

    //READ
    @GetMapping
    public List<Admin> getAllAdmins(){
        return adminService.getAllAdmins();
    }

    @GetMapping("/{id}")
    public Optional<Admin> getAdminById(@PathVariable Long id) throws EntityNotFoundException{
        return adminService.findAdminById(id);
    }

    @GetMapping("/search")
    public Optional<Admin> getAdminByEmail(@RequestParam String email) throws EntityNotFoundException{
        return adminService.findAdminByEmail(email);
    }

    //UPDATE
    @PatchMapping("/{adminId}/login")
    public void recordAdminLogin(@RequestParam Long adminId) throws EntityNotFoundException{
        adminService.recordLogin(adminId);
    }

    @PatchMapping("/{id}/role")
    public void changeAdminRole(@PathVariable Long id, @RequestParam Admin.AdminRole newRole) throws EntityNotFoundException{
        adminService.changeAdminRole(id, newRole);
    }

    //DELETE
    @DeleteMapping("/{targetId}")
    public void deactivateAdmin(Long targetId, Long currentAdminId) throws EntityNotFoundException {
        adminService.deactivateAdmin(targetId, currentAdminId);
    }
}
