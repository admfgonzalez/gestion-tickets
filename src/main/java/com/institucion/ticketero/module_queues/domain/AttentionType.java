package com.institucion.ticketero.module_queues.domain;

/**
 * Q-Insight: Domain Enum for Attention Type.
 * Represents the different types of service queues a customer can join.
 * This is a core concept in the domain, driving rules for priority and service times.
 * RF-005 specifies these types.
 */
public enum AttentionType {
    CAJA(5, 1, "C"),             // 5 minutes average time, priority 1 (low)
    PERSONAL_BANKER(15, 2, "P"), // 15 minutes average time, priority 2 (medium)
    EMPRESAS(20, 3, "E"),        // 20 minutes average time, priority 3 (medium-high)
    GERENCIA(30, 4, "G");        // 30 minutes average time, priority 4 (max)

    private final int averageServiceTimeMinutes;
    private final int priority;
    private final String prefix;

    AttentionType(int averageServiceTimeMinutes, int priority, String prefix) {
        this.averageServiceTimeMinutes = averageServiceTimeMinutes;
        this.priority = priority;
        this.prefix = prefix;
    }

    public int getAverageServiceTimeMinutes() {
        return averageServiceTimeMinutes;
    }

    public int getPriority() {
        return priority;
    }

    public String getPrefix() {
        return prefix;
    }
}
