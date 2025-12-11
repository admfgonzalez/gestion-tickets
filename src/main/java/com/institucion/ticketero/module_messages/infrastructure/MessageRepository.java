package com.institucion.ticketero.module_messages.infrastructure;

import com.institucion.ticketero.module_messages.domain.Message;
import com.institucion.ticketero.module_messages.domain.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByEstadoEnvioAndFechaProgramadaBefore(MessageStatus estadoEnvio, LocalDateTime fechaProgramada);
}
