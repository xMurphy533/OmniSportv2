package pl.omnisport.api.admin;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.omnisport.api.auth.AdminRegisterRequest;
import pl.omnisport.api.exception.SelfDeletionNotAllowedException;
import pl.omnisport.api.user.AppUser;
import pl.omnisport.api.user.AppUserRepository;
import pl.omnisport.api.user.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    @Test
    void givenNewAdminRequest_whenRegisterNewAdmin_thenPersistUserAndAdmin() {
        // Given
        AdminRegisterRequest request = new AdminRegisterRequest(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "secret",
                Admin.AdminRole.SUPER_ADMIN
        );
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encoded-secret");
        when(appUserRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(adminRepository.save(any(Admin.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        adminService.registerNewAdmin(request);

        // Then
        ArgumentCaptor<AppUser> appUserCaptor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(appUserCaptor.capture());
        AppUser savedUser = appUserCaptor.getValue();
        assertEquals(request.getEmail(), savedUser.getEmail());
        assertEquals("encoded-secret", savedUser.getPassword());
        assertEquals(Role.ADMIN, savedUser.getRole());
        assertTrue(savedUser.isActive());

        ArgumentCaptor<Admin> adminCaptor = ArgumentCaptor.forClass(Admin.class);
        verify(adminRepository).save(adminCaptor.capture());
        Admin savedAdmin = adminCaptor.getValue();
        assertEquals(request.getName(), savedAdmin.getName());
        assertEquals(request.getSurname(), savedAdmin.getSurname());
        assertEquals(request.getAdminRole(), savedAdmin.getAdminRole());
        assertNotNull(savedAdmin.getCreatedAt());
        assertEquals(savedUser, savedAdmin.getAppUser());
        verify(appUserRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verifyNoMoreInteractions(appUserRepository, passwordEncoder, adminRepository);
    }

    @Test
    void givenExistingEmail_whenRegisterNewAdmin_thenThrowIllegalArgumentException() {
        // Given
        AdminRegisterRequest request = new AdminRegisterRequest(
                "Jan",
                "Kowalski",
                "jan.kowalski@example.com",
                "secret",
                Admin.AdminRole.MODERATOR
        );
        when(appUserRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // When
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> adminService.registerNewAdmin(request));

        // Then
        assertEquals(
                "An account with the address jan.kowalski@example.com already exists in the system",
                exception.getMessage()
        );
        verify(appUserRepository).existsByEmail(request.getEmail());
        verifyNoInteractions(passwordEncoder, adminRepository);
    }

    @Test
    void givenAdminsPage_whenGetAllAdmins_thenMapToResponsePage() {
        // Given
        AppUser activeUser = new AppUser();
        activeUser.setActive(true);

        Admin activeAdmin = new Admin();
        activeAdmin.setId(1L);
        activeAdmin.setName("Jan");
        activeAdmin.setSurname("Kowalski");
        activeAdmin.setAdminRole(Admin.AdminRole.SUPER_ADMIN);
        activeAdmin.setAppUser(activeUser);

        Admin inactiveAdmin = new Admin();
        inactiveAdmin.setId(2L);
        inactiveAdmin.setName("Anna");
        inactiveAdmin.setSurname("Nowak");
        inactiveAdmin.setAdminRole(Admin.AdminRole.MODERATOR);
        inactiveAdmin.setAppUser(null);

        PageRequest pageable = PageRequest.of(0, 10);
        when(adminRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(activeAdmin, inactiveAdmin), pageable, 2));

        // When
        Page<AdminResponse> response = adminService.getAllAdmins(pageable);

        // Then
        assertEquals(2, response.getTotalElements());
        assertEquals(1L, response.getContent().get(0).getId());
        assertEquals("Jan", response.getContent().get(0).getName());
        assertTrue(response.getContent().get(0).isActive());
        assertEquals(2L, response.getContent().get(1).getId());
        assertEquals("Anna", response.getContent().get(1).getName());
        assertFalse(response.getContent().get(1).isActive());
        verify(adminRepository).findAll(pageable);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenExistingAdminId_whenFindAdminById_thenReturnAdminDetails() {
        // Given
        AppUser appUser = new AppUser();
        appUser.setActive(true);

        Admin admin = new Admin();
        admin.setId(10L);
        admin.setName("Jan");
        admin.setSurname("Kowalski");
        admin.setAdminRole(Admin.AdminRole.MODERATOR);
        admin.setCreatedAt(LocalDate.of(2024, 1, 15));
        admin.setLastLoginAt(LocalDate.of(2024, 2, 20));
        admin.setAppUser(appUser);
        when(adminRepository.findById(10L)).thenReturn(Optional.of(admin));

        // When
        AdminGetByIdResponse response = adminService.findAdminById(10L);

        // Then
        assertEquals(10L, response.getId());
        assertEquals("Jan", response.getName());
        assertEquals("Kowalski", response.getSurname());
        assertEquals(Admin.AdminRole.MODERATOR, response.getAdminRole());
        assertEquals(LocalDate.of(2024, 1, 15), response.getCreatedAt());
        assertEquals(LocalDate.of(2024, 2, 20), response.getLastLoginAt());
        assertTrue(response.isActive());
        verify(adminRepository).findById(10L);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenMissingAdminId_whenFindAdminById_thenThrowEntityNotFoundException() {
        // Given
        when(adminRepository.findById(10L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> adminService.findAdminById(10L));

        // Then
        assertEquals("Admin not found", exception.getMessage());
        verify(adminRepository).findById(10L);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenExistingAdminEmail_whenFindAdminByEmail_thenReturnAdminWrappedInOptional() {
        // Given
        Admin admin = new Admin();
        admin.setId(11L);
        admin.setName("Anna");
        when(adminRepository.findByAppUserEmail("anna@example.com")).thenReturn(Optional.of(admin));

        // When
        Optional<Admin> response = adminService.findAdminByEmail("anna@example.com");

        // Then
        assertTrue(response.isPresent());
        assertEquals(admin, response.orElseThrow());
        verify(adminRepository).findByAppUserEmail("anna@example.com");
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenMissingAdminEmail_whenFindAdminByEmail_thenThrowEntityNotFoundException() {
        // Given
        when(adminRepository.findByAppUserEmail("missing@example.com")).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> adminService.findAdminByEmail("missing@example.com"));

        // Then
        assertEquals("Admin not found", exception.getMessage());
        verify(adminRepository).findByAppUserEmail("missing@example.com");
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenDifferentCurrentUser_whenDeactivateAdmin_thenDeactivateTargetUser() {
        // Given
        AppUser currentUser = new AppUser();
        currentUser.setId(1L);

        AppUser targetUser = new AppUser();
        targetUser.setId(2L);
        targetUser.setActive(true);

        Admin targetAdmin = new Admin();
        targetAdmin.setId(20L);
        targetAdmin.setAppUser(targetUser);
        when(adminRepository.findById(20L)).thenReturn(Optional.of(targetAdmin));
        when(adminRepository.save(targetAdmin)).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        adminService.deactivateAdmin(20L, currentUser.getId());

        // Then
        assertFalse(targetUser.isActive());
        verify(adminRepository).findById(20L);
        verify(adminRepository).save(targetAdmin);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenCurrentUserIsTargetAdmin_whenDeactivateAdmin_thenThrowSelfDeletionNotAllowedException() {
        // Given
        AppUser targetUser = new AppUser();
        targetUser.setId(1L);
        targetUser.setActive(true);

        Admin targetAdmin = new Admin();
        targetAdmin.setAppUser(targetUser);
        when(adminRepository.findById(20L)).thenReturn(Optional.of(targetAdmin));

        // When
        SelfDeletionNotAllowedException exception = assertThrows(SelfDeletionNotAllowedException.class,
                () -> adminService.deactivateAdmin(20L, 1L));

        // Then
        assertEquals("You cannot deactivate your account", exception.getMessage());
        assertTrue(targetUser.isActive());
        verify(adminRepository).findById(20L);
        verify(adminRepository, never()).save(any(Admin.class));
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenMissingTargetAdmin_whenDeactivateAdmin_thenThrowEntityNotFoundException() {
        // Given
        when(adminRepository.findById(20L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> adminService.deactivateAdmin(20L, 1L));

        // Then
        assertEquals("Admin not found", exception.getMessage());
        verify(adminRepository).findById(20L);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenExistingAdminId_whenRecordLogin_thenSetLastLoginDate() {
        // Given
        Admin admin = new Admin();
        when(adminRepository.findById(30L)).thenReturn(Optional.of(admin));

        // When
        adminService.recordLogin(30L);

        // Then
        assertEquals(LocalDate.now(), admin.getLastLoginAt());
        verify(adminRepository).findById(30L);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenMissingAdminId_whenRecordLogin_thenThrowEntityNotFoundException() {
        // Given
        when(adminRepository.findById(30L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> adminService.recordLogin(30L));

        // Then
        assertEquals("Admin not found", exception.getMessage());
        verify(adminRepository).findById(30L);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenExistingAdminId_whenChangeAdminRole_thenUpdateAdminRole() {
        // Given
        Admin admin = new Admin();
        admin.setAdminRole(Admin.AdminRole.MODERATOR);
        when(adminRepository.findById(40L)).thenReturn(Optional.of(admin));

        // When
        adminService.changeAdminRole(40L, Admin.AdminRole.SUPER_ADMIN);

        // Then
        assertEquals(Admin.AdminRole.SUPER_ADMIN, admin.getAdminRole());
        verify(adminRepository).findById(40L);
        verifyNoMoreInteractions(adminRepository);
    }

    @Test
    void givenMissingAdminId_whenChangeAdminRole_thenThrowEntityNotFoundException() {
        // Given
        when(adminRepository.findById(40L)).thenReturn(Optional.empty());

        // When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> adminService.changeAdminRole(40L, Admin.AdminRole.SUPER_ADMIN));

        // Then
        assertEquals("Admin not found", exception.getMessage());
        verify(adminRepository).findById(40L);
        verifyNoMoreInteractions(adminRepository);
    }
}
