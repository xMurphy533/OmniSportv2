package pl.omnisport.api.coach;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachServiceTest {

    @Mock
    private CoachRepository coachRepository;

    @InjectMocks
    private CoachService coachService;

    @Test
    void shouldSaveNewCoach() {
        // GIVEN
        Coach coach = createCoach(1L, "Adam", "Nowak", 35, "BJJ");

        // WHEN
        coachService.saveNewCoach(coach);

        // THEN
        verify(coachRepository).save(coach);
    }

    @Test
    void shouldGetAllCoaches() {
        // GIVEN
        Pageable pageable = PageRequest.of(0, 10);
        List<Coach> coaches = createCoachesList(3);
        Page<Coach> expectedPage = new PageImpl<>(coaches, pageable, 3);

        when(coachRepository.findAll(pageable)).thenReturn(expectedPage);

        // WHEN
        Page<Coach> result = coachService.getAllCoaches(pageable);

        // THEN
        assertNotNull(result, "Result should not be null");
        assertEquals(3, result.getContent().size(), "Page should contain 3 coaches");
        assertEquals(3, result.getTotalElements(), "Total elements should be 3");
        assertEquals(1, result.getTotalPages(), "Total pages should be 1");
        assertTrue(result.isFirst(), "Should be first page");
        assertTrue(result.isLast(), "Should be last page");
        verify(coachRepository).findAll(pageable);
    }

    @Test
    void shouldUpdateCoachSpecialization() {
        // GIVEN
        Long coachId = 1L;
        String newSpecialization = "Kickboxing";
        Coach coach = createCoach(coachId, "Adam", "Nowak", 35, "Muay Thai");

        when(coachRepository.findById(coachId)).thenReturn(Optional.of(coach));

        // WHEN
        coachService.updateCoachSpecialization(coachId, newSpecialization);

        // THEN
        assertEquals(newSpecialization, coach.getSpecialization(), "Specialization should be updated");
        verify(coachRepository).findById(coachId);
        verify(coachRepository).save(coach);
    }

    @Test
    void shouldThrowExceptionWhenUpdatingSpecializationForMissingCoach() {
        // GIVEN
        Long coachId = 999L;

        when(coachRepository.findById(coachId)).thenReturn(Optional.empty());

        // WHEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> coachService.updateCoachSpecialization(coachId, "MMA"),
                "Should throw EntityNotFoundException when coach is missing"
        );

        // THEN
        assertEquals("Coach not found", exception.getMessage(), "Exception message should match");
        verify(coachRepository).findById(coachId);
    }

    @Test
    void shouldRemoveCoach() {
        // GIVEN
        Long coachId = 1L;
        Coach coach = createCoach(coachId, "Adam", "Nowak", 35, "Strength");

        when(coachRepository.findById(coachId)).thenReturn(Optional.of(coach));

        // WHEN
        coachService.removeCoach(coachId);

        // THEN
        verify(coachRepository).findById(coachId);
        verify(coachRepository).delete(coach);
    }

    @Test
    void shouldThrowExceptionWhenRemovingMissingCoach() {
        // GIVEN
        Long coachId = 999L;

        when(coachRepository.findById(coachId)).thenReturn(Optional.empty());

        // WHEN
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> coachService.removeCoach(coachId),
                "Should throw EntityNotFoundException when coach is missing"
        );

        // THEN
        assertEquals("Coach not found", exception.getMessage(), "Exception message should match");
        verify(coachRepository).findById(coachId);
    }

    private Coach createCoach(Long id, String name, String surname, int age, String specialization) {
        Coach coach = new Coach();
        coach.setId(id);
        coach.setName(name);
        coach.setSurname(surname);
        coach.setAge(age);
        coach.setSpecialization(specialization);
        return coach;
    }

    private List<Coach> createCoachesList(int count) {
        List<Coach> coaches = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            coaches.add(createCoach((long) i, "Coach" + i, "Test" + i, 30 + i, "Spec" + i));
        }
        return coaches;
    }
}
