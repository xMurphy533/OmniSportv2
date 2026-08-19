package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admins")
public class AdminController {
    private final AdminService adminService;
    private final AdminMapper adminMapper;

    //CREATE
    @PostMapping
    public void addNewAdmin(@Valid @RequestBody AdminRequest request){
        Admin admin = adminMapper.toEntity(request);
        adminService.registerNewAdmin(admin);
    }

    //READ
    @GetMapping
    public List<AdminResponse> getAllAdmins(){
        List<Admin> admins = adminService.getAllAdmins();
        return adminMapper.toResponseList(admins);
    }

    @GetMapping("/{id}")
    public Optional<AdminResponse> getAdminById(@PathVariable Long id) throws EntityNotFoundException{
        return adminService.findAdminById(id).map(adminMapper::toResponse);
    }

    @GetMapping("/search")
    public Optional<AdminResponse> getAdminByEmail(@RequestParam String email) throws EntityNotFoundException{
        return adminService.findAdminByEmail(email).map(adminMapper::toResponse);
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
