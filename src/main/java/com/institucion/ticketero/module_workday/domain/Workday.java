package com.institucion.ticketero.module_workday.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "workdays")
public class Workday {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private WorkdayStatus status;

    public Workday() {
    }

    public Workday(LocalDateTime startTime, LocalDateTime endTime, WorkdayStatus status) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public WorkdayStatus getStatus() {
        return status;
    }

    public void setStatus(WorkdayStatus status) {
        this.status = status;
    }

    public boolean isOpen() {
        return this.status == WorkdayStatus.OPEN;
    }

    public void close() {
        if (this.status == WorkdayStatus.OPEN) {
            this.status = WorkdayStatus.CLOSED;
            this.endTime = LocalDateTime.now();
        }
    }
}