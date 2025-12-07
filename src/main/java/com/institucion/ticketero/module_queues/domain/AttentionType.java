package com.institucion.ticketero.module_queues.domain;

/**
 * Q-Insight: Domain Enum for Attention Type.
 * Represents the different types of service queues a customer can join.
 * This is a core concept in the domain, driving rules for priority and service times.
 * RF-005 specifies these types.
 */
public enum AttentionType {
    CAJA(5, 1),             // 5 minutes average time, priority 1 (low)
    PERSONAL_BANKER(15, 2), // 15 minutes average time, priority 2 (medium)
    EMPRESAS(20, 2),        // 20 minutes average time, priority 2 (medium)
    GERENCIA(30, 3);        // 30 minutes average time, priority 3 (max)

    private final int averageServiceTimeMinutes;
    private final int priority;

    AttentionType(int averageServiceTimeMinutes, int priority) {
        this.averageServiceTimeMinutes = averageServiceTimeMinutes;
        this.priority = priority;
    }

    public int getAverageServiceTimeMinutes() {
        return averageServiceTimeMinutes;
    }

    public int getPriority() {
        return priority;
    }
}
