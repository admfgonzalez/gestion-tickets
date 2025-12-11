# 🏗️ Propuesta Técnica - Sistema Ticketero Digital

**Proyecto:** Sistema de Gestión de Tickets Bancarios  
**Arquitecto:** Senior Java Developer  
**Stack:** Java 21 + Spring Boot 3.2 + PostgreSQL  
**Fecha:** Diciembre 2024

---

## 1. Decisiones Arquitectónicas

### 1.1 Arquitectura: Monolito Modular

**✅ DECISIÓN:** Monolito modular sobre microservicios

**Justificación:**
- **Simplicidad operacional**: Una aplicación, un deployment
- **Transacciones ACID**: Consistencia entre ticket-notificación-asignación
- **Latencia mínima**: RF-002 requiere <5s para mensajes
- **MVP rápido**: Time-to-market optimizado
- **Equipo pequeño**: Menos complejidad de coordinación

### 1.2 Stack Tecnológico

| Componente | Tecnología | Justificación |
|------------|------------|---------------|
| **Runtime** | Java 21 | Records, Pattern matching, Virtual threads |
| **Framework** | Spring Boot 3.2 | Ecosistema maduro, productividad |
| **Base de datos** | PostgreSQL 15 | ACID, JSON support, escalabilidad |
| **Build** | Gradle 8.5 | Flexibilidad, performance |
| **Migraciones** | Flyway | Simplicidad sobre Liquibase |
| **Cache** | Caffeine | In-memory, alta performance |
| **Mensajería** | Telegram Bot API | Requerimiento del negocio |
| **WebSockets** | Spring WebSocket | Dashboard tiempo real |

---

## 2. Módulos del Sistema

### 2.1 Módulos Core

```
src/main/java/com/banco/ticketero/
├── controller/          # REST endpoints
├── service/            # Lógica de negocio
├── repository/         # Acceso a datos
├── model/             # Entidades del dominio
└── config/            # Configuraciones
```

**🎫 Ticket Management Module**
- **Responsabilidad**: Ciclo de vida completo de tickets
- **Componentes**: TicketService, EstadoTicket, TicketRepository

**📱 Notification Module**
- **Responsabilidad**: Mensajes Telegram con reintentos
- **Componentes**: TelegramService, MensajeTemplate, NotificationScheduler

**⏱️ Queue Management Module**
- **Responsabilidad**: Cálculo posiciones y tiempos
- **Componentes**: ColaCalculatorService, TiempoEstimadoService

**👥 Executive Assignment Module**
- **Responsabilidad**: Asignación automática y balanceo
- **Componentes**: EjecutivoService, AsignacionService

**📊 Monitoring Module**
- **Responsabilidad**: Dashboard y métricas tiempo real
- **Componentes**: DashboardService, WebSocketController

---

## 3. Modelo de Dominio

### 3.1 Entidades Principales

```java
@Entity
public class Ticket {
    @Id @GeneratedValue
    private Long id;
    
    private String codigoReferencia;  // TK-20241201-001
    private String rutCliente;
    private TipoAtencion tipoAtencion;
    private EstadoTicket estado;
    private LocalDateTime fechaCreacion;
    private Integer posicionInicial;
    private Integer tiempoEstimadoMinutos;
    
    @ManyToOne
    private Ejecutivo ejecutivoAsignado;
}

@Entity
public class Ejecutivo {
    @Id @GeneratedValue
    private Long id;
    
    private String nombre;
    private String modulo;
    private Set<TipoAtencion> tiposAtencion;
    private EstadoEjecutivo estado;
    private LocalDateTime ultimaAsignacion;
}

@Entity
public class Mensaje {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Ticket ticket;
    
    private TipoMensaje tipo;
    private String contenido;
    private EstadoMensaje estado;
    private LocalDateTime fechaEnvio;
    private Integer intentos;
}
```

### 3.2 Enums del Dominio

```java
public enum EstadoTicket {
    CREADO,           // Ticket generado
    EN_ESPERA,        // En cola
    PRE_LLAMADO,      // 3 personas adelante
    ASIGNADO,         // Con ejecutivo
    COMPLETADO,       // Atendido
    CANCELADO         // No atendido
}

public enum TipoAtencion {
    CAJA(5, 1),              // 5 min promedio, prioridad baja
    PERSONAL_BANKER(15, 2),   // 15 min promedio, prioridad media
    EMPRESAS(20, 2),         // 20 min promedio, prioridad media
    GERENCIA(30, 3);         // 30 min promedio, prioridad alta
    
    private final int tiempoPromedioMinutos;
    private final int prioridad;
}

public enum TipoMensaje {
    CONFIRMACION,     // Mensaje 1: Ticket creado
    PRE_AVISO,        // Mensaje 2: Quedan 3 personas
    ASIGNACION        // Mensaje 3: Turno activo
}
```

### 3.3 DTOs de API

```java
public record TicketRequest(String rut, TipoAtencion tipo) {}

public record TicketResponse(
    String codigoReferencia,
    int posicion,
    int tiempoEstimadoMinutos,
    EstadoTicket estado,
    String ejecutivoAsignado,
    String modulo
) {}

public record DashboardResponse(
    Map<TipoAtencion, Integer> ticketsPorCola,
    Map<String, EstadoEjecutivo> estadoEjecutivos,
    Map<TipoAtencion, Double> tiemposPromedio,
    List<String> alertas
) {}
```

---

## 4. API Endpoints

### 4.1 Tickets API

```java
@RestController
@RequestMapping("/api/tickets")
public class TicketController {
    
    @PostMapping
    public ResponseEntity<TicketResponse> crearTicket(@RequestBody TicketRequest request) {}
    
    @GetMapping("/{codigo}")
    public ResponseEntity<TicketResponse> consultarTicket(@PathVariable String codigo) {}
    
    @PutMapping("/{codigo}/cancelar")
    public ResponseEntity<Void> cancelarTicket(@PathVariable String codigo) {}
    
    @PostMapping("/{codigo}/ubicacion")
    public ResponseEntity<TiempoRegresoResponse> actualizarUbicacion(
        @PathVariable String codigo,
        @RequestBody UbicacionRequest ubicacion) {}
}
```

### 4.2 Dashboard API

```java
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    @GetMapping("/resumen")
    public ResponseEntity<DashboardResponse> obtenerResumen() {}
    
    @GetMapping("/metricas")
    public ResponseEntity<MetricasResponse> obtenerMetricas() {}
}
```

### 4.3 WebSocket para Tiempo Real

```java
@Controller
public class WebSocketController {
    
    @MessageMapping("/dashboard")
    @SendTo("/topic/dashboard")
    public DashboardResponse actualizarDashboard() {}
}
```

---

## 5. Persistencia y Optimizaciones

### 5.1 Esquema de Base de Datos

Ver archivo: `docs/diagrams/database-schema.puml`

### 5.2 Índices Críticos

```sql
-- Consultas frecuentes optimizadas
CREATE INDEX idx_ticket_estado_tipo ON ticket(estado, tipo_atencion);
CREATE INDEX idx_ticket_fecha_creacion ON ticket(fecha_creacion);
CREATE INDEX idx_ejecutivo_estado ON ejecutivo(estado);
CREATE INDEX idx_mensaje_ticket_tipo ON mensaje(ticket_id, tipo);
```

### 5.3 Queries Optimizadas

```java
@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.tipoAtencion = :tipo " +
           "AND t.estado IN ('EN_ESPERA', 'PRE_LLAMADO', 'ASIGNADO')")
    int countActivosPorTipo(@Param("tipo") TipoAtencion tipo);
    
    @Query("SELECT t FROM Ticket t WHERE t.estado = 'EN_ESPERA' " +
           "AND t.tipoAtencion = :tipo ORDER BY t.fechaCreacion ASC")
    List<Ticket> findProximosEnCola(@Param("tipo") TipoAtencion tipo, Pageable pageable);
}
```

---

## 6. Integraciones Externas

### 6.1 Telegram Bot API

```java
@Service
public class TelegramService {
    
    @Value("${telegram.bot.token}")
    private String botToken;
    
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 30000, multiplier = 2))
    public void enviarMensaje(String chatId, String mensaje) {
        // RestTemplate o WebClient para Telegram API
    }
}
```

### 6.2 Templates de Mensajes

```java
public class MensajeTemplate {
    public static final String CONFIRMACION = 
        "✅ Ticket #{numero}\nPosición: {posicion}\nTiempo estimado: {tiempo} min";
    
    public static final String PRE_AVISO = 
        "🔔 Ticket #{numero}\nQuedan 3 personas. Acércate a sucursal";
    
    public static final String ASIGNACION = 
        "👤 Tu turno\nMódulo: {modulo}\nEjecutivo: {ejecutivo}";
}
```

---

## 7. Cache y Performance

### 7.1 Configuración de Cache

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .recordStats()
            .build();
    }
}
```

### 7.2 Servicios con Cache

```java
@Service
public class ColaCalculatorService {
    
    @Cacheable("posiciones")
    public int calcularPosicion(TipoAtencion tipo) {
        return ticketRepository.countActivosPorTipo(tipo);
    }
    
    @Cacheable("tiempos-estimados")
    public int calcularTiempoEstimado(int posicion, TipoAtencion tipo) {
        int tiempoPromedio = tipo.getTiempoPromedioMinutos();
        int ejecutivosDisponibles = ejecutivoService.contarDisponibles(tipo);
        return (posicion * tiempoPromedio) / Math.max(ejecutivosDisponibles, 1);
    }
}
```

---

## 8. Mejoras Propuestas

### 8.1 Geolocalización Inteligente

**Problema**: RF-003 no considera distancia del cliente a sucursal

**Solución**:
```java
public class TiempoEstimadoService {
    
    public int calcularTiempoConDistancia(int posicion, TipoAtencion tipo, 
                                        Double latCliente, Double lonCliente) {
        int tiempoBase = calcularTiempoBase(posicion, tipo);
        int tiempoTraslado = calcularTiempoTraslado(latCliente, lonCliente);
        return Math.max(tiempoBase - tiempoTraslado, 5); // Mínimo 5 min
    }
    
    private int calcularTiempoTraslado(Double lat, Double lon) {
        // Google Maps API o cálculo de distancia euclidiana
        // Retorna tiempo en minutos considerando tráfico
    }
}
```

### 8.2 Priorización Dinámica

**Mejora**: Algoritmo que considera tiempo de espera vs tiempo esperado

```java
public class PriorizacionService {
    
    public List<Ticket> ordenarColaPorPrioridad(TipoAtencion tipo) {
        return ticketRepository.findEnEsperaPorTipo(tipo)
            .stream()
            .sorted((t1, t2) -> {
                double factor1 = calcularFactorPrioridad(t1);
                double factor2 = calcularFactorPrioridad(t2);
                return Double.compare(factor2, factor1); // Descendente
            })
            .toList();
    }
    
    private double calcularFactorPrioridad(Ticket ticket) {
        long tiempoEspera = Duration.between(ticket.getFechaCreacion(), LocalDateTime.now()).toMinutes();
        int tiempoEsperado = ticket.getTipoAtencion().getTiempoPromedioMinutos();
        return (double) tiempoEspera / tiempoEsperado;
    }
}
```

---

## 9. Deployment y DevOps

### 9.1 Docker Compose (Desarrollo)

```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - TELEGRAM_BOT_TOKEN=${TELEGRAM_BOT_TOKEN}
    depends_on:
      - postgres
      
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: ticketero
      POSTGRES_USER: admin
      POSTGRES_PASSWORD: admin123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

### 9.2 Gradle Build

```gradle
plugins {
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.4'
    id 'java'
}

java {
    sourceCompatibility = '21'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-websocket'
    implementation 'org.springframework.boot:spring-boot-starter-cache'
    implementation 'org.springframework.boot:spring-boot-starter-actuator'
    implementation 'org.flywaydb:flyway-core'
    implementation 'com.github.ben-manes.caffeine:caffeine'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0'
    
    runtimeOnly 'org.postgresql:postgresql'
    
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.testcontainers:postgresql'
    testImplementation 'org.testcontainers:junit-jupiter'
}
```

---

## 10. Testing Strategy

### 10.1 Pirámide de Testing

- **Unit Tests**: Services (80% cobertura)
- **Integration Tests**: Repositories + Controllers (70% cobertura)
- **E2E Tests**: Flujos críticos (Happy path + error scenarios)

### 10.2 Testcontainers

```java
@SpringBootTest
@Testcontainers
class TicketServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("ticketero_test")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void debeCrearTicketYCalcularPosicion() {
        // Test de integración completo
    }
}
```

---

## 11. Monitoreo y Observabilidad

### 11.1 Métricas Personalizadas

```java
@Component
public class TicketMetrics {
    
    private final Counter ticketsCreados;
    private final Timer tiempoCreacion;
    private final Gauge colaActual;
    
    public TicketMetrics(MeterRegistry registry) {
        this.ticketsCreados = Counter.builder("tickets.creados")
            .tag("tipo", "total")
            .register(registry);
            
        this.tiempoCreacion = Timer.builder("tickets.tiempo.creacion")
            .register(registry);
    }
}
```

### 11.2 Health Checks

```java
@Component
public class TelegramHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        try {
            // Ping a Telegram API
            return Health.up()
                .withDetail("telegram", "OK")
                .withDetail("lastCheck", LocalDateTime.now())
                .build();
        } catch (Exception e) {
            return Health.down()
                .withDetail("telegram", "ERROR")
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

---

## 12. Roadmap de Implementación

### Fase 1: MVP Core (4 semanas)
- ✅ Entidades y repositorios
- ✅ Servicios básicos (crear ticket, calcular posición)
- ✅ API REST endpoints
- ✅ Integración Telegram básica

### Fase 2: Funcionalidades Avanzadas (3 semanas)
- ✅ Asignación automática de ejecutivos
- ✅ Dashboard tiempo real con WebSockets
- ✅ Sistema de notificaciones completo
- ✅ Cache y optimizaciones

### Fase 3: Mejoras y Producción (2 semanas)
- ✅ Geolocalización inteligente
- ✅ Métricas y monitoreo
- ✅ Tests de integración
- ✅ Deployment automatizado

---

## 13. Diagramas de Arquitectura

Los siguientes diagramas PlantUML están disponibles en `/docs/diagrams/`:

1. **system-context.puml** - Diagrama de contexto del sistema
2. **use-cases.puml** - Casos de uso principales
3. **sequence-crear-ticket.puml** - Flujo de creación de ticket
4. **sequence-asignacion.puml** - Flujo de asignación automática
5. **database-schema.puml** - Esquema de base de datos
6. **component-architecture.puml** - Arquitectura de componentes

---

## 🎯 Resumen Ejecutivo

**Arquitectura**: Monolito modular Spring Boot 3.2 + PostgreSQL  
**Deployment**: Docker Compose (dev) → Kubernetes (prod opcional)  
**Integraciones**: Telegram Bot API + Cache Caffeine  
**Testing**: Testcontainers + JUnit 5  
**Monitoreo**: Micrometer + Spring Actuator  

**Time to Market**: 6-8 semanas para MVP funcional  
**Escalabilidad**: Soporta hasta 25K tickets/día sin refactoring mayor  
**Mantenibilidad**: Arquitectura simple, documentación automática, CI/CD ready

Esta propuesta balancea **simplicidad operacional** con **robustez técnica**, priorizando entrega rápida de valor sin comprometer calidad o escalabilidad futura.

---

**Versión**: 1.0  
**Última actualización**: Diciembre 2024  
**Estado**: Aprobada para implementación