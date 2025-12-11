package com.institucion.ticketero.module_workday.application;

import com.institucion.ticketero.common.exceptions.ResourceNotFoundException;
import com.institucion.ticketero.module_workday.domain.Workday;
import com.institucion.ticketero.module_workday.domain.WorkdayStatus;
import com.institucion.ticketero.module_workday.infrastructure.WorkdayRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkdayServiceTest {

    @Mock
    private WorkdayRepository workdayRepository;

    @InjectMocks
    private WorkdayService workdayService;

    private Workday activeWorkday;
    private Long activeWorkdayId;

    @BeforeEach
    void setUp() {
        activeWorkdayId = 1L;
        activeWorkday = new Workday(LocalDateTime.now().minusHours(1), null, WorkdayStatus.OPEN);
        activeWorkday.setId(activeWorkdayId);
    }

    @Test
    void testOpenNewWorkday_noExistingWorkday() {
        when(workdayRepository.findByStatus(WorkdayStatus.OPEN)).thenReturn(Optional.empty());
        when(workdayRepository.save(any(Workday.class))).thenAnswer(invocation -> {
            Workday savedWorkday = invocation.getArgument(0);
            savedWorkday.setId(2L);
            return savedWorkday;
        });

        Workday newWorkday = workdayService.openNewWorkday();

        assertNotNull(newWorkday);
        assertNotNull(newWorkday.getId());
        assertEquals(WorkdayStatus.OPEN, newWorkday.getStatus());
        verify(workdayRepository, times(1)).findByStatus(WorkdayStatus.OPEN);
        verify(workdayRepository, times(1)).save(any(Workday.class));
    }

    @Test
    void testOpenNewWorkday_existingWorkdayIsClosed() {
        when(workdayRepository.findByStatus(WorkdayStatus.OPEN)).thenReturn(Optional.of(activeWorkday));
        when(workdayRepository.save(any(Workday.class))).thenAnswer(invocation -> {
            Workday savedWorkday = invocation.getArgument(0);
            if (savedWorkday.getId() == null) {
                savedWorkday.setId(2L);
            }
            return savedWorkday;
        });

        Workday newWorkday = workdayService.openNewWorkday();

        assertNotNull(newWorkday);
        assertNotNull(newWorkday.getId());
        assertEquals(WorkdayStatus.CLOSED, activeWorkday.getStatus()); // Old workday should be closed
        assertNotNull(activeWorkday.getEndTime());
        assertEquals(WorkdayStatus.OPEN, newWorkday.getStatus()); // New workday should be open
        verify(workdayRepository, times(1)).findByStatus(WorkdayStatus.OPEN);
        verify(workdayRepository, times(2)).save(any(Workday.class)); // One for closing, one for new
    }

    @Test
    void testCloseWorkday_success() {
        when(workdayRepository.findById(activeWorkdayId)).thenReturn(Optional.of(activeWorkday));
        when(workdayRepository.save(any(Workday.class))).thenReturn(activeWorkday);

        Workday closedWorkday = workdayService.closeWorkday(activeWorkdayId);

        assertNotNull(closedWorkday);
        assertEquals(WorkdayStatus.CLOSED, closedWorkday.getStatus());
        assertNotNull(closedWorkday.getEndTime());
        verify(workdayRepository, times(1)).findById(activeWorkdayId);
        verify(workdayRepository, times(1)).save(activeWorkday);
    }

    @Test
    void testCloseWorkday_notFound() {
        when(workdayRepository.findById(activeWorkdayId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                workdayService.closeWorkday(activeWorkdayId));

        assertEquals("Workday not found with id: " + activeWorkdayId, exception.getMessage());
        verify(workdayRepository, times(1)).findById(activeWorkdayId);
        verify(workdayRepository, never()).save(any(Workday.class));
    }

    @Test
    void testGetCurrentActiveWorkday_exists() {
        when(workdayRepository.findByStatus(WorkdayStatus.OPEN)).thenReturn(Optional.of(activeWorkday));

        Workday currentWorkday = workdayService.getCurrentActiveWorkday();

        assertNotNull(currentWorkday);
        assertEquals(activeWorkdayId, currentWorkday.getId());
        assertEquals(WorkdayStatus.OPEN, currentWorkday.getStatus());
        verify(workdayRepository, times(1)).findByStatus(WorkdayStatus.OPEN);
        verify(workdayRepository, never()).save(any(Workday.class));
    }

    @Test
    void testGetCurrentActiveWorkday_notFound_opensNew() {
        when(workdayRepository.findByStatus(WorkdayStatus.OPEN)).thenReturn(Optional.empty()).thenReturn(Optional.empty());
        when(workdayRepository.save(any(Workday.class))).thenAnswer(invocation -> {
            Workday savedWorkday = invocation.getArgument(0);
            savedWorkday.setId(2L);
            return savedWorkday;
        });

        Workday newWorkday = workdayService.getCurrentActiveWorkday();

        assertNotNull(newWorkday);
        assertNotNull(newWorkday.getId());
        assertEquals(WorkdayStatus.OPEN, newWorkday.getStatus());
        verify(workdayRepository, times(2)).findByStatus(WorkdayStatus.OPEN);
        verify(workdayRepository, times(1)).save(any(Workday.class));
    }

    @Test
    void testGetWorkdayById_success() {
        when(workdayRepository.findById(activeWorkdayId)).thenReturn(Optional.of(activeWorkday));

        Workday foundWorkday = workdayService.getWorkdayById(activeWorkdayId);

        assertNotNull(foundWorkday);
        assertEquals(activeWorkdayId, foundWorkday.getId());
        verify(workdayRepository, times(1)).findById(activeWorkdayId);
    }

    @Test
    void testGetWorkdayById_notFound() {
        when(workdayRepository.findById(activeWorkdayId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                workdayService.getWorkdayById(activeWorkdayId));

        verify(workdayRepository, times(1)).findById(activeWorkdayId);
    }
}
