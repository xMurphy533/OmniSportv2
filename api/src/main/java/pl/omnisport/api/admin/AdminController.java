package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Page<AdminResponse>> getAllAdmins(Pageable pageable){
        Page<Admin> adminPage = adminService.getAllAdmins(pageable);
        Page<AdminResponse> responsePage = adminPage.map(adminMapper::toResponse);
        return ResponseEntity.ok(responsePage);
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
    @PatchMapping("/{id}/login")
    public void recordAdminLogin(@PathVariable Long id) throws EntityNotFoundException{
        adminService.recordLogin(id);
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> changeAdminRole(@PathVariable Long id, @RequestBody AdminRole request) throws EntityNotFoundException{
        adminService.changeAdminRole(id, Admin.AdminRole.valueOf(request.newRole()));
        return ResponseEntity.ok().build();
    }

    //DELETE
    @DeleteMapping("/{targetId}")
    public void deactivateAdmin(@PathVariable Long targetId, Long currentAdminId) throws EntityNotFoundException {
        adminService.deactivateAdmin(targetId, currentAdminId);
    }
}
