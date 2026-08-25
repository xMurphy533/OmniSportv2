package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.omnisport.api.exception.SelfDeletionNotAllowedException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @InjectMocks
    private AdminService adminService;

    @Test
    void shouldRegisterNewAdmin() {
        //GIVEN
        Admin admin = new Admin();
        admin.setName("Test");
        admin.setSurname("Admin");
        admin.setEmail("test@admin.pl");
        admin.setPassword("password123");
        admin.setRole(Admin.AdminRole.SUPER_ADMIN);

        //WHEN
        adminService.registerNewAdmin(admin);

        //THEN
        assertTrue(admin.isActive(), "Admin should be active after the registration");
        assertEquals(LocalDate.now(), admin.getCreatedAt(), "Registration date should be the same as today");

        verify(adminRepository).save(admin);
    }

    @Test
    void shouldGetAllAdmins() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Admin> adminsList = createAdminsList(3);
        Page<Admin> expectedPage = new PageImpl<>(adminsList, pageable, 3);

        when(adminRepository.findAll(pageable)).thenReturn(expectedPage);

        // WHEN
        Page<Admin> result = adminService.getAllAdmins(pageable);

        // THEN
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.getContent().size(), "Page should contain 3 admins");
        assertEquals(3, result.getTotalElements(), "Total elements should be 3");
        assertEquals(1, result.getTotalPages(), "Total pages should be 1");
        assertTrue(result.isFirst(), "Should be first page");
        assertTrue(result.isLast(), "Should be last page");
        verify(adminRepository).findAll(pageable);
    }

    @Test
    void shouldFindAdminByIdSuccessfully() {
        // GIVEN
        Long adminId = 1L;
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("John");
        admin.setEmail("john@admin.pl");
        admin.setActive(true);

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // WHEN
        Optional<Admin> result = adminService.findAdminById(adminId);

        // THEN
        assertTrue(result.isPresent(), "Optional should contain an admin");
        assertEquals(adminId, result.get().getId(), "Admin ID should match");
        assertEquals("John", result.get().getName(), "Admin name should match");
        assertEquals("john@admin.pl", result.get().getEmail(), "Admin email should match");
        verify(adminRepository).findById(adminId);
    }

    @Test
    void shouldThrowExceptionWhenAdminByIdNotFound() {
        // GIVEN
        Long nonExistentAdminId = 999L;

        when(adminRepository.findById(nonExistentAdminId)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> adminService.findAdminById(nonExistentAdminId),
                "Should throw EntityNotFoundException when admin is not found"
        );

        assertEquals("Admin not found", exception.getMessage(), "Exception message should be 'Admin not found'");
        verify(adminRepository).findById(nonExistentAdminId);
    }

    @Test
    void shouldFindAdminByEmailSuccessfully() {
        // GIVEN
        String email = "john@admin.pl";
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setName("John");
        admin.setEmail(email);
        admin.setActive(true);

        when(adminRepository.findByEmail(email)).thenReturn(Optional.of(admin));

        // WHEN
        Optional<Admin> result = adminService.findAdminByEmail(email);

        // THEN
        assertTrue(result.isPresent(), "Optional should contain an admin");
        assertEquals(email, result.get().getEmail(), "Admin email should match");
        assertEquals("John", result.get().getName(), "Admin name should match");
        verify(adminRepository).findByEmail(email);
    }

    @Test
    void shouldThrowExceptionWhenAdminByEmailNotFound() {
        // GIVEN
        String nonExistentEmail = "nonexistent@admin.pl";

        when(adminRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> adminService.findAdminByEmail(nonExistentEmail),
                "Should throw EntityNotFoundException when admin email is not found"
        );

        assertEquals("Admin not found", exception.getMessage(), "Exception message should be 'Admin not found'");
        verify(adminRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void shouldDeactivateAdminSuccessfully() {
        // GIVEN
        Long targetAdminId = 2L;
        Long currentAdminId = 1L;
        Admin adminToDeactivate = new Admin();
        adminToDeactivate.setId(targetAdminId);
        adminToDeactivate.setName("Target Admin");
        adminToDeactivate.setActive(true);

        when(adminRepository.findById(targetAdminId)).thenReturn(Optional.of(adminToDeactivate));

        // WHEN
        adminService.deactivateAdmin(targetAdminId, currentAdminId);

        // THEN
        assertFalse(adminToDeactivate.isActive(), "Admin should be deactivated");
        verify(adminRepository).findById(targetAdminId);
    }

    @Test
    void shouldThrowExceptionWhenTryingToDeactivateSelf() {
        // GIVEN
        Long adminId = 1L;

        // WHEN & THEN
        SelfDeletionNotAllowedException exception = assertThrows(
                SelfDeletionNotAllowedException.class,
                () -> adminService.deactivateAdmin(adminId, adminId),
                "Should throw SelfDeletionNotAllowedException when trying to deactivate self"
        );

        assertEquals("You cannot deactivate your account", exception.getMessage(), 
                "Exception message should match");
    }

    @Test
    void shouldThrowExceptionWhenDeactivatingNonExistentAdmin() {
        // GIVEN
        Long targetAdminId = 999L;
        Long currentAdminId = 1L;

        when(adminRepository.findById(targetAdminId)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> adminService.deactivateAdmin(targetAdminId, currentAdminId),
                "Should throw EntityNotFoundException when target admin is not found"
        );

        assertEquals("Admin not found", exception.getMessage(), "Exception message should be 'Admin not found'");
        verify(adminRepository).findById(targetAdminId);
    }

    // ==================== RECORD LOGIN TESTS ====================

    @Test
    void shouldRecordLoginAndUpdateLastLoginDate() {
        // GIVEN
        Long adminId = 1L;
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("John");
        admin.setLastLoginAt(LocalDate.of(2025, 1, 1));

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // WHEN
        adminService.recordLogin(adminId);

        // THEN
        assertEquals(LocalDate.now(), admin.getLastLoginAt(), 
                "Last login date should be updated to today");
        verify(adminRepository).findById(adminId);
    }

    @Test
    void shouldThrowExceptionWhenRecordingLoginForNonExistentAdmin() {
        // GIVEN
        Long nonExistentAdminId = 999L;

        when(adminRepository.findById(nonExistentAdminId)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> adminService.recordLogin(nonExistentAdminId),
                "Should throw EntityNotFoundException when admin is not found"
        );

        assertEquals("Admin not found", exception.getMessage(), "Exception message should be 'Admin not found'");
        verify(adminRepository).findById(nonExistentAdminId);
    }

    @Test
    void shouldRecordLoginMultipleTimes() {
        // GIVEN
        Long adminId = 1L;
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("John");
        admin.setLastLoginAt(null);

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // WHEN - First login
        adminService.recordLogin(adminId);
        LocalDate firstLoginDate = admin.getLastLoginAt();

        // THEN - First login
        assertEquals(LocalDate.now(), firstLoginDate, "First login date should be today");

        // WHEN - Second login (simulating next day)
        adminService.recordLogin(adminId);

        // THEN - Second login
        assertEquals(LocalDate.now(), admin.getLastLoginAt(), "Login date should be updated to today");
        verify(adminRepository, times(2)).findById(adminId);
    }

    @Test
    void shouldChangeAdminRoleFromAdminToSuperAdmin() {
        // GIVEN
        Long adminId = 1L;
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("John");
        admin.setRole(Admin.AdminRole.SUPER_ADMIN);

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // WHEN
        adminService.changeAdminRole(adminId, Admin.AdminRole.SUPER_ADMIN);

        // THEN
        assertEquals(Admin.AdminRole.SUPER_ADMIN, admin.getRole(), 
                "Admin role should be changed to SUPER_ADMIN");
        verify(adminRepository).findById(adminId);
    }

    @Test
    void shouldChangeAdminRoleFromSuperAdminToModerator() {
        // GIVEN
        Long adminId = 1L;
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("John");
        admin.setRole(Admin.AdminRole.SUPER_ADMIN);

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // WHEN
        adminService.changeAdminRole(adminId, Admin.AdminRole.MODERATOR);

        // THEN
        assertEquals(Admin.AdminRole.MODERATOR, admin.getRole(),
                "Admin role should be changed to MODERATOR");
        verify(adminRepository).findById(adminId);
    }

    @Test
    void shouldThrowExceptionWhenChangingRoleForNonExistentAdmin() {
        // GIVEN
        Long nonExistentAdminId = 999L;

        when(adminRepository.findById(nonExistentAdminId)).thenReturn(Optional.empty());

        // WHEN & THEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> adminService.changeAdminRole(nonExistentAdminId, Admin.AdminRole.SUPER_ADMIN),
                "Should throw EntityNotFoundException when admin is not found"
        );

        assertEquals("Admin not found", exception.getMessage(), "Exception message should be 'Admin not found'");
        verify(adminRepository).findById(nonExistentAdminId);
    }

    @Test
    void shouldChangeAdminRoleMultipleTimes() {
        // GIVEN
        Long adminId = 1L;
        Admin admin = new Admin();
        admin.setId(adminId);
        admin.setName("John");
        admin.setRole(Admin.AdminRole.SUPER_ADMIN);

        when(adminRepository.findById(adminId)).thenReturn(Optional.of(admin));

        // WHEN - First change
        adminService.changeAdminRole(adminId, Admin.AdminRole.MODERATOR);

        // THEN - First change
        assertEquals(Admin.AdminRole.MODERATOR, admin.getRole(),
                "Role should be changed to MODERATOR");

        // WHEN - Second change
        adminService.changeAdminRole(adminId, Admin.AdminRole.SUPER_ADMIN);

        // THEN - Second change
        assertEquals(Admin.AdminRole.SUPER_ADMIN, admin.getRole(),
                "Role should be changed back to SUPER_ADMIN");
        verify(adminRepository, times(2)).findById(adminId);
    }

    private List<Admin> createAdminsList(int count) {
        List<Admin> admins = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            Admin admin = new Admin();
            admin.setId((long) i);
            admin.setName("Admin" + i);
            admin.setSurname("Test" + i);
            admin.setEmail("admin" + i + "@test.pl");
            admin.setRole(Admin.AdminRole.SUPER_ADMIN);
            admin.setActive(true);
            admin.setCreatedAt(LocalDate.now());
            admins.add(admin);
        }
        return admins;
    }
}