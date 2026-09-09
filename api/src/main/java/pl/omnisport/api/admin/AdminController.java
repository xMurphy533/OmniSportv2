package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import pl.omnisport.api.auth.AdminRegisterRequest;
import pl.omnisport.api.user.AppUser;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admins")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    private final AdminMapper adminMapper;

    //CREATE
    @PostMapping("/addNewAdmin")
    public ResponseEntity<Void> addNewAdmin(@Valid @RequestBody AdminRegisterRequest request){
        adminService.registerNewAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //READ
    @GetMapping("/getAllAdmins")
    public ResponseEntity<Page<AdminResponse>> getAllAdmins(Pageable pageable){
        return ResponseEntity.ok(adminService.getAllAdmins(pageable));
    }

    @GetMapping("/getAdminById/{id}")
    public ResponseEntity<AdminResponse> getAdminById(@PathVariable Long id) throws EntityNotFoundException{
        return ResponseEntity.ok(adminService.findAdminById(id));
    }

    @GetMapping("/getAdminByEmail")
    public ResponseEntity<AdminResponse> getAdminByEmail(@RequestParam String email) throws EntityNotFoundException{
        return ResponseEntity.ok(adminService.findAdminByEmail(email).map(adminMapper::toResponse).orElseThrow());
    }

    //UPDATE
    @PatchMapping("/recordAdminLogin/{id}")
    public ResponseEntity<Void> recordAdminLogin(@PathVariable Long id) throws EntityNotFoundException{
        adminService.recordLogin(id);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/changeAdminRole/{id}/role")
    public ResponseEntity<Void> changeAdminRole(@PathVariable Long id, @RequestBody AdminRole request) throws EntityNotFoundException{
        adminService.changeAdminRole(id, Admin.AdminRole.valueOf(request.newRole()));
        return ResponseEntity.ok().build();
    }

    //DELETE
    @DeleteMapping("/deactivate/{targetId}")
    public ResponseEntity<Void> deactivateAdmin(@PathVariable Long targetId, @AuthenticationPrincipal AppUser currentUser) throws EntityNotFoundException {
        Long currentUserId = currentUser.getId();
        adminService.deactivateAdmin(targetId, currentUserId);
        return ResponseEntity.ok().build();
    }
}
