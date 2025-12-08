package com.institucion.ticketero.module_workday.application;

import com.institucion.ticketero.BaseIntegrationTest;
import com.institucion.ticketero.module_workday.domain.Workday;
import com.institucion.ticketero.module_workday.domain.WorkdayStatus;
import com.institucion.ticketero.module_workday.infrastructure.WorkdayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class WorkdayServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WorkdayService workdayService;

    @Autowired
    private WorkdayRepository workdayRepository;

    @BeforeEach
    void setUp() {
        // Clean up the repository before each test to ensure isolation
        workdayRepository.deleteAll();
    }

    @Test
    void whenStartNewDay_thenWorkdayIsActiveAndSaved() {
        // When
        Workday startedDay = workdayService.startNewDay();

        // Then
        assertNotNull(startedDay.getId());
        assertEquals(WorkdayStatus.ACTIVE, startedDay.getStatus());
        assertNotNull(startedDay.getStartTime());
        assertNull(startedDay.getEndTime());

        Optional<Workday> foundDay = workdayRepository.findById(startedDay.getId());
        assertTrue(foundDay.isPresent());
        assertEquals(WorkdayStatus.ACTIVE, foundDay.get().getStatus());
    }

    @Test
    void whenStartNewDayWhileAnotherIsActive_thenThrowException() {
        // Given
        workdayService.startNewDay(); // An active day already exists

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            workdayService.startNewDay();
        });
    }

    @Test
    void whenEndCurrentDay_thenWorkdayIsEndedAndUpdated() {
        // Given
        Workday activeDay = workdayService.startNewDay();
        assertNotNull(activeDay);

        // When
        Workday endedDay = workdayService.endCurrentDay();

        // Then
        assertEquals(activeDay.getId(), endedDay.getId());
        assertEquals(WorkdayStatus.ENDED, endedDay.getStatus());
        assertNotNull(endedDay.getEndTime());

        Optional<Workday> foundDay = workdayRepository.findById(endedDay.getId());
        assertTrue(foundDay.isPresent());
        assertEquals(WorkdayStatus.ENDED, foundDay.get().getStatus());
    }

    @Test
    void whenEndDayWithNoActiveDay_thenThrowException() {
        // Given no active day

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            workdayService.endCurrentDay();
        });
    }
}
