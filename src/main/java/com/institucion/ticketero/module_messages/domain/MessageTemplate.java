package com.institucion.ticketero.module_messages.domain;

public enum MessageTemplate {
    TOTEM_TICKET_CREADO("✅ <b>Ticket Creado</b>\n\nTu número de turno: <b>%s</b>\nPosición en cola: <b>#%d</b>\nTiempo estimado: <b>%d minutos</b>\n\nTe notificaremos cuando estés próximo."),
    TOTEM_PROXIMO_TURNO("⏰ <b>¡Pronto será tu turno!</b>\n\nTurno: <b>%s</b>\nFaltan aproximadamente 3 turnos.\n\nPor favor, acércate a la sucursal."),
    TOTEM_ES_TU_TURNO("🔔 <b>¡ES TU TURNO %s!</b>\n\nDirígete al módulo: <b>%s</b>\nAsesor: <b>%s</b>");

    private final String template;

    MessageTemplate(String template) {
        this.template = template;
    }

    public String getTemplate() {
        return template;
    }
}
