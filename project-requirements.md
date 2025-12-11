# **Sistema de Gestión de Tickets para Atención en Sucursales**

**Proyecto:** Ticketero Digital con Notificaciones en Tiempo Real  
**Cliente:** Institución Financiera  
**Versión:** 1.0  
**Fecha:** Diciembre 2025

## **1\. Descripción del Proyecto**

### **1.1 Contexto**

Las instituciones financieras enfrentan desafíos en la atención presencial: los clientes no tienen visibilidad de tiempos de espera, deben permanecer físicamente en sucursal sin poder realizar otras actividades, y existe incertidumbre sobre el progreso de su turno.

### **1.2 Solución Propuesta**

Sistema digital de gestión de tickets que moderniza la experiencia de atención mediante:

* Digitalización del proceso de tickets  
* Notificaciones automáticas en tiempo real vía Telegram  
* Movilidad del cliente durante la espera  
* Asignación automática de clientes a ejecutivos disponibles  
* Panel de monitoreo para supervisión operacional

### **1.3 Beneficios Esperados**

* Mejora de NPS de 45 a 65 puntos  
* Reducción de abandonos de cola de 15% a 5%  
* Incremento de 20% en tickets atendidos por ejecutivo  
* Trazabilidad completa para análisis y mejora continua

## **2\. Reglas de Negocio**

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
El tiempo estimado de espera se calcula como: `tiempoEstimado = posiciónEnCola × tiempoPromedioCola`

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

## **3\. Requerimientos Funcionales**

### **RF-001: Crear Ticket Digital**

El sistema debe permitir al cliente crear un ticket digital para ser atendido en sucursal, ingresando su identificación nacional (RUT/ID), número de teléfono y seleccionando el tipo de atención requerida. El sistema generará un número único de ticket, calculará la posición actual en cola y el tiempo estimado de espera basado en datos reales de la operación.

### **RF-002: Enviar Notificaciones Automáticas vía Telegram**

El sistema debe enviar automáticamente tres tipos de mensajes vía Telegram al cliente durante el ciclo de vida de su ticket: confirmación de creación, pre-aviso cuando esté próximo a ser atendido, y notificación de turno activo con asignación de módulo y asesor. Los mensajes deben enviarse de forma asíncrona con reintentos automáticos en caso de fallo.

### **RF-003: Calcular Posición y Tiempo Estimado**

El sistema debe calcular en tiempo real la posición exacta del cliente en cola y estimar el tiempo de espera basado en: posición actual, tiempo promedio de atención por tipo de cola, y cantidad de tickets pendientes. El cálculo debe actualizarse dinámicamente cuando otros tickets son atendidos o cancelados.

### **RF-004: Asignar Ticket a Ejecutivo Automáticamente**

El sistema debe asignar automáticamente el siguiente ticket en cola cuando un ejecutivo se libere, considerando: prioridad de colas, balanceo de carga entre ejecutivos disponibles, y orden FIFO dentro de cada cola. La asignación debe ser instantánea y notificar tanto al cliente como al ejecutivo.

### **RF-005: Gestionar Múltiples Colas**

El sistema debe gestionar cuatro tipos de cola independientes con diferentes características operacionales: CAJA (transacciones básicas), PERSONAL_BANKER (productos financieros), EMPRESAS (clientes corporativos), y GERENCIA (casos especiales). Cada cola tiene su propio tiempo promedio de atención, prioridad de asignación, y métricas independientes.

### **RF-006: Consultar Estado del Ticket**

El sistema debe permitir al cliente consultar en cualquier momento el estado actual de su ticket, mostrando: estado, posición en cola actualizada, tiempo estimado recalculado, y datos del ejecutivo asignado si aplica. La consulta puede realizarse por UUID (código de referencia) o por número de ticket.

### **RF-007: Panel de Monitoreo para Supervisor**

El sistema debe proveer un dashboard en tiempo real para supervisores que muestre: resumen de tickets por estado, cantidad de clientes en espera por cola, estado de ejecutivos, tiempos promedio de atención, y alertas de situaciones críticas. El dashboard debe actualizarse automáticamente cada 5 segundos sin intervención del usuario.

### **RF-008: Registrar Auditoría de Eventos**

El sistema debe registrar automáticamente todos los eventos relevantes del ciclo de vida de tickets, asignaciones, cambios de estado, envío de mensajes, y acciones de usuarios. Los registros de auditoría deben incluir: timestamp, tipo de evento, actor involucrado, entidad afectada, y cambios de estado. La auditoría es obligatoria para cumplimiento normativo y análisis de operaciones.

## **4\. Modelo de Datos y Enumeraciones**

### **4.1 Entidades Principales**

*   **Ticket**: `codigoReferencia` (UUID, PK), `numero` (String), `nationalId` (String), `telefono` (String, nullable), `branchOffice` (String), `queueType` (Enum), `status` (Enum), `positionInQueue` (Integer), `estimatedWaitMinutes` (Integer), `createdAt` (Timestamp), `assignedAdvisor` (FK → Advisor, nullable), `assignedModuleNumber` (Integer, nullable)
*   **Mensaje**: `id` (BIGSERIAL, PK), `ticket_id` (FK → Ticket), `plantilla` (String), `estadoEnvio` (Enum), `fechaProgramada` (Timestamp), `fechaEnvio` (Timestamp, nullable), `telegramMessageId` (String, nullable), `intentos` (Integer)
*   **Advisor**: `id` (BIGSERIAL, PK), `name` (String), `email` (String), `status` (Enum), `moduleNumber` (Integer), `assignedTicketsCount` (Integer), `queueTypes` (Array)
*   **AuditLog**: `id` (BIGSERIAL, PK), `timestamp` (Timestamp), `eventType` (String), `actor` (String), `entityType` (String), `entityId` (BIGINT), `entityIdentifier` (String), `previousState` (JSON, nullable), `newState` (JSON), `metadata` (JSON)

### **4.2 Enumeraciones**

*   **QueueType**: CAJA, PERSONAL_BANKER, EMPRESAS, GERENCIA
*   **TicketStatus**: EN_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO_ATENDIDO
*   **AdvisorStatus**: AVAILABLE, BUSY, OFFLINE
*   **MessageTemplate**: totem_ticket_creado, totem_proximo_turno, totem_es_tu_turno

## **5\. Requerimientos No Funcionales**

### **RNF-001: Disponibilidad**

* Uptime de 99.5% durante horario de atención  
* Máximo 4 horas de downtime al mes  
* Recovery automático en menos de 5 minutos

### **RNF-002: Performance**

* Creación de ticket: menos de 3 segundos  
* Envío de Mensaje 1: menos de 5 segundos  
* Cálculo de posición: menos de 1 segundo  
* Actualización de dashboard: cada 5 segundos

### **RNF-003: Escalabilidad**

* Fase Piloto: 500-800 tickets/día, 1 sucursal  
* Fase Expansión: 2,500-3,000 tickets/día, 5 sucursales  
* Fase Nacional: 25,000+ tickets/día, 50+ sucursales

### **RNF-004: Confiabilidad**

* 99.9% de mensajes entregados exitosamente  
* Sin pérdida de datos ante fallos  
* 3 reintentos automáticos para envío de mensajes (30s, 60s, 120s)

### **RNF-005: Seguridad**

* Cumplimiento de ley de protección de datos personales  
* Encriptación de datos sensibles (teléfonos, RUT)  
* Acceso controlado al panel administrativo  
* Logs de auditoría de todos los accesos

### **RNF-006: Usabilidad**

* Cliente obtiene ticket en menos de 2 minutos  
* Interfaz intuitiva sin necesidad de capacitación  
* Mensajes claros en español simple  
* Dashboard comprensible a primera vista

### **RNF-007: Mantenibilidad**

* Código documentado  
* Arquitectura modular  
* Logs detallados para diagnóstico  
* Actualizaciones sin interrupción de servicio

---

**Preparado por:** Área de Producto e Innovación  
**Tipo:** Proyecto de Capacitación \- Ciclo Completo de Desarrollo de Software