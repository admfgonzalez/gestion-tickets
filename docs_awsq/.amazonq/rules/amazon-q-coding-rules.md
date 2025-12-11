# Reglas de Codificación para Amazon Q - Sistema Ticketero

**Proyecto:** Sistema de Gestión de Tickets  
**Versión:** 1.0  
**Categoría:** Desarrollo - Estándares de Código

---

## 🎯 Reglas Core para Amazon Q

### Rule #1: Arquitectura Hexagonal Simplificada

**Estructura obligatoria:**
```
src/
├── controller/     # REST endpoints únicamente
├── service/        # Lógica de negocio
├── repository/     # Acceso a datos
├── model/          # Entidades del dominio
└── config/         # Configuraciones
```

**Prohibido:**
- Más de 4 capas de abstracción
- Patrones complejos (Factory, Builder, Strategy) en MVP
- Interfaces innecesarias para clases con una sola implementación

### Rule #2: Naming Convention Específico del Dominio

**Entidades del negocio:**
```java
// ✅ CORRECTO
class Ticket { }
class TipoAtencion { }
class EstadoTicket { }
class Ejecutivo { }

// ❌ INCORRECTO
class TicketEntity { }
class AttentionTypeEnum { }
class TicketStatusValue { }
```

**Servicios:**
```java
// ✅ CORRECTO
class TicketService { }
class TelegramNotificationService { }
class ColaCalculatorService { }

// ❌ INCORRECTO
class TicketBusinessLogic { }
class NotificationManager { }
class QueueProcessorImpl { }
```

### Rule #3: Métodos con Responsabilidad Única

**Límites cuantitativos:**
- Máximo 15 líneas por método
- Máximo 3 parámetros por método
- Un solo nivel de indentación en métodos de servicio

**Ejemplo correcto:**
```java
public TicketResponse crearTicket(String rut, TipoAtencion tipo) {
    validarRut(rut);
    Ticket ticket = generarTicket(rut, tipo);
    int posicion = calcularPosicion(tipo);
    int tiempoEstimado = calcularTiempo(posicion, tipo);
    
    repository.save(ticket);
    telegramService.enviarConfirmacion(ticket, posicion, tiempoEstimado);
    
    return new TicketResponse(ticket, posicion, tiempoEstimado);
}
```

### Rule #4: Manejo de Estados Explícito

**Estados permitidos para Ticket:**
```java
public enum EstadoTicket {
    CREADO,           // Ticket generado
    EN_ESPERA,        // En cola
    PRE_LLAMADO,      // 3 personas adelante
    ASIGNADO,         // Con ejecutivo
    COMPLETADO,       // Atendido
    CANCELADO         // No atendido
}
```

**Transiciones válidas:**
```java
// ✅ CORRECTO - Transiciones explícitas
CREADO → EN_ESPERA → PRE_LLAMADO → ASIGNADO → COMPLETADO
CREADO → EN_ESPERA → CANCELADO

// ❌ PROHIBIDO - Saltos de estado
CREADO → ASIGNADO
PRE_LLAMADO → COMPLETADO
```

### Rule #5: Configuración Centralizada

**Archivo único:** `application.yml`
```yaml
# ✅ CORRECTO - Valores específicos del negocio
ticketero:
  colas:
    caja:
      tiempo-promedio: 5
      prioridad: 1
    personal-banker:
      tiempo-promedio: 15
      prioridad: 2
  notificaciones:
    pre-aviso-posicion: 3
    reintentos: 3
    intervalos: [30, 60, 120]
```

**Prohibido:**
- Valores hardcodeados en código
- Múltiples archivos de configuración
- Configuración en base de datos para MVP

---

## 📋 Reglas de Implementación por Funcionalidad

### RF-001: Crear Ticket Digital

**Controller:**
```java
@PostMapping("/api/tickets")
public ResponseEntity<TicketResponse> crearTicket(@RequestBody TicketRequest request) {
    TicketResponse response = ticketService.crearTicket(request.getRut(), request.getTipo());
    return ResponseEntity.status(201).body(response);
}
```

**Validaciones obligatorias:**
- RUT formato chileno válido
- Tipo de atención existe
- Cliente no tiene ticket activo

### RF-002: Notificaciones Telegram

**Estructura de mensajes:**
```java
// ✅ CORRECTO - Templates simples
public class MensajeTemplate {
    public static final String CONFIRMACION = 
        "✅ Ticket #{numero}\nPosición: {posicion}\nTiempo estimado: {tiempo} min";
    
    public static final String PRE_AVISO = 
        "🔔 Ticket #{numero}\nQuedan 3 personas. Acércate a sucursal";
    
    public static final String ASIGNACION = 
        "👤 Tu turno\nMódulo: {modulo}\nEjecutivo: {ejecutivo}";
}
```

**Prohibido:**
- Mensajes dinámicos complejos
- HTML en mensajes
- Más de 3 tipos de mensaje

### RF-003: Cálculo de Posición

**Algoritmo simple:**
```java
public int calcularPosicion(TipoAtencion tipo) {
    return repository.countByTipoAndEstadoIn(tipo, 
        Arrays.asList(EN_ESPERA, PRE_LLAMADO, ASIGNADO));
}

public int calcularTiempoEstimado(int posicion, TipoAtencion tipo) {
    int tiempoPromedio = configuracion.getTiempoPromedio(tipo);
    int ejecutivosDisponibles = ejecutivoService.contarDisponibles(tipo);
    return (posicion * tiempoPromedio) / Math.max(ejecutivosDisponibles, 1);
}
```

### RF-007: Panel de Monitoreo

**DTO específico:**
```java
public class DashboardResponse {
    private Map<TipoAtencion, Integer> ticketsPorCola;
    private Map<String, EstadoEjecutivo> estadoEjecutivos;
    private Map<TipoAtencion, Double> tiemposPromedio;
    private List<String> alertas;
}
```

**Actualización:**
```java
@Scheduled(fixedRate = 5000) // 5 segundos
public void actualizarDashboard() {
    DashboardResponse data = dashboardService.generarResumen();
    websocketService.broadcast("/dashboard", data);
}
```

---

## 🚫 Anti-Patrones Prohibidos

### Código Prohibido #1: Over-Engineering
```java
// ❌ PROHIBIDO - Demasiada abstracción
interface TicketFactory {
    Ticket createTicket(TicketCreationStrategy strategy);
}

class TicketCreationStrategyFactory {
    public TicketCreationStrategy getStrategy(TipoAtencion tipo) { ... }
}
```

### Código Prohibido #2: Lógica en Controller
```java
// ❌ PROHIBIDO - Lógica de negocio en controller
@PostMapping("/tickets")
public ResponseEntity<?> crear(@RequestBody TicketRequest request) {
    if (request.getRut().length() < 8) return badRequest();
    Ticket ticket = new Ticket();
    ticket.setNumero(UUID.randomUUID().toString());
    // ... más lógica
}
```

### Código Prohibido #3: Queries Complejas
```java
// ❌ PROHIBIDO - Query compleja en repository
@Query("SELECT t FROM Ticket t JOIN t.ejecutivo e WHERE t.estado IN :estados " +
       "AND e.disponible = true AND t.tipoAtencion = :tipo " +
       "ORDER BY t.prioridad DESC, t.fechaCreacion ASC")
List<Ticket> findComplexQuery(@Param("estados") List<EstadoTicket> estados, 
                              @Param("tipo") TipoAtencion tipo);
```

---

## ✅ Checklist para Amazon Q

### Antes de generar código:
- [ ] ¿La clase tiene una sola responsabilidad?
- [ ] ¿El método tiene menos de 15 líneas?
- [ ] ¿Los nombres reflejan el dominio del negocio?
- [ ] ¿Evita abstracciones innecesarias?

### Para cada endpoint:
- [ ] ¿Valida entrada?
- [ ] ¿Delega a service?
- [ ] ¿Retorna DTO específico?
- [ ] ¿Maneja errores apropiadamente?

### Para cada service:
- [ ] ¿Métodos con responsabilidad única?
- [ ] ¿Transacciones explícitas?
- [ ] ¿Logging de eventos importantes?
- [ ] ¿Manejo de estados válido?

---

## 🎯 Ejemplos de Prompts Efectivos para Amazon Q

### ✅ Prompt Correcto:
```
"Crea el TicketService.crearTicket() que:
1. Valide RUT chileno
2. Genere ticket con número único
3. Calcule posición en cola
4. Guarde en repository
5. Envíe notificación Telegram
6. Retorne TicketResponse con posición y tiempo estimado
Máximo 15 líneas, sin abstracciones complejas"
```

### ❌ Prompt Incorrecto:
```
"Crea un sistema completo de tickets con patrones de diseño, 
manejo de excepciones avanzado, cache distribuido y 
arquitectura de microservicios"
```

---

## 📊 Métricas de Calidad

### Límites por Clase:
- Máximo 200 líneas por clase
- Máximo 10 métodos públicos
- Máximo 5 dependencias inyectadas

### Límites por Package:
- controller: Máximo 8 clases
- service: Máximo 6 clases  
- repository: Máximo 5 interfaces
- model: Máximo 10 clases

### Cobertura de Tests:
- Services: 80% mínimo
- Controllers: 70% mínimo
- Repositories: Tests de integración únicamente

---

## 🔄 Validación Continua

### En cada commit:
```bash
# Validaciones automáticas
□ Naming conventions
□ Límites de líneas por método
□ Dependencias circulares
□ Tests unitarios pasan
```

### En code review:
```bash
□ ¿Sigue arquitectura hexagonal simplificada?
□ ¿Nombres reflejan dominio del negocio?
□ ¿Evita over-engineering?
□ ¿Cumple reglas específicas por RF?
```

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2024  
**Estado:** Activa  
**Aplicable a:** Todas las interacciones con Amazon Q en este proyecto