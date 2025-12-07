package com.institucion.ticketero.module_executives.application;

import com.institucion.ticketero.common.exceptions.ResourceNotFoundException;
import com.institucion.ticketero.module_executives.domain.Executive;
import com.institucion.ticketero.module_executives.infrastructure.ExecutiveRepository;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class ExecutiveService {

    private final ExecutiveRepository executiveRepository;

    public ExecutiveService(ExecutiveRepository executiveRepository) {
        this.executiveRepository = executiveRepository;
    }

    @Transactional
    public void updateSupportedAttentionTypes(UUID executiveId, List<AttentionType> attentionTypes) {
        Executive executive = executiveRepository.findById(executiveId)
                .orElseThrow(() -> new ResourceNotFoundException("Executive not found with ID: " + executiveId));
        
        executive.setSupportedAttentionTypes(new HashSet<>(attentionTypes));
        executiveRepository.save(executive);
    }
}
