package com.institucion.ticketero.module_executives.application;

import com.institucion.ticketero.common.exceptions.ResourceNotFoundException;
import com.institucion.ticketero.module_executives.api.CreateExecutiveRequest;
import com.institucion.ticketero.module_executives.api.ExecutiveDetailsResponse;
import com.institucion.ticketero.module_executives.api.UpdateExecutiveRequest;
import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_executives.infrastructure.ExecutiveRepository;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutiveServiceTest {

    @Mock
    private ExecutiveRepository executiveRepository;

    @InjectMocks
    private ExecutiveService executiveService;

    private Executive executive;
    private Long executiveId;

    @BeforeEach
    void setUp() {
        executiveId = 1L;
        executive = new Executive();
        executive.setId(executiveId);
        executive.setFullName("John Doe");
        executive.setModule("A1");
        executive.setStatus(ExecutiveStatus.AVAILABLE);
        executive.setSupportedAttentionTypes(new HashSet<>(Set.of(AttentionType.CAJA)));
    }

    @Test
    void createExecutive_shouldReturnExecutiveDetails() {
        CreateExecutiveRequest request = new CreateExecutiveRequest("Jane Doe", "B2", Set.of(AttentionType.PERSONAL_BANKER));
        
        when(executiveRepository.save(any(Executive.class))).thenAnswer(invocation -> {
            Executive exec = invocation.getArgument(0);
            exec.setId(2L);
            return exec;
        });

        ExecutiveDetailsResponse response = executiveService.createExecutive(request);

        assertNotNull(response);
        assertNotNull(response.id());
        assertEquals("Jane Doe", response.fullName());
        assertEquals("B2", response.module());
        assertEquals(ExecutiveStatus.AVAILABLE, response.status());
        assertTrue(response.supportedAttentionTypes().contains(AttentionType.PERSONAL_BANKER));

        verify(executiveRepository, times(1)).save(any(Executive.class));
    }

    @Test
    void getAllExecutives_shouldReturnListOfExecutives() {
        when(executiveRepository.findAll()).thenReturn(List.of(executive));

        List<ExecutiveDetailsResponse> responses = executiveService.getAllExecutives();

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(executiveId, responses.get(0).id());
        verify(executiveRepository, times(1)).findAll();
    }
    
    @Test
    void getExecutiveById_whenExecutiveExists_shouldReturnExecutiveDetails() {
        when(executiveRepository.findById(executiveId)).thenReturn(Optional.of(executive));

        ExecutiveDetailsResponse response = executiveService.getExecutiveById(executiveId);

        assertNotNull(response);
        assertEquals(executiveId, response.id());
        verify(executiveRepository, times(1)).findById(executiveId);
    }

    @Test
    void getExecutiveById_whenExecutiveDoesNotExist_shouldThrowResourceNotFoundException() {
        Long nonExistentId = 2L;
        when(executiveRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> executiveService.getExecutiveById(nonExistentId));
        verify(executiveRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void updateExecutive_whenExecutiveExists_shouldUpdateAndReturnExecutiveDetails() {
        UpdateExecutiveRequest request = new UpdateExecutiveRequest("Johnathan Doe", "C3", Set.of(AttentionType.EMPRESAS));
        when(executiveRepository.findById(executiveId)).thenReturn(Optional.of(executive));
        when(executiveRepository.save(any(Executive.class))).thenReturn(executive);
        
        ExecutiveDetailsResponse response = executiveService.updateExecutive(executiveId, request);

        assertNotNull(response);
        assertEquals("Johnathan Doe", response.fullName());
        assertEquals("C3", response.module());
        assertTrue(response.supportedAttentionTypes().contains(AttentionType.EMPRESAS));

        verify(executiveRepository, times(1)).findById(executiveId);
        verify(executiveRepository, times(1)).save(executive);
    }

    @Test
    void updateExecutive_whenExecutiveDoesNotExist_shouldThrowResourceNotFoundException() {
        Long nonExistentId = 2L;
        UpdateExecutiveRequest request = new UpdateExecutiveRequest("Johnathan Doe", "C3", Set.of(AttentionType.EMPRESAS));
        when(executiveRepository.findById(nonExistentId)).thenReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> executiveService.updateExecutive(nonExistentId, request));

        verify(executiveRepository, times(1)).findById(nonExistentId);
        verify(executiveRepository, never()).save(any(Executive.class));
    }

    @Test
    void deleteExecutive_whenExecutiveExists_shouldDeleteExecutive() {
        when(executiveRepository.existsById(executiveId)).thenReturn(true);
        doNothing().when(executiveRepository).deleteById(executiveId);

        executiveService.deleteExecutive(executiveId);

        verify(executiveRepository, times(1)).existsById(executiveId);
        verify(executiveRepository, times(1)).deleteById(executiveId);
    }

    @Test
    void deleteExecutive_whenExecutiveDoesNotExist_shouldThrowResourceNotFoundException() {
        Long nonExistentId = 2L;
        when(executiveRepository.existsById(nonExistentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> executiveService.deleteExecutive(nonExistentId));

        verify(executiveRepository, times(1)).existsById(nonExistentId);
        verify(executiveRepository, never()).deleteById(any(Long.class));
    }
    
    @Test
    void updateSupportedAttentionTypes_whenExecutiveExists_shouldUpdateTypes() {
        List<AttentionType> newTypes = List.of(AttentionType.PERSONAL_BANKER, AttentionType.CAJA);
        when(executiveRepository.findById(executiveId)).thenReturn(Optional.of(executive));
        when(executiveRepository.save(any(Executive.class))).thenReturn(executive);

        executiveService.updateSupportedAttentionTypes(executiveId, newTypes);

        assertEquals(2, executive.getSupportedAttentionTypes().size());
        assertTrue(executive.getSupportedAttentionTypes().containsAll(newTypes));

        verify(executiveRepository, times(1)).findById(executiveId);
        verify(executiveRepository, times(1)).save(executive);
    }

    @Test
    void updateSupportedAttentionTypes_whenExecutiveDoesNotExist_shouldThrowResourceNotFoundException() {
        Long nonExistentId = 2L;
        List<AttentionType> newTypes = List.of(AttentionType.PERSONAL_BANKER);
        when(executiveRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> executiveService.updateSupportedAttentionTypes(nonExistentId, newTypes));
        
        verify(executiveRepository, times(1)).findById(nonExistentId);
        verify(executiveRepository, never()).save(any(Executive.class));
    }
}
