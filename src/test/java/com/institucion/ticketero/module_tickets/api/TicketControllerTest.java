package com.institucion.ticketero.module_tickets.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.institucion.ticketero.module_queues.domain.AttentionType;
import com.institucion.ticketero.module_tickets.application.TicketService;
import com.institucion.ticketero.module_tickets.domain.TicketStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TicketController.class)
@AutoConfigureMockMvc(addFilters = false)
class TicketControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TicketService ticketService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testCreateTicket() throws Exception {
        // Given
        CreateTicketRequest request = new CreateTicketRequest("12345678-9", AttentionType.CAJA, "12345", "Main Branch");
        CreateTicketResponse response = new CreateTicketResponse(UUID.randomUUID(), "C-1", 1, 5, AttentionType.CAJA);
        when(ticketService.createTicket(any(CreateTicketRequest.class))).thenReturn(response);

        // When/Then
        mockMvc.perform(post("/api/tickets")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testGetTicketStatus() throws Exception {
        // Given
        TicketStatusResponse response = new TicketStatusResponse("C-1", TicketStatus.EN_ESPERA, 1, 5, null, null);
        when(ticketService.getTicketStatus("C-1")).thenReturn(response);

        // When/Then
        mockMvc.perform(get("/api/tickets/C-1/position"))
                .andExpect(status().isOk());
    }

    @Test
    void testGetTicketStatusByCodigoReferencia() throws Exception {
        // Given
        UUID codigoReferencia = UUID.randomUUID();
        TicketStatusResponse response = new TicketStatusResponse("C-1", TicketStatus.EN_ESPERA, 1, 5, null, null);
        when(ticketService.getTicketStatusByCodigoReferencia(codigoReferencia)).thenReturn(response);

        // When/Then
        mockMvc.perform(get("/api/tickets/" + codigoReferencia))
                .andExpect(status().isOk());
    }
}
