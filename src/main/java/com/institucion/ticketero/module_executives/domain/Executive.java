package com.institucion.ticketero.module_executives.domain;

import com.institucion.ticketero.module_queues.domain.AttentionType;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * Q-Insight: Domain Entity for Executive.
 * Represents a service executive who attends to customers. This is a key aggregate root in the executive module.
 * It manages the executive's status (AVAILABLE, BUSY) and their assigned service desk/module.
 * This entity is crucial for the automatic ticket assignment logic (RF-004).
 */
@Entity
@Table(name = "executives")
public class Executive {

    /**
     * Q-Insight: Primary Key.
     * Unique identifier for the executive.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /**
     * Q-Insight: Executive's Full Name.
     * The name of the executive, to be displayed to the customer (RF-002).
     */
    @Column(nullable = false)
    private String fullName;

    /**
     * Q-Insight: Service Desk/Module Identifier.
     * The physical location or desk number where the executive works (e.g., "Module 3").
     */
    @Column(nullable = false, unique = true)
    private String module;

    /**
     * Q-Insight: Executive Status.
     * The current availability of the executive. This field is frequently updated.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutiveStatus status;

    /**
     * Q-Insight: Supported Attention Types.
     * A set of attention types that this executive is qualified to handle.
     * This allows for flexible assignment rules (e.g., a versatile executive can handle both CAJA and PERSONAL_BANKER).
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "executive_attention_types", joinColumns = @JoinColumn(name = "executive_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "attention_type", nullable = false)
    private Set<AttentionType> supportedAttentionTypes;

    /**
     * Q-Insight: Last Status Change Timestamp.
     * Records when the executive's status last changed. Useful for performance metrics and monitoring.
     */
    private LocalDateTime lastStatusChange;
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public ExecutiveStatus getStatus() {
        return status;
    }

    public void setStatus(ExecutiveStatus status) {
        this.status = status;
    }

    public Set<AttentionType> getSupportedAttentionTypes() {
        return supportedAttentionTypes;
    }

    public void setSupportedAttentionTypes(Set<AttentionType> supportedAttentionTypes) {
        this.supportedAttentionTypes = supportedAttentionTypes;
    }

    public LocalDateTime getLastStatusChange() {
        return lastStatusChange;
    }

    public void setLastStatusChange(LocalDateTime lastStatusChange) {
        this.lastStatusChange = lastStatusChange;
    }
}
