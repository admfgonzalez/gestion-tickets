package com.institucion.ticketero.module_executives.api;

import com.institucion.ticketero.module_queues.domain.AttentionType;
import java.util.List;

public record UpdateAttentionTypesRequest(List<AttentionType> attentionTypes) {
}
