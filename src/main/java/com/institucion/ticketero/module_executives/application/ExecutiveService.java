package com.institucion.ticketero.module_executives.application;

import com.institucion.ticketero.common.exceptions.ResourceNotFoundException;
import com.institucion.ticketero.module_executives.api.CreateExecutiveRequest;
import com.institucion.ticketero.module_executives.api.ExecutiveDetailsResponse;
import com.institucion.ticketero.module_executives.api.UpdateExecutiveRequest;
import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_executives.domain.ExecutiveStatus;
import com.institucion.ticketero.module_executives.infrastructure.ExecutiveRepository;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ExecutiveService {

    private final ExecutiveRepository executiveRepository;

    public ExecutiveService(ExecutiveRepository executiveRepository) {
        this.executiveRepository = executiveRepository;
    }

    @Transactional
    public ExecutiveDetailsResponse createExecutive(CreateExecutiveRequest request) {
        Executive executive = new Executive();
        executive.setFullName(request.fullName());
        executive.setModule(request.module());
        executive.setStatus(ExecutiveStatus.AVAILABLE); // New executives are available by default
        executive.setSupportedAttentionTypes(new HashSet<>(request.supportedAttentionTypes()));
        Executive savedExecutive = executiveRepository.save(executive);
        return mapToExecutiveDetailsResponse(savedExecutive);
    }

    public List<ExecutiveDetailsResponse> getAllExecutives() {
        return executiveRepository.findAll().stream()
                .map(this::mapToExecutiveDetailsResponse)
                .collect(Collectors.toList());
    }

    public ExecutiveDetailsResponse getExecutiveById(Long executiveId) {
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive not found with ID: " + executiveId));
        return mapToExecutiveDetailsResponse(executive);
    }

    @Transactional
    public ExecutiveDetailsResponse updateExecutive(Long executiveId, UpdateExecutiveRequest request) {
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive not found with ID: " + executiveId));
        
        executive.setFullName(request.fullName());
        executive.setModule(request.module());
        // Status is managed separately, not via this update request
        executive.setSupportedAttentionTypes(new HashSet<>(request.supportedAttentionTypes()));
        Executive updatedExecutive = executiveRepository.save(executive);
        return mapToExecutiveDetailsResponse(updatedExecutive);
    }

    @Transactional
    public void deleteExecutive(Long executiveId) {
        if (!executiveRepository.existsById(executiveId)) {
            throw new ResourceNotFoundException("Executive not found with ID: " + executiveId);
        }
        executiveRepository.deleteById(executiveId);
    }

    @Transactional
    public void updateSupportedAttentionTypes(Long executiveId, List<AttentionType> attentionTypes) {
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive not found with ID: " + executiveId));
        
        executive.setSupportedAttentionTypes(new HashSet<>(attentionTypes));
        executiveRepository.save(executive);
    }

    private ExecutiveDetailsResponse mapToExecutiveDetailsResponse(Executive executive) {
        return new ExecutiveDetailsResponse(
                executive.getId(),
                executive.getFullName(),
                executive.getModule(),
                executive.getStatus(),
                executive.getSupportedAttentionTypes()
        );
    }
}
