package com.institucion.ticketero.module_messages.domain;

import com.institucion.ticketero.module_tickets.domain.Ticket;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id")
    private Ticket ticket;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageTemplate plantilla;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageStatus estadoEnvio;

    @Column(nullable = false)
    private LocalDateTime fechaProgramada;

    private LocalDateTime fechaEnvio;

    private String telegramMessageId;

    @Column(nullable = false)
    private int intentos = 0;

    @Column(nullable = false)
    private String chatId;

    @Lob
    private String content;
    
    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public MessageTemplate getPlantilla() {
        return plantilla;
    }

    public void setPlantilla(MessageTemplate plantilla) {
        this.plantilla = plantilla;
    }

    public MessageStatus getEstadoEnvio() {
        return estadoEnvio;
    }

    public void setEstadoEnvio(MessageStatus estadoEnvio) {
        this.estadoEnvio = estadoEnvio;
    }

    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDateTime fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public String getTelegramMessageId() {
        return telegramMessageId;
    }

    public void setTelegramMessageId(String telegramMessageId) {
        this.telegramMessageId = telegramMessageId;
    }

    public int getIntentos() {
        return intentos;
    }

    public void setIntentos(int intentos) {
        this.intentos = intentos;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
