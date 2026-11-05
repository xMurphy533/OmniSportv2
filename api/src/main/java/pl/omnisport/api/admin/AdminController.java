package pl.omnisport.api.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
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
@Tag(name="Admins", description = "Menaging admins account")
public class AdminController {
    private final AdminService adminService;
    private final AdminMapper adminMapper;

    //CREATE
    @Operation(summary = "Add new admin")
    @PostMapping("/add-new-admin")
    public ResponseEntity<Void> addNewAdmin(
            @Valid
            @RequestBody AdminRegisterRequest request){
        adminService.registerNewAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    //READ
    @Operation(summary = "Get all admins")
    @GetMapping("/get-all-admins")
    public ResponseEntity<Page<AdminResponse>> getAllAdmins(
            @ParameterObject Pageable pageable){
        return ResponseEntity.ok(adminService.getAllAdmins(pageable));
    }

    @Operation(summary = "Get admin by ID")
    @GetMapping("/{id}/get-admin-by-id")
    public ResponseEntity<AdminGetByIdResponse> getAdminById(
            @PathVariable Long id) throws EntityNotFoundException{
        return ResponseEntity.ok(adminService.findAdminById(id));
    }

    @Operation(summary = "Get admin by e-mail address")
    @GetMapping("/get-admin-by-email")
    public ResponseEntity<AdminResponse> getAdminByEmail(
            @RequestParam String email) throws EntityNotFoundException{
        return ResponseEntity.ok(adminService.findAdminByEmail(email).map(adminMapper::toResponse).orElseThrow());
    }

    //UPDATE
    @Operation(summary = "Record admin's login")
    @PatchMapping("/{id}/record-admin-login")
    public ResponseEntity<Void> recordAdminLogin(
            @PathVariable Long id) throws EntityNotFoundException{
        adminService.recordLogin(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change admin role")
    @PatchMapping("/{id}/change-admin-role")
    public ResponseEntity<Void> changeAdminRole(
            @PathVariable Long id,
            @RequestBody AdminRole request) throws EntityNotFoundException{
        adminService.changeAdminRole(id, Admin.AdminRole.valueOf(request.newRole()));
        return ResponseEntity.ok().build();
    }

    //DELETE
    @Operation(summary = "Deactivate admin")
    @PatchMapping("/{targetId}/deactivate-admin")
    public ResponseEntity<Void> deactivateAdmin(
            @PathVariable Long targetId,
            @AuthenticationPrincipal AppUser currentUser) throws EntityNotFoundException {
        Long currentUserId = currentUser.getId();
        adminService.deactivateAdmin(targetId, currentUserId);
        return ResponseEntity.ok().build();
    }
}
