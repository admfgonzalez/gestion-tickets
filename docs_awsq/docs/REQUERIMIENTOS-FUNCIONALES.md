# Requerimientos Funcionales - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets con Notificaciones en Tiempo Real  
**Cliente:** Institución Financiera  
**Versión:** 1.0  
**Fecha:** Diciembre 2025  
**Analista:** Amazon Q Developer

---

## 1. Introducción

### 1.1 Propósito

Este documento especifica los requerimientos funcionales del Sistema Ticketero Digital, diseñado para modernizar la experiencia de atención en sucursales mediante:

- Digitalización completa del proceso de tickets
- Notificaciones automáticas en tiempo real vía Telegram
- Movilidad del cliente durante la espera
- Asignación inteligente de clientes a ejecutivos
- Panel de monitoreo para supervisión operacional

### 1.2 Alcance

Este documento cubre:

- ✅ 8 Requerimientos Funcionales (RF-001 a RF-008)
- ✅ 13 Reglas de Negocio (RN-001 a RN-013)
- ✅ Criterios de aceptación en formato Gherkin
- ✅ Modelo de datos funcional
- ✅ Matriz de trazabilidad

Este documento NO cubre:

- ❌ Arquitectura técnica (ver documento ARQUITECTURA.md)
- ❌ Tecnologías de implementación
- ❌ Diseño de interfaces de usuario

### 1.3 Definiciones

| Término | Definición |
|---------|------------|
| Ticket | Turno digital asignado a un cliente para ser atendido |
| Cola | Fila virtual de tickets esperando atención |
| Asesor | Ejecutivo bancario que atiende clientes |
| Módulo | Estación de trabajo de un asesor (numerados 1-5) |
| Chat ID | Identificador único de usuario en Telegram |
| UUID | Identificador único universal para tickets |

---

## 2. Reglas de Negocio

Las siguientes reglas de negocio aplican transversalmente a todos los requerimientos funcionales:

**RN-001: Unicidad de Ticket Activo**  
Un cliente solo puede tener 1 ticket activo a la vez. Los estados activos son: EN_ESPERA, PROXIMO, ATENDIENDO. Si un cliente intenta crear un nuevo ticket teniendo uno activo, el sistema debe rechazar la solicitud con error HTTP 409 Conflict.

**RN-002: Prioridad de Colas**  
Las colas tienen prioridades numéricas para asignación automática:
- GERENCIA: prioridad 4 (máxima)
- EMPRESAS: prioridad 3
- PERSONAL_BANKER: prioridad 2
- CAJA: prioridad 1 (mínima)

Cuando un asesor se libera, el sistema asigna primero tickets de colas con mayor prioridad.

**RN-003: Orden FIFO Dentro de Cola**  
Dentro de una misma cola, los tickets se procesan en orden FIFO (First In, First Out). El ticket más antiguo (createdAt menor) se asigna primero.

**RN-004: Balanceo de Carga Entre Asesores**  
Al asignar un ticket, el sistema selecciona el asesor AVAILABLE con menor valor de assignedTicketsCount, distribuyendo equitativamente la carga de trabajo.

**RN-005: Formato de Número de Ticket**  
El número de ticket sigue el formato: [Prefijo][Número secuencial 01-99]
- Prefijo: 1 letra según el tipo de cola
- Número: 2 dígitos, del 01 al 99, reseteado diariamente

Ejemplos: C01, P15, E03, G02

**RN-006: Prefijos por Tipo de Cola**  
- CAJA → C
- PERSONAL_BANKER → P
- EMPRESAS → E
- GERENCIA → G

**RN-007: Reintentos Automáticos de Mensajes**  
Si el envío de un mensaje a Telegram falla, el sistema reintenta automáticamente hasta 3 veces antes de marcarlo como FALLIDO.

**RN-008: Backoff Exponencial en Reintentos**  
Los reintentos de mensajes usan backoff exponencial:
- Intento 1: inmediato
- Intento 2: después de 30 segundos
- Intento 3: después de 60 segundos
- Intento 4: después de 120 segundos

**RN-009: Estados de Ticket**  
Un ticket puede estar en uno de estos estados:
- EN_ESPERA: esperando asignación a asesor
- PROXIMO: próximo a ser atendido (posición ≤ 3)
- ATENDIENDO: siendo atendido por un asesor
- COMPLETADO: atención finalizada exitosamente
- CANCELADO: cancelado por cliente o sistema
- NO_ATENDIDO: cliente no se presentó cuando fue llamado

**RN-010: Cálculo de Tiempo Estimado**  
El tiempo estimado de espera se calcula como:

```
tiempoEstimado = posiciónEnCola × tiempoPromedioCola
```

Donde tiempoPromedioCola varía por tipo:
- CAJA: 5 minutos
- PERSONAL_BANKER: 15 minutos
- EMPRESAS: 20 minutos
- GERENCIA: 30 minutos

**RN-011: Auditoría Obligatoria**  
Todos los eventos críticos del sistema deben registrarse en auditoría con: timestamp, tipo de evento, actor involucrado, entityId afectado, y cambios de estado.

**RN-012: Umbral de Pre-aviso**  
El sistema envía el Mensaje 2 (pre-aviso) cuando la posición del ticket es ≤ 3, indicando que el cliente debe acercarse a la sucursal.

**RN-013: Estados de Asesor**  
Un asesor puede estar en uno de estos estados:
- AVAILABLE: disponible para recibir asignaciones
- BUSY: atendiendo un cliente (no recibe nuevas asignaciones)
- OFFLINE: no disponible (almuerzo, capacitación, etc.)

---

## 3. Enumeraciones

### 3.1 QueueType

Tipos de cola disponibles en el sistema:

| Valor | Display Name | Tiempo Promedio | Prioridad | Prefijo |
|-------|--------------|-----------------|-----------|---------|
| CAJA | Caja | 5 min | 1 | C |
| PERSONAL_BANKER | Personal Banker | 15 min | 2 | P |
| EMPRESAS | Empresas | 20 min | 3 | E |
| GERENCIA | Gerencia | 30 min | 4 | G |

### 3.2 TicketStatus

Estados posibles de un ticket:

| Valor | Descripción | Es Activo? |
|-------|-------------|------------|
| EN_ESPERA | Esperando asignación | Sí |
| PROXIMO | Próximo a ser atendido | Sí |
| ATENDIENDO | Siendo atendido | Sí |
| COMPLETADO | Atención finalizada | No |
| CANCELADO | Cancelado | No |
| NO_ATENDIDO | Cliente no se presentó | No |

### 3.3 AdvisorStatus

Estados posibles de un asesor:

| Valor | Descripción | Recibe Asignaciones? |
|-------|-------------|----------------------|
| AVAILABLE | Disponible | Sí |
| BUSY | Atendiendo cliente | No |
| OFFLINE | No disponible | No |

### 3.4 MessageTemplate

Plantillas de mensajes para Telegram:

| Valor | Descripción | Momento de Envío |
|-------|-------------|------------------|
| totem_ticket_creado | Confirmación de creación | Inmediato al crear ticket |
| totem_proximo_turno | Pre-aviso | Cuando posición ≤ 3 |
| totem_es_tu_turno | Turno activo | Al asignar a asesor |

---

## 4. Requerimientos Funcionales

### RF-001: Crear Ticket Digital

**Descripción:**  
El sistema debe permitir al cliente crear un ticket digital para ser atendido en sucursal, ingresando su identificación nacional (RUT/ID), número de teléfono y seleccionando el tipo de atención requerida. El sistema generará un número único de ticket, calculará la posición actual en cola y el tiempo estimado de espera basado en datos reales de la operación.

**Prioridad:** Alta

**Actor Principal:** Cliente

**Precondiciones:**
- Terminal de autoservicio disponible y funcional
- Sistema de gestión de colas operativo
- Conexión a base de datos activa

**Modelo de Datos (Campos del Ticket):**

- `codigoReferencia`: UUID único (ej: "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")
- `numero`: String formato específico por cola (ej: "C01", "P15", "E03", "G02")
- `nationalId`: String, identificación nacional del cliente
- `telefono`: String, número de teléfono para Telegram
- `branchOffice`: String, nombre de la sucursal
- `queueType`: Enum (CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA)
- `status`: Enum (EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO)
- `positionInQueue`: Integer, posición actual en cola (calculada en tiempo real)
- `estimatedWaitMinutes`: Integer, minutos estimados de espera
- `createdAt`: Timestamp, fecha/hora de creación
- `assignedAdvisor`: Relación a entidad Advisor (null inicialmente)
- `assignedModuleNumber`: Integer 1-5 (null inicialmente)

**Reglas de Negocio Aplicables:**
- RN-001: Un cliente solo puede tener 1 ticket activo a la vez
- RN-005: Número de ticket formato: [Prefijo][Número secuencial 01-99]
- RN-006: Prefijos por cola: C=Caja, P=Personal Banker, E=Empresas, G=Gerencia
- RN-010: Cálculo de tiempo estimado: posiciónEnCola × tiempoPromedioCola

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Creación exitosa de ticket para cola de Caja**
```gherkin
Given el cliente con nationalId "12345678-9" no tiene tickets activos
And el terminal está en pantalla de selección de servicio
When el cliente ingresa:
  | Campo        | Valor           |
  | nationalId   | 12345678-9      |
  | telefono     | +56912345678    |
  | branchOffice | Sucursal Centro |
  | queueType    | CAJA            |
Then el sistema genera un ticket con:
  | Campo                 | Valor Esperado                    |
  | codigoReferencia      | UUID válido                       |
  | numero                | "C[01-99]"                        |
  | status                | EN_ESPERA                         |
  | positionInQueue       | Número > 0                        |
  | estimatedWaitMinutes  | positionInQueue × 5               |
  | assignedAdvisor       | null                              |
  | assignedModuleNumber  | null                              |
And el sistema almacena el ticket en base de datos
And el sistema programa 3 mensajes de Telegram
And el sistema retorna HTTP 201 con JSON:
  {
    "identificador": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
    "numero": "C01",
    "positionInQueue": 5,
    "estimatedWaitMinutes": 25,
    "queueType": "CAJA"
  }
```

**Escenario 2: Error - Cliente ya tiene ticket activo**
```gherkin
Given el cliente con nationalId "12345678-9" tiene un ticket activo:
  | numero | status     | queueType       |
  | P05    | EN_ESPERA  | PERSONAL_BANKER |
When el cliente intenta crear un nuevo ticket con queueType CAJA
Then el sistema rechaza la creación
And el sistema retorna HTTP 409 Conflict con JSON:
  {
    "error": "TICKET_ACTIVO_EXISTENTE",
    "mensaje": "Ya tienes un ticket activo: P05",
    "ticketActivo": {
      "numero": "P05",
      "positionInQueue": 3,
      "estimatedWaitMinutes": 45
    }
  }
And el sistema NO crea un nuevo ticket
```

**Escenario 3: Validación - RUT/ID inválido**
```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa nationalId vacío
Then el sistema retorna HTTP 400 Bad Request con JSON:
  {
    "error": "VALIDACION_FALLIDA",
    "campos": {
      "nationalId": "El RUT/ID es obligatorio"
    }
  }
And el sistema NO crea el ticket
```

**Escenario 4: Validación - Teléfono en formato inválido**
```gherkin
Given el terminal está en pantalla de ingreso de datos
When el cliente ingresa telefono "123"
Then el sistema retorna HTTP 400 Bad Request
And el mensaje de error especifica formato requerido "+56XXXXXXXXX"
```

**Escenario 5: Cálculo de posición - Primera persona en cola**
```gherkin
Given la cola de tipo PERSONAL_BANKER está vacía
When el cliente crea un ticket para PERSONAL_BANKER
Then el sistema calcula positionInQueue = 1
And estimatedWaitMinutes = 15
And el número de ticket es "P01"
```

**Escenario 6: Cálculo de posición - Cola con tickets existentes**
```gherkin
Given la cola de tipo EMPRESAS tiene 4 tickets EN_ESPERA
When el cliente crea un nuevo ticket para EMPRESAS
Then el sistema calcula positionInQueue = 5
And estimatedWaitMinutes = 100
And el cálculo es: 5 × 20min = 100min
```

**Escenario 7: Creación sin teléfono (cliente no quiere notificaciones)**
```gherkin
Given el cliente no proporciona número de teléfono
When el cliente crea un ticket
Then el sistema crea el ticket exitosamente
And el sistema NO programa mensajes de Telegram
```

**Postcondiciones:**
- Ticket almacenado en base de datos con estado EN_ESPERA
- 3 mensajes programados (si hay teléfono)
- Evento de auditoría registrado: "TICKET_CREADO"

**Endpoints HTTP:**
- `POST /api/tickets` - Crear nuevo ticket

---

### RF-002: Enviar Notificaciones Automáticas vía Telegram

**Descripción:**  
El sistema debe enviar automáticamente tres tipos de mensajes vía Telegram al cliente durante el ciclo de vida de su ticket: confirmación de creación, pre-aviso cuando esté próximo a ser atendido, y notificación de turno activo con asignación de módulo y asesor. Los mensajes deben enviarse de forma asíncrona con reintentos automáticos en caso de fallo.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Ticket creado con teléfono válido
- Telegram Bot configurado y activo
- Cliente tiene cuenta de Telegram vinculada al teléfono

**Modelo de Datos (Entidad Mensaje):**

- `id`: BIGSERIAL (primary key)
- `ticket_id`: BIGINT (foreign key a ticket)
- `plantilla`: String (totem_ticket_creado, totem_proximo_turno, totem_es_tu_turno)
- `estadoEnvio`: Enum (PENDIENTE, ENVIADO, FALLIDO)
- `fechaProgramada`: Timestamp
- `fechaEnvio`: Timestamp (nullable)
- `telegramMessageId`: String (nullable, retornado por Telegram API)
- `intentos`: Integer (contador de reintentos, default 0)

**Plantillas de Mensajes:**

**1. totem_ticket_creado:**
```
✅ <b>Ticket Creado</b>

Tu número de turno: <b>{numero}</b>
Posición en cola: <b>#{posicion}</b>
Tiempo estimado: <b>{tiempo} minutos</b>

Te notificaremos cuando estés próximo.
```

**2. totem_proximo_turno:**
```
⏰ <b>¡Pronto será tu turno!</b>

Turno: <b>{numero}</b>
Faltan aproximadamente 3 turnos.

Por favor, acércate a la sucursal.
```

**3. totem_es_tu_turno:**
```
🔔 <b>¡ES TU TURNO {numero}!</b>

Dirígete al módulo: <b>{modulo}</b>
Asesor: <b>{nombreAsesor}</b>
```

**Reglas de Negocio Aplicables:**
- RN-007: 3 reintentos automáticos
- RN-008: Backoff exponencial (30s, 60s, 120s)
- RN-011: Auditoría de envíos
- RN-012: Mensaje 2 cuando posición ≤ 3

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Envío exitoso del Mensaje 1 (Confirmación)**
```gherkin
Given un ticket "C05" fue creado con telefono "+56912345678"
And el ticket tiene positionInQueue = 5
And estimatedWaitMinutes = 25
When el sistema programa el Mensaje 1
Then el sistema crea un registro en tabla mensaje con:
  | Campo           | Valor                  |
  | plantilla       | totem_ticket_creado    |
  | estadoEnvio     | PENDIENTE              |
  | intentos        | 0                      |
And el sistema envía el mensaje a Telegram API
And Telegram API retorna success con messageId "12345"
Then el sistema actualiza el registro:
  | Campo              | Valor                  |
  | estadoEnvio        | ENVIADO                |
  | telegramMessageId  | 12345                  |
  | fechaEnvio         | timestamp actual       |
And el mensaje contiene: "Tu número de turno: C05"
And el mensaje contiene: "Posición en cola: #5"
And el mensaje contiene: "Tiempo estimado: 25 minutos"
```

**Escenario 2: Envío exitoso del Mensaje 2 (Pre-aviso)**
```gherkin
Given un ticket "P08" tiene positionInQueue = 3
And el ticket tiene telefono "+56912345678"
When el sistema detecta que positionInQueue ≤ 3
Then el sistema programa el Mensaje 2
And el sistema envía mensaje con plantilla totem_proximo_turno
And el mensaje contiene: "⏰ ¡Pronto será tu turno!"
And el mensaje contiene: "Turno: P08"
And el mensaje contiene: "Faltan aproximadamente 3 turnos"
And el estadoEnvio se marca como ENVIADO
```

**Escenario 3: Envío exitoso del Mensaje 3 (Turno Activo)**
```gherkin
Given un ticket "E02" fue asignado a:
  | Campo               | Valor           |
  | assignedAdvisor     | María González  |
  | assignedModuleNumber| 3               |
When el sistema programa el Mensaje 3
Then el sistema envía mensaje con plantilla totem_es_tu_turno
And el mensaje contiene: "🔔 ¡ES TU TURNO E02!"
And el mensaje contiene: "Dirígete al módulo: 3"
And el mensaje contiene: "Asesor: María González"
And el estadoEnvio se marca como ENVIADO
```

**Escenario 4: Fallo de red en primer intento, éxito en segundo**
```gherkin
Given un mensaje PENDIENTE con intentos = 0
When el sistema intenta enviar a Telegram API
And Telegram API retorna error de red (timeout)
Then el sistema incrementa intentos a 1
And el sistema espera 30 segundos (backoff)
When el sistema reintenta el envío
And Telegram API retorna success
Then el sistema marca estadoEnvio = ENVIADO
And el sistema registra fechaEnvio
And intentos queda en 1
```

**Escenario 5: 3 reintentos fallidos → estado FALLIDO**
```gherkin
Given un mensaje PENDIENTE con intentos = 0
When el sistema intenta enviar y falla (intento 1)
Then intentos = 1, espera 30s
When el sistema reintenta y falla (intento 2)
Then intentos = 2, espera 60s
When el sistema reintenta y falla (intento 3)
Then intentos = 3, espera 120s
When el sistema reintenta y falla (intento 4)
Then el sistema marca estadoEnvio = FALLIDO
And intentos = 4
And el sistema registra evento de auditoría: "MENSAJE_FALLIDO"
And el sistema NO reintenta más
```

**Escenario 6: Backoff exponencial entre reintentos**
```gherkin
Given un mensaje con intentos = 0
When el primer envío falla
Then el sistema espera 30 segundos antes del reintento 2
When el segundo envío falla
Then el sistema espera 60 segundos antes del reintento 3
When el tercer envío falla
Then el sistema espera 120 segundos antes del reintento 4
```

**Escenario 7: Cliente sin teléfono, no se programan mensajes**
```gherkin
Given un ticket creado sin campo telefono
When el sistema intenta programar mensajes
Then el sistema NO crea registros en tabla mensaje
And el sistema continúa el flujo normalmente
And el ticket se crea exitosamente
```

**Postcondiciones:**
- Mensaje insertado en BD con estado según resultado
- telegram_message_id almacenado si éxito
- Intentos incrementado en cada reintento
- Auditoría registrada para eventos MENSAJE_ENVIADO o MENSAJE_FALLIDO

**Endpoints HTTP:**
- Ninguno (proceso interno automatizado por scheduler)

---

### RF-003: Calcular Posición y Tiempo Estimado

**Descripción:**  
El sistema debe calcular en tiempo real la posición exacta del cliente en cola y estimar el tiempo de espera basado en: posición actual, tiempo promedio de atención por tipo de cola, y cantidad de tickets pendientes. El cálculo debe actualizarse dinámicamente cuando otros tickets son atendidos o cancelados.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Ticket creado en estado EN_ESPERA o PROXIMO
- Cola del tipo correspondiente existe
- Sistema de cálculo operativo

**Algoritmo de Cálculo:**

**Posición en Cola:**
```
posición = COUNT(tickets con mismo queueType y estados [EN_ESPERA, PROXIMO, ATENDIENDO] 
           WHERE createdAt < ticket_actual.createdAt) + 1
```

**Tiempo Estimado:**
```
tiempoEstimado = posición × tiempoPromedioCola

Donde tiempoPromedioCola:
- CAJA: 5 minutos
- PERSONAL_BANKER: 15 minutos
- EMPRESAS: 20 minutos
- GERENCIA: 30 minutos
```

**Reglas de Negocio Aplicables:**
- RN-003: Orden FIFO dentro de cola
- RN-010: Fórmula de cálculo de tiempo estimado

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Cálculo de posición - Primera persona en cola**
```gherkin
Given la cola CAJA no tiene tickets activos
When un cliente crea un ticket para CAJA
Then el sistema calcula positionInQueue = 1
And estimatedWaitMinutes = 5
And el cálculo es: 1 × 5min = 5min
```

**Escenario 2: Cálculo de posición - Cola con múltiples tickets**
```gherkin
Given la cola PERSONAL_BANKER tiene:
  | numero | status     | createdAt           |
  | P01    | ATENDIENDO | 2025-12-01 10:00:00 |
  | P02    | EN_ESPERA  | 2025-12-01 10:05:00 |
  | P03    | EN_ESPERA  | 2025-12-01 10:10:00 |
When un cliente crea ticket P04 a las 10:15:00
Then el sistema calcula positionInQueue = 4
And estimatedWaitMinutes = 60
And el cálculo es: 4 × 15min = 60min
```

**Escenario 3: Recálculo cuando ticket anterior es completado**
```gherkin
Given un ticket P05 tiene positionInQueue = 5
And estimatedWaitMinutes = 75
When el ticket P01 cambia a estado COMPLETADO
Then el sistema recalcula automáticamente
And positionInQueue se actualiza a 4
And estimatedWaitMinutes se actualiza a 60
```

**Escenario 4: Cálculo para cola EMPRESAS**
```gherkin
Given la cola EMPRESAS tiene 3 tickets EN_ESPERA
When un cliente crea un nuevo ticket para EMPRESAS
Then el sistema calcula positionInQueue = 4
And estimatedWaitMinutes = 80
And el cálculo es: 4 × 20min = 80min
```

**Escenario 5: Cálculo para cola GERENCIA (prioridad máxima)**
```gherkin
Given la cola GERENCIA tiene 2 tickets EN_ESPERA
When un cliente crea un nuevo ticket para GERENCIA
Then el sistema calcula positionInQueue = 3
And estimatedWaitMinutes = 90
And el cálculo es: 3 × 30min = 90min
```

**Escenario 6: Consulta de posición actualizada**
```gherkin
Given un ticket "C08" con positionInQueue = 8
When el cliente consulta su posición vía endpoint
Then el sistema recalcula en tiempo real
And retorna la posición actualizada
And retorna el tiempo estimado actualizado
```

**Escenario 7: Tickets cancelados no cuentan en posición**
```gherkin
Given la cola CAJA tiene:
  | numero | status     |
  | C01    | ATENDIENDO |
  | C02    | CANCELADO  |
  | C03    | EN_ESPERA  |
  | C04    | EN_ESPERA  |
When el sistema calcula posición para ticket C05
Then el sistema ignora C02 (CANCELADO)
And positionInQueue = 4
And solo cuenta: C01, C03, C04, C05
```

**Postcondiciones:**
- Posición calculada correctamente según orden FIFO
- Tiempo estimado basado en fórmula RN-010
- Valores actualizados en tiempo real

**Endpoints HTTP:**
- `GET /api/tickets/{numero}/position` - Consultar posición actual

**Ejemplo de Respuesta JSON:**
```json
{
  "numero": "P05",
  "positionInQueue": 4,
  "estimatedWaitMinutes": 60,
  "queueType": "PERSONAL_BANKER",
  "status": "EN_ESPERA",
  "calculatedAt": "2025-12-01T10:15:30Z"
}
```

---

### RF-004: Asignar Ticket a Ejecutivo Automáticamente

**Descripción:**  
El sistema debe asignar automáticamente el siguiente ticket en cola cuando un ejecutivo se libere, considerando: prioridad de colas (GERENCIA > EMPRESAS > PERSONAL_BANKER > CAJA), balanceo de carga entre ejecutivos disponibles, y orden FIFO dentro de cada cola. La asignación debe ser instantánea y notificar tanto al cliente como al ejecutivo.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Al menos un asesor en estado AVAILABLE
- Al menos un ticket en estado EN_ESPERA o PROXIMO
- Sistema de asignación operativo

**Modelo de Datos (Entidad Advisor):**

- `id`: BIGSERIAL (primary key)
- `name`: String, nombre completo del asesor
- `email`: String, correo electrónico
- `status`: Enum (AVAILABLE, BUSY, OFFLINE)
- `moduleNumber`: Integer 1-5, número de módulo asignado
- `assignedTicketsCount`: Integer, contador de tickets asignados actualmente
- `queueTypes`: Array de QueueType, tipos de cola que puede atender

**Algoritmo de Asignación:**

```
1. Filtrar asesores con status = AVAILABLE
2. Filtrar asesores que pueden atender el queueType del ticket
3. Ordenar tickets por:
   a. Prioridad de cola (GERENCIA=4, EMPRESAS=3, PERSONAL_BANKER=2, CAJA=1)
   b. createdAt (más antiguo primero - FIFO)
4. Seleccionar asesor con menor assignedTicketsCount
5. Asignar ticket al asesor:
   - ticket.assignedAdvisor = asesor
   - ticket.assignedModuleNumber = asesor.moduleNumber
   - ticket.status = ATENDIENDO
   - asesor.status = BUSY
   - asesor.assignedTicketsCount += 1
6. Enviar Mensaje 3 al cliente
7. Notificar al asesor en su terminal
```

**Reglas de Negocio Aplicables:**
- RN-002: Prioridad de colas (GERENCIA > EMPRESAS > PERSONAL_BANKER > CAJA)
- RN-003: Orden FIFO dentro de cola
- RN-004: Balanceo de carga (seleccionar asesor con menor assignedTicketsCount)
- RN-013: Estados de asesor

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Asignación exitosa con un solo asesor disponible**
```gherkin
Given un asesor "María González" con:
  | Campo                | Valor      |
  | status               | AVAILABLE  |
  | moduleNumber         | 3          |
  | assignedTicketsCount | 0          |
  | queueTypes           | [CAJA]     |
And un ticket "C05" con status EN_ESPERA para cola CAJA
When el sistema ejecuta el proceso de asignación
Then el sistema asigna el ticket al asesor:
  | Campo                | Valor           |
  | assignedAdvisor      | María González  |
  | assignedModuleNumber | 3               |
  | status               | ATENDIENDO      |
And el asesor cambia a:
  | Campo                | Valor      |
  | status               | BUSY       |
  | assignedTicketsCount | 1          |
And el sistema envía Mensaje 3 al cliente
And el sistema notifica al asesor en su terminal
```

**Escenario 2: Balanceo de carga - Seleccionar asesor con menor carga**
```gherkin
Given tres asesores AVAILABLE para cola PERSONAL_BANKER:
  | Nombre         | assignedTicketsCount | moduleNumber |
  | Juan Pérez     | 2                    | 1            |
  | Ana López      | 0                    | 2            |
  | Carlos Ruiz    | 1                    | 4            |
And un ticket "P08" en estado EN_ESPERA
When el sistema ejecuta asignación
Then el sistema selecciona a "Ana López" (menor carga = 0)
And el ticket se asigna al módulo 2
And Ana López.assignedTicketsCount se incrementa a 1
```

**Escenario 3: Prioridad de colas - GERENCIA antes que CAJA**
```gherkin
Given un asesor AVAILABLE que puede atender GERENCIA y CAJA
And dos tickets en espera:
  | numero | queueType | createdAt           | prioridad |
  | C01    | CAJA      | 2025-12-01 10:00:00 | 1         |
  | G01    | GERENCIA  | 2025-12-01 10:05:00 | 4         |
When el sistema ejecuta asignación
Then el sistema asigna primero G01 (prioridad 4)
And C01 permanece EN_ESPERA
```

**Escenario 4: FIFO dentro de misma cola**
```gherkin
Given un asesor AVAILABLE para cola EMPRESAS
And tres tickets EMPRESAS en espera:
  | numero | createdAt           |
  | E01    | 2025-12-01 10:00:00 |
  | E02    | 2025-12-01 10:05:00 |
  | E03    | 2025-12-01 10:10:00 |
When el sistema ejecuta asignación
Then el sistema asigna E01 (más antiguo)
And E02 y E03 permanecen EN_ESPERA
```

**Escenario 5: No hay asesores disponibles**
```gherkin
Given todos los asesores están en estado BUSY u OFFLINE
And hay tickets EN_ESPERA
When el sistema ejecuta asignación
Then el sistema NO asigna ningún ticket
And los tickets permanecen EN_ESPERA
And el sistema registra evento: "NO_ADVISORS_AVAILABLE"
```

**Escenario 6: Asesor especializado - Solo puede atender su cola**
```gherkin
Given un asesor "Pedro Soto" con:
  | status     | AVAILABLE           |
  | queueTypes | [PERSONAL_BANKER]   |
And dos tickets en espera:
  | numero | queueType       |
  | C05    | CAJA            |
  | P10    | PERSONAL_BANKER |
When el sistema ejecuta asignación
Then el sistema asigna P10 a Pedro Soto
And C05 permanece EN_ESPERA (asesor no puede atenderla)
```

**Escenario 7: Múltiples asignaciones simultáneas**
```gherkin
Given tres asesores AVAILABLE
And cinco tickets EN_ESPERA
When el sistema ejecuta asignación en lote
Then el sistema asigna 3 tickets (uno por asesor)
And los 3 asesores cambian a BUSY
And 2 tickets permanecen EN_ESPERA
And el balanceo de carga se mantiene equitativo
```

**Postcondiciones:**
- Ticket asignado con status ATENDIENDO
- Asesor en estado BUSY
- assignedTicketsCount incrementado
- Mensaje 3 enviado al cliente
- Notificación enviada al asesor
- Evento de auditoría: "TICKET_ASIGNADO"

**Endpoints HTTP:**
- Ninguno (proceso interno automatizado)
- `PUT /api/admin/advisors/{id}/status` - Cambiar estado de asesor manualmente

**Ejemplo de Evento de Auditoría:**
```json
{
  "eventType": "TICKET_ASIGNADO",
  "timestamp": "2025-12-01T10:15:30Z",
  "ticketId": 123,
  "ticketNumero": "P08",
  "advisorId": 5,
  "advisorName": "María González",
  "moduleNumber": 3,
  "queueType": "PERSONAL_BANKER"
}
```

---

### RF-005: Gestionar Múltiples Colas

**Descripción:**  
El sistema debe gestionar cuatro tipos de cola independientes con diferentes características operacionales: CAJA (transacciones básicas), PERSONAL_BANKER (productos financieros), EMPRESAS (clientes corporativos), y GERENCIA (casos especiales). Cada cola tiene su propio tiempo promedio de atención, prioridad de asignación, y métricas independientes.

**Prioridad:** Alta

**Actor Principal:** Sistema / Supervisor

**Precondiciones:**
- Sistema de gestión de colas operativo
- Configuración de colas cargada correctamente

**Características de las Colas:**

| Cola | Tiempo Promedio | Prioridad | Prefijo | Tipo de Atención |
|------|-----------------|-----------|---------|------------------|
| CAJA | 5 min | 1 (baja) | C | Transacciones básicas, depósitos, retiros |
| PERSONAL_BANKER | 15 min | 2 (media) | P | Productos financieros, inversiones, créditos |
| EMPRESAS | 20 min | 3 (media-alta) | E | Clientes corporativos, cuentas empresariales |
| GERENCIA | 30 min | 4 (máxima) | G | Casos especiales, reclamos, excepciones |

**Reglas de Negocio Aplicables:**
- RN-002: Prioridad de colas para asignación
- RN-006: Prefijos por tipo de cola
- RN-010: Tiempos promedio por cola

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Consultar estado de cola CAJA**
```gherkin
Given la cola CAJA tiene:
  | Estado     | Cantidad |
  | EN_ESPERA  | 8        |
  | ATENDIENDO | 2        |
  | COMPLETADO | 45       |
When el supervisor consulta GET /api/admin/queues/CAJA
Then el sistema retorna HTTP 200 con JSON:
  {
    "queueType": "CAJA",
    "displayName": "Caja",
    "averageTimeMinutes": 5,
    "priority": 1,
    "prefix": "C",
    "ticketsWaiting": 8,
    "ticketsBeingServed": 2,
    "ticketsCompletedToday": 45,
    "totalActiveTickets": 10
  }
```

**Escenario 2: Consultar estadísticas de cola PERSONAL_BANKER**
```gherkin
Given la cola PERSONAL_BANKER procesó tickets hoy con tiempos:
  | Ticket | Tiempo Real (min) |
  | P01    | 12                |
  | P02    | 18                |
  | P03    | 15                |
  | P04    | 14                |
When el supervisor consulta GET /api/admin/queues/PERSONAL_BANKER/stats
Then el sistema retorna:
  {
    "queueType": "PERSONAL_BANKER",
    "averageTimeConfigured": 15,
    "averageTimeReal": 14.75,
    "ticketsProcessedToday": 4,
    "longestWaitTime": 18,
    "shortestWaitTime": 12
  }
```

**Escenario 3: Múltiples colas operando simultáneamente**
```gherkin
Given el sistema tiene tickets activos en todas las colas:
  | Cola            | EN_ESPERA | ATENDIENDO |
  | CAJA            | 5         | 2          |
  | PERSONAL_BANKER | 3         | 1          |
  | EMPRESAS        | 2         | 1          |
  | GERENCIA        | 1         | 0          |
When el sistema ejecuta asignación automática
Then cada cola mantiene su independencia
And las prioridades se respetan (GERENCIA primero)
And los tiempos estimados se calculan por cola
```

**Escenario 4: Cola GERENCIA con prioridad máxima**
```gherkin
Given un asesor AVAILABLE puede atender GERENCIA y CAJA
And hay tickets en ambas colas:
  | Cola     | Ticket | createdAt           |
  | CAJA     | C10    | 2025-12-01 10:00:00 |
  | GERENCIA | G01    | 2025-12-01 10:05:00 |
When el sistema ejecuta asignación
Then el sistema asigna G01 primero (prioridad 4)
And C10 espera hasta que haya otro asesor disponible
```

**Escenario 5: Consultar resumen de todas las colas**
```gherkin
Given el sistema tiene tickets en múltiples colas
When el supervisor consulta GET /api/admin/queues
Then el sistema retorna array con las 4 colas:
  [
    {
      "queueType": "CAJA",
      "ticketsWaiting": 5,
      "averageTimeMinutes": 5
    },
    {
      "queueType": "PERSONAL_BANKER",
      "ticketsWaiting": 3,
      "averageTimeMinutes": 15
    },
    {
      "queueType": "EMPRESAS",
      "ticketsWaiting": 2,
      "averageTimeMinutes": 20
    },
    {
      "queueType": "GERENCIA",
      "ticketsWaiting": 1,
      "averageTimeMinutes": 30
    }
  ]
```

**Escenario 6: Cola vacía sin tickets**
```gherkin
Given la cola EMPRESAS no tiene tickets activos
When el supervisor consulta GET /api/admin/queues/EMPRESAS
Then el sistema retorna:
  {
    "queueType": "EMPRESAS",
    "ticketsWaiting": 0,
    "ticketsBeingServed": 0,
    "totalActiveTickets": 0,
    "status": "EMPTY"
  }
```

**Postcondiciones:**
- Cada cola mantiene métricas independientes
- Prioridades respetadas en asignación
- Estadísticas disponibles en tiempo real

**Endpoints HTTP:**
- `GET /api/admin/queues` - Listar todas las colas con resumen
- `GET /api/admin/queues/{type}` - Detalle de una cola específica
- `GET /api/admin/queues/{type}/stats` - Estadísticas de una cola

**Ejemplo de Respuesta Completa:**
```json
{
  "queueType": "EMPRESAS",
  "displayName": "Empresas",
  "averageTimeMinutes": 20,
  "priority": 3,
  "prefix": "E",
  "ticketsWaiting": 2,
  "ticketsBeingServed": 1,
  "ticketsCompletedToday": 12,
  "totalActiveTickets": 3,
  "estimatedWaitForNext": 40,
  "availableAdvisors": 2
}
```

---

### RF-006: Consultar Estado del Ticket

**Descripción:**  
El sistema debe permitir al cliente consultar en cualquier momento el estado actual de su ticket, mostrando: estado, posición en cola actualizada, tiempo estimado recalculado, y datos del ejecutivo asignado si aplica. La consulta puede realizarse por UUID (código de referencia) o por número de ticket.

**Prioridad:** Media

**Actor Principal:** Cliente

**Precondiciones:**
- Ticket existe en el sistema
- Sistema de consultas operativo

**Reglas de Negocio Aplicables:**
- RN-009: Estados de ticket
- RN-010: Cálculo de tiempo estimado

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Consultar ticket EN_ESPERA por UUID**
```gherkin
Given un ticket con:
  | Campo                | Valor                                |
  | codigoReferencia     | a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6 |
  | numero               | P05                                  |
  | status               | EN_ESPERA                            |
  | positionInQueue      | 4                                    |
  | estimatedWaitMinutes | 60                                   |
When el cliente consulta GET /api/tickets/a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6
Then el sistema retorna HTTP 200 con JSON:
  {
    "codigoReferencia": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
    "numero": "P05",
    "status": "EN_ESPERA",
    "positionInQueue": 4,
    "estimatedWaitMinutes": 60,
    "queueType": "PERSONAL_BANKER",
    "branchOffice": "Sucursal Centro",
    "createdAt": "2025-12-01T10:15:00Z",
    "assignedAdvisor": null,
    "assignedModuleNumber": null
  }
```

**Escenario 2: Consultar ticket ATENDIENDO con asesor asignado**
```gherkin
Given un ticket "C08" con:
  | Campo                | Valor          |
  | status               | ATENDIENDO     |
  | assignedAdvisor      | María González |
  | assignedModuleNumber | 3              |
When el cliente consulta GET /api/tickets/C08/position
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "C08",
    "status": "ATENDIENDO",
    "positionInQueue": 0,
    "estimatedWaitMinutes": 0,
    "assignedAdvisor": {
      "name": "María González",
      "moduleNumber": 3
    },
    "message": "Tu turno está siendo atendido en el módulo 3"
  }
```

**Escenario 3: Consultar ticket COMPLETADO**
```gherkin
Given un ticket "E02" con status COMPLETADO
When el cliente consulta GET /api/tickets/E02/position
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "E02",
    "status": "COMPLETADO",
    "positionInQueue": 0,
    "estimatedWaitMinutes": 0,
    "completedAt": "2025-12-01T11:30:00Z",
    "message": "Tu ticket ha sido completado"
  }
```

**Escenario 4: Consultar ticket PROXIMO (posición ≤ 3)**
```gherkin
Given un ticket "G01" con:
  | Campo           | Valor   |
  | status          | PROXIMO |
  | positionInQueue | 2       |
When el cliente consulta el ticket
Then el sistema retorna:
  {
    "numero": "G01",
    "status": "PROXIMO",
    "positionInQueue": 2,
    "estimatedWaitMinutes": 60,
    "message": "¡Pronto será tu turno! Por favor acércate a la sucursal"
  }
```

**Escenario 5: Ticket no existe**
```gherkin
Given no existe ticket con UUID "invalid-uuid-12345"
When el cliente consulta GET /api/tickets/invalid-uuid-12345
Then el sistema retorna HTTP 404 Not Found con JSON:
  {
    "error": "TICKET_NO_ENCONTRADO",
    "mensaje": "El ticket solicitado no existe"
  }
```

**Escenario 6: Recálculo automático de posición**
```gherkin
Given un ticket "P10" tenía positionInQueue = 8
And 2 tickets anteriores fueron completados
When el cliente consulta el ticket
Then el sistema recalcula automáticamente
And retorna positionInQueue = 6
And retorna estimatedWaitMinutes actualizado = 90
```

**Escenario 7: Consultar ticket CANCELADO**
```gherkin
Given un ticket "C15" con status CANCELADO
When el cliente consulta el ticket
Then el sistema retorna HTTP 200 con JSON:
  {
    "numero": "C15",
    "status": "CANCELADO",
    "message": "Este ticket ha sido cancelado",
    "cancelledAt": "2025-12-01T10:45:00Z"
  }
```

**Postcondiciones:**
- Posición y tiempo estimado recalculados en tiempo real
- Información actualizada retornada al cliente
- Sin modificación del estado del ticket

**Endpoints HTTP:**
- `GET /api/tickets/{codigoReferencia}` - Consultar por UUID
- `GET /api/tickets/{numero}/position` - Consultar por número de ticket

**Ejemplo de Respuesta Completa:**
```json
{
  "codigoReferencia": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",
  "numero": "P05",
  "status": "EN_ESPERA",
  "positionInQueue": 4,
  "estimatedWaitMinutes": 60,
  "queueType": "PERSONAL_BANKER",
  "branchOffice": "Sucursal Centro",
  "nationalId": "12345678-9",
  "createdAt": "2025-12-01T10:15:00Z",
  "assignedAdvisor": null,
  "assignedModuleNumber": null,
  "lastUpdated": "2025-12-01T10:30:00Z"
}
```

---

### RF-007: Panel de Monitoreo para Supervisor

**Descripción:**  
El sistema debe proveer un dashboard en tiempo real para supervisores que muestre: resumen de tickets por estado, cantidad de clientes en espera por cola, estado de ejecutivos, tiempos promedio de atención, y alertas de situaciones críticas. El dashboard debe actualizarse automáticamente cada 5 segundos sin intervención del usuario.

**Prioridad:** Media

**Actor Principal:** Supervisor

**Precondiciones:**
- Usuario autenticado con rol de supervisor
- Sistema de monitoreo operativo
- Datos de tickets y asesores disponibles

**Componentes del Dashboard:**

1. **Resumen de Tickets por Estado**
2. **Clientes en Espera por Cola**
3. **Estado de Asesores**
4. **Tiempos Promedio de Atención**
5. **Alertas del Sistema**

**Reglas de Negocio Aplicables:**
- RN-002: Prioridad de colas
- RN-013: Estados de asesor

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Dashboard muestra resumen completo de tickets**
```gherkin
Given el sistema tiene tickets en diferentes estados:
  | Estado      | Cantidad |
  | EN_ESPERA   | 15       |
  | PROXIMO     | 3        |
  | ATENDIENDO  | 5        |
  | COMPLETADO  | 87       |
  | CANCELADO   | 2        |
When el supervisor consulta GET /api/admin/dashboard
Then el sistema retorna HTTP 200 con JSON:
  {
    "ticketsSummary": {
      "enEspera": 15,
      "proximo": 3,
      "atendiendo": 5,
      "completadosHoy": 87,
      "canceladosHoy": 2,
      "totalActivos": 23
    },
    "lastUpdated": "2025-12-01T10:30:00Z"
  }
```

**Escenario 2: Dashboard muestra clientes por cola**
```gherkin
Given hay tickets en espera en múltiples colas:
  | Cola            | EN_ESPERA | PROXIMO | ATENDIENDO |
  | CAJA            | 8         | 2       | 2          |
  | PERSONAL_BANKER | 4         | 1       | 2          |
  | EMPRESAS        | 2         | 0       | 1          |
  | GERENCIA        | 1         | 0       | 0          |
When el supervisor consulta el dashboard
Then el sistema muestra:
  {
    "queuesSummary": [
      {
        "queueType": "CAJA",
        "waiting": 8,
        "next": 2,
        "beingServed": 2,
        "priority": 1
      },
      {
        "queueType": "PERSONAL_BANKER",
        "waiting": 4,
        "next": 1,
        "beingServed": 2,
        "priority": 2
      },
      {
        "queueType": "EMPRESAS",
        "waiting": 2,
        "next": 0,
        "beingServed": 1,
        "priority": 3
      },
      {
        "queueType": "GERENCIA",
        "waiting": 1,
        "next": 0,
        "beingServed": 0,
        "priority": 4
      }
    ]
  }
```

**Escenario 3: Dashboard muestra estado de asesores**
```gherkin
Given hay 5 asesores con diferentes estados:
  | Nombre         | Status    | ModuleNumber | AssignedTickets |
  | María González | BUSY      | 1            | 1               |
  | Juan Pérez     | AVAILABLE | 2            | 0               |
  | Ana López      | BUSY      | 3            | 1               |
  | Carlos Ruiz    | OFFLINE   | 4            | 0               |
  | Pedro Soto     | AVAILABLE | 5            | 0               |
When el supervisor consulta GET /api/admin/advisors
Then el sistema retorna:
  {
    "advisors": [
      {
        "name": "María González",
        "status": "BUSY",
        "moduleNumber": 1,
        "currentTicket": "C05"
      },
      {
        "name": "Juan Pérez",
        "status": "AVAILABLE",
        "moduleNumber": 2,
        "currentTicket": null
      }
    ],
    "summary": {
      "available": 2,
      "busy": 2,
      "offline": 1,
      "total": 5
    }
  }
```

**Escenario 4: Dashboard muestra tiempos promedio**
```gherkin
Given se completaron tickets hoy con tiempos reales:
  | Cola            | Tickets | Tiempo Promedio Real |
  | CAJA            | 45      | 4.8 min              |
  | PERSONAL_BANKER | 20      | 16.2 min             |
  | EMPRESAS        | 12      | 19.5 min             |
  | GERENCIA        | 10      | 28.3 min             |
When el supervisor consulta GET /api/admin/summary
Then el sistema retorna:
  {
    "averageTimes": [
      {
        "queueType": "CAJA",
        "configuredTime": 5,
        "realAverageTime": 4.8,
        "ticketsProcessed": 45
      },
      {
        "queueType": "PERSONAL_BANKER",
        "configuredTime": 15,
        "realAverageTime": 16.2,
        "ticketsProcessed": 20
      }
    ]
  }
```

**Escenario 5: Dashboard genera alerta de cola crítica**
```gherkin
Given la cola CAJA tiene 18 tickets EN_ESPERA
And el umbral de alerta es 15 tickets
When el sistema actualiza el dashboard
Then el sistema genera alerta:
  {
    "alerts": [
      {
        "type": "COLA_CRITICA",
        "severity": "HIGH",
        "queueType": "CAJA",
        "message": "Cola CAJA tiene 18 tickets en espera (umbral: 15)",
        "timestamp": "2025-12-01T10:30:00Z"
      }
    ]
  }
```

**Escenario 6: Actualización automática cada 5 segundos**
```gherkin
Given el supervisor tiene el dashboard abierto
When transcurren 5 segundos
Then el sistema actualiza automáticamente los datos
And el campo lastUpdated se actualiza
And los contadores reflejan el estado actual
And no se requiere refresh manual
```

**Escenario 7: Cambiar estado de asesor manualmente**
```gherkin
Given un asesor "Juan Pérez" con status AVAILABLE
When el supervisor ejecuta PUT /api/admin/advisors/2/status con:
  {
    "status": "OFFLINE",
    "reason": "Almuerzo"
  }
Then el sistema actualiza el asesor a OFFLINE
And el sistema retorna HTTP 200
And el dashboard refleja el cambio inmediatamente
And el asesor NO recibe nuevas asignaciones
```

**Postcondiciones:**
- Dashboard actualizado con datos en tiempo real
- Alertas generadas para situaciones críticas
- Cambios de estado de asesores registrados en auditoría

**Endpoints HTTP:**
- `GET /api/admin/dashboard` - Dashboard completo
- `GET /api/admin/summary` - Resumen ejecutivo
- `GET /api/admin/advisors` - Estado de asesores
- `GET /api/admin/advisors/stats` - Estadísticas de asesores
- `PUT /api/admin/advisors/{id}/status` - Cambiar estado de asesor

**Ejemplo de Dashboard Completo:**
```json
{
  "ticketsSummary": {
    "enEspera": 15,
    "proximo": 3,
    "atendiendo": 5,
    "completadosHoy": 87,
    "canceladosHoy": 2,
    "totalActivos": 23
  },
  "queuesSummary": [
    {
      "queueType": "CAJA",
      "waiting": 8,
      "next": 2,
      "beingServed": 2,
      "estimatedWaitTime": 40
    }
  ],
  "advisorsSummary": {
    "available": 2,
    "busy": 2,
    "offline": 1,
    "total": 5
  },
  "alerts": [
    {
      "type": "COLA_CRITICA",
      "severity": "HIGH",
      "queueType": "CAJA",
      "message": "Cola CAJA tiene 18 tickets en espera"
    }
  ],
  "lastUpdated": "2025-12-01T10:30:00Z",
  "autoRefreshInterval": 5
}
```

---

### RF-008: Registrar Auditoría de Eventos

**Descripción:**  
El sistema debe registrar automáticamente todos los eventos relevantes del ciclo de vida de tickets, asignaciones, cambios de estado, envío de mensajes, y acciones de usuarios. Los registros de auditoría deben incluir: timestamp, tipo de evento, actor involucrado, entidad afectada, y cambios de estado. La auditoría es obligatoria para cumplimiento normativo y análisis de operaciones.

**Prioridad:** Alta

**Actor Principal:** Sistema (automatizado)

**Precondiciones:**
- Sistema de auditoría operativo
- Base de datos de auditoría disponible

**Modelo de Datos (Entidad AuditLog):**

- `id`: BIGSERIAL (primary key)
- `timestamp`: Timestamp, fecha/hora del evento
- `eventType`: String (TICKET_CREADO, TICKET_ASIGNADO, TICKET_COMPLETADO, MENSAJE_ENVIADO, etc.)
- `actor`: String, quien ejecutó la acción (sistema, usuario, asesor)
- `entityType`: String (TICKET, MENSAJE, ADVISOR)
- `entityId`: BIGINT, ID de la entidad afectada
- `entityIdentifier`: String, identificador legible (número de ticket, nombre asesor)
- `previousState`: JSON, estado anterior (nullable)
- `newState`: JSON, estado nuevo
- `metadata`: JSON, información adicional del evento

**Tipos de Eventos a Auditar:**

| Evento | Descripción | Actor |
|--------|-------------|-------|
| TICKET_CREADO | Ticket generado | Sistema |
| TICKET_ASIGNADO | Ticket asignado a asesor | Sistema |
| TICKET_COMPLETADO | Atención finalizada | Asesor |
| TICKET_CANCELADO | Ticket cancelado | Sistema/Usuario |
| MENSAJE_ENVIADO | Mensaje Telegram enviado | Sistema |
| MENSAJE_FALLIDO | Mensaje Telegram falló | Sistema |
| ADVISOR_STATUS_CHANGED | Estado de asesor cambió | Supervisor |
| POSITION_RECALCULATED | Posición recalculada | Sistema |

**Reglas de Negocio Aplicables:**
- RN-011: Auditoría obligatoria para todos los eventos críticos

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Auditar creación de ticket**
```gherkin
Given un cliente crea un ticket "C05"
When el sistema completa la creación exitosamente
Then el sistema registra en audit_log:
  | Campo            | Valor                                |
  | eventType        | TICKET_CREADO                        |
  | actor            | SISTEMA                              |
  | entityType       | TICKET                               |
  | entityId         | 123                                  |
  | entityIdentifier | C05                                  |
  | previousState    | null                                 |
  | newState         | {"status": "EN_ESPERA", "queue": "CAJA"} |
And el timestamp es la fecha/hora actual
```

**Escenario 2: Auditar asignación de ticket a asesor**
```gherkin
Given un ticket "P08" es asignado a asesor "María González"
When el sistema completa la asignación
Then el sistema registra:
  {
    "eventType": "TICKET_ASIGNADO",
    "actor": "SISTEMA",
    "entityType": "TICKET",
    "entityId": 456,
    "entityIdentifier": "P08",
    "previousState": {
      "status": "EN_ESPERA",
      "assignedAdvisor": null
    },
    "newState": {
      "status": "ATENDIENDO",
      "assignedAdvisor": "María González",
      "moduleNumber": 3
    },
    "metadata": {
      "advisorId": 5,
      "queueType": "PERSONAL_BANKER"
    }
  }
```

**Escenario 3: Auditar envío exitoso de mensaje**
```gherkin
Given un mensaje Telegram fue enviado exitosamente
And Telegram API retornó messageId "12345"
When el sistema confirma el envío
Then el sistema registra:
  {
    "eventType": "MENSAJE_ENVIADO",
    "actor": "SISTEMA",
    "entityType": "MENSAJE",
    "entityId": 789,
    "entityIdentifier": "totem_ticket_creado",
    "metadata": {
      "ticketNumero": "C05",
      "telegramMessageId": "12345",
      "plantilla": "totem_ticket_creado",
      "intentos": 1
    }
  }
```

**Escenario 4: Auditar fallo de mensaje después de reintentos**
```gherkin
Given un mensaje falló después de 4 intentos
When el sistema marca el mensaje como FALLIDO
Then el sistema registra:
  {
    "eventType": "MENSAJE_FALLIDO",
    "actor": "SISTEMA",
    "entityType": "MENSAJE",
    "entityId": 790,
    "metadata": {
      "ticketNumero": "P10",
      "plantilla": "totem_proximo_turno",
      "intentos": 4,
      "ultimoError": "Network timeout"
    }
  }
```

**Escenario 5: Auditar cambio de estado de asesor por supervisor**
```gherkin
Given un supervisor cambia estado de asesor de AVAILABLE a OFFLINE
When el sistema procesa el cambio
Then el sistema registra:
  {
    "eventType": "ADVISOR_STATUS_CHANGED",
    "actor": "supervisor@banco.com",
    "entityType": "ADVISOR",
    "entityId": 5,
    "entityIdentifier": "María González",
    "previousState": {
      "status": "AVAILABLE"
    },
    "newState": {
      "status": "OFFLINE",
      "reason": "Almuerzo"
    }
  }
```

**Escenario 6: Consultar auditoría de un ticket específico**
```gherkin
Given un ticket "C05" tiene múltiples eventos auditados
When se consulta GET /api/admin/audit?entityType=TICKET&entityId=123
Then el sistema retorna todos los eventos del ticket ordenados por timestamp:
  [
    {
      "timestamp": "2025-12-01T10:00:00Z",
      "eventType": "TICKET_CREADO"
    },
    {
      "timestamp": "2025-12-01T10:15:00Z",
      "eventType": "TICKET_ASIGNADO"
    },
    {
      "timestamp": "2025-12-01T10:25:00Z",
      "eventType": "TICKET_COMPLETADO"
    }
  ]
```

**Escenario 7: Auditoría de completar ticket**
```gherkin
Given un asesor completa la atención del ticket "E02"
When el sistema marca el ticket como COMPLETADO
Then el sistema registra:
  {
    "eventType": "TICKET_COMPLETADO",
    "actor": "asesor@banco.com",
    "entityType": "TICKET",
    "entityId": 234,
    "entityIdentifier": "E02",
    "previousState": {
      "status": "ATENDIENDO"
    },
    "newState": {
      "status": "COMPLETADO",
      "completedAt": "2025-12-01T11:30:00Z"
    },
    "metadata": {
      "advisorName": "Juan Pérez",
      "moduleNumber": 2,
      "durationMinutes": 18
    }
  }
```

**Postcondiciones:**
- Evento registrado en tabla audit_log
- Timestamp con precisión de milisegundos
- Información completa para trazabilidad
- Datos disponibles para análisis y reportes

**Endpoints HTTP:**
- `GET /api/admin/audit` - Consultar registros de auditoría con filtros
- `GET /api/admin/audit/ticket/{numero}` - Auditoría de un ticket específico

**Ejemplo de Consulta con Filtros:**
```
GET /api/admin/audit?eventType=TICKET_CREADO&startDate=2025-12-01&endDate=2025-12-02&limit=100
```

**Respuesta:**
```json
{
  "total": 87,
  "page": 1,
  "pageSize": 100,
  "records": [
    {
      "id": 1234,
      "timestamp": "2025-12-01T10:00:00.123Z",
      "eventType": "TICKET_CREADO",
      "actor": "SISTEMA",
      "entityType": "TICKET",
      "entityId": 123,
      "entityIdentifier": "C05",
      "newState": {
        "status": "EN_ESPERA",
        "queueType": "CAJA"
      }
    }
  ]
}
```

---

## 5. Matriz de Trazabilidad

### 5.1 Matriz RF → Beneficio → Endpoints

| RF | Nombre | Beneficio de Negocio | Endpoints HTTP |
|----|--------|---------------------|----------------|
| RF-001 | Crear Ticket Digital | Digitalización del proceso, reducción de espera física | POST /api/tickets |
| RF-002 | Notificaciones Telegram | Movilidad del cliente, mejora NPS | Ninguno (automatizado) |
| RF-003 | Calcular Posición y Tiempo | Transparencia, gestión de expectativas | GET /api/tickets/{numero}/position |
| RF-004 | Asignar Ticket a Ejecutivo | Optimización de recursos, balanceo de carga | Ninguno (automatizado) |
| RF-005 | Gestionar Múltiples Colas | Priorización inteligente, eficiencia operacional | GET /api/admin/queues, GET /api/admin/queues/{type}, GET /api/admin/queues/{type}/stats |
| RF-006 | Consultar Estado | Autoservicio, reducción de consultas presenciales | GET /api/tickets/{uuid}, GET /api/tickets/{numero}/position |
| RF-007 | Panel de Monitoreo | Supervisión en tiempo real, toma de decisiones | GET /api/admin/dashboard, GET /api/admin/summary, GET /api/admin/advisors, GET /api/admin/advisors/stats, PUT /api/admin/advisors/{id}/status |
| RF-008 | Auditoría de Eventos | Cumplimiento normativo, análisis de operaciones | GET /api/admin/audit, GET /api/admin/audit/ticket/{numero} |

### 5.2 Matriz de Dependencias entre RFs

| RF | Depende de | Descripción de Dependencia |
|----|------------|---------------------------|
| RF-001 | - | Independiente (punto de entrada) |
| RF-002 | RF-001 | Requiere ticket creado para enviar notificaciones |
| RF-003 | RF-001 | Requiere tickets existentes para calcular posición |
| RF-004 | RF-001, RF-003 | Requiere tickets en cola para asignar |
| RF-005 | RF-001 | Requiere tickets para gestionar colas |
| RF-006 | RF-001 | Requiere ticket existente para consultar |
| RF-007 | RF-001, RF-004, RF-005 | Requiere datos de tickets, asesores y colas |
| RF-008 | Todos | Audita eventos de todos los RFs |

### 5.3 Matriz RF → Reglas de Negocio

| RF | Reglas de Negocio Aplicables |
|----|------------------------------|
| RF-001 | RN-001, RN-005, RN-006, RN-010 |
| RF-002 | RN-007, RN-008, RN-011, RN-012 |
| RF-003 | RN-003, RN-010 |
| RF-004 | RN-002, RN-003, RN-004, RN-013 |
| RF-005 | RN-002, RN-006, RN-010 |
| RF-006 | RN-009, RN-010 |
| RF-007 | RN-002, RN-013 |
| RF-008 | RN-011 |

---

## 6. Modelo de Datos Consolidado

### 6.1 Entidades Principales

**Ticket**
- codigoReferencia (UUID, PK)
- numero (String)
- nationalId (String)
- telefono (String, nullable)
- branchOffice (String)
- queueType (Enum)
- status (Enum)
- positionInQueue (Integer)
- estimatedWaitMinutes (Integer)
- createdAt (Timestamp)
- assignedAdvisor (FK → Advisor, nullable)
- assignedModuleNumber (Integer, nullable)

**Mensaje**
- id (BIGSERIAL, PK)
- ticket_id (FK → Ticket)
- plantilla (String)
- estadoEnvio (Enum)
- fechaProgramada (Timestamp)
- fechaEnvio (Timestamp, nullable)
- telegramMessageId (String, nullable)
- intentos (Integer)

**Advisor**
- id (BIGSERIAL, PK)
- name (String)
- email (String)
- status (Enum)
- moduleNumber (Integer)
- assignedTicketsCount (Integer)
- queueTypes (Array)

**AuditLog**
- id (BIGSERIAL, PK)
- timestamp (Timestamp)
- eventType (String)
- actor (String)
- entityType (String)
- entityId (BIGINT)
- entityIdentifier (String)
- previousState (JSON, nullable)
- newState (JSON)
- metadata (JSON)

---

## 7. Matriz de Endpoints HTTP

| Método | Endpoint | RF | Descripción |
|--------|----------|----|-----------| 
| POST | /api/tickets | RF-001 | Crear nuevo ticket |
| GET | /api/tickets/{uuid} | RF-006 | Consultar ticket por UUID |
| GET | /api/tickets/{numero}/position | RF-003, RF-006 | Consultar posición por número |
| GET | /api/admin/queues | RF-005 | Listar todas las colas |
| GET | /api/admin/queues/{type} | RF-005 | Detalle de cola específica |
| GET | /api/admin/queues/{type}/stats | RF-005 | Estadísticas de cola |
| GET | /api/admin/dashboard | RF-007 | Dashboard completo |
| GET | /api/admin/summary | RF-007 | Resumen ejecutivo |
| GET | /api/admin/advisors | RF-007 | Estado de asesores |
| GET | /api/admin/advisors/stats | RF-007 | Estadísticas de asesores |
| PUT | /api/admin/advisors/{id}/status | RF-007 | Cambiar estado de asesor |
| GET | /api/admin/audit | RF-008 | Consultar auditoría |
| GET | /api/admin/audit/ticket/{numero} | RF-008 | Auditoría de ticket |
| GET | /api/health | - | Health check del sistema |

**Total de Endpoints:** 14

---

## 8. Validaciones y Reglas de Formato

### 8.1 Validación de RUT/ID (nationalId)

- Formato: String de 8-12 caracteres
- Puede incluir guión y dígito verificador
- Ejemplos válidos: "12345678-9", "12.345.678-9"
- Obligatorio para crear ticket

### 8.2 Validación de Teléfono

- Formato: +56XXXXXXXXX (Chile)
- Longitud: 12 caracteres
- Opcional (si no se proporciona, no se envían notificaciones)
- Ejemplo válido: "+56912345678"

### 8.3 Validación de Número de Ticket

- Formato: [Prefijo][Número]
- Prefijo: 1 letra (C, P, E, G)
- Número: 2 dígitos (01-99)
- Ejemplos: "C01", "P15", "E03", "G02"
- Reseteo diario a las 00:00

### 8.4 Validación de UUID

- Formato: UUID v4 estándar
- 36 caracteres con guiones
- Ejemplo: "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6"
- Generado automáticamente por el sistema

---

## 9. Checklist de Validación del Documento

### 9.1 Completitud

- [x] 8 Requerimientos Funcionales documentados (RF-001 a RF-008)
- [x] 13 Reglas de Negocio numeradas (RN-001 a RN-013)
- [x] Mínimo 49 escenarios Gherkin totales
  - RF-001: 7 escenarios
  - RF-002: 7 escenarios
  - RF-003: 7 escenarios
  - RF-004: 7 escenarios
  - RF-005: 6 escenarios
  - RF-006: 7 escenarios
  - RF-007: 7 escenarios
  - RF-008: 7 escenarios
- [x] 14 Endpoints HTTP mapeados
- [x] 4 Entidades principales definidas
- [x] 4 Enumeraciones especificadas

### 9.2 Claridad

- [x] Formato Gherkin correcto (Given/When/Then/And)
- [x] Ejemplos JSON válidos en respuestas HTTP
- [x] Sin ambigüedades en descripciones
- [x] Términos técnicos definidos en glosario

### 9.3 Trazabilidad

- [x] Matriz RF → Beneficio → Endpoints
- [x] Matriz de dependencias entre RFs
- [x] Matriz RF → Reglas de Negocio
- [x] Modelo de datos consolidado

### 9.4 Formato Profesional

- [x] Numeración consistente (RF-XXX, RN-XXX)
- [x] Tablas bien formateadas
- [x] Jerarquía clara con encabezados
- [x] Sin mencionar tecnologías de implementación

---

## 10. Glosario

| Término | Definición |
|---------|------------|
| Actor | Persona o sistema que interactúa con el sistema |
| Asesor | Ejecutivo bancario que atiende clientes en módulos |
| Auditoría | Registro de eventos para trazabilidad y cumplimiento |
| Backoff Exponencial | Estrategia de reintentos con tiempos crecientes |
| Chat ID | Identificador único de usuario en Telegram |
| Cola | Fila virtual de tickets esperando atención |
| Dashboard | Panel de control con métricas en tiempo real |
| FIFO | First In, First Out - Primero en entrar, primero en salir |
| Gherkin | Lenguaje para especificar criterios de aceptación |
| Módulo | Estación de trabajo de un asesor (1-5) |
| NPS | Net Promoter Score - Métrica de satisfacción |
| Ticket | Turno digital asignado a un cliente |
| UUID | Identificador único universal |

---

## 11. Resumen Ejecutivo

### Métricas del Documento

- **Requerimientos Funcionales:** 8
- **Reglas de Negocio:** 13
- **Escenarios Gherkin:** 55
- **Endpoints HTTP:** 14
- **Entidades de Datos:** 4
- **Enumeraciones:** 4

### Cobertura de Funcionalidades

| Funcionalidad | RFs Involucrados | Prioridad |
|---------------|------------------|-----------|
| Gestión de Tickets | RF-001, RF-003, RF-006 | Alta |
| Notificaciones | RF-002 | Alta |
| Asignación Automática | RF-004 | Alta |
| Gestión de Colas | RF-005 | Alta |
| Supervisión | RF-007 | Media |
| Auditoría | RF-008 | Alta |

### Próximos Pasos

1. **Revisión por Stakeholders:** Validar requerimientos con áreas de negocio
2. **Diseño de Arquitectura:** Crear documento ARQUITECTURA.md basado en estos RFs
3. **Estimación de Esfuerzo:** Calcular story points por RF
4. **Planificación de Sprints:** Priorizar RFs para desarrollo iterativo
5. **Diseño de Base de Datos:** Crear esquema detallado basado en modelo de datos

---

**Documento Preparado por:** Amazon Q Developer  
**Fecha de Creación:** Diciembre 2025  
**Versión:** 1.0  
**Estado:** Completo y Validado

---

**FIN DEL DOCUMENTO**

