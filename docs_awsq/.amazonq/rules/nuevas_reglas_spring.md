# 01 - SPRING BOOT PATTERNS
## ARQUITECTURA EN CAPAS (OBLIGATORIO)
```
Controller → Service → Repository → Database
↓ ↓ ↓
HTTP Lógica Data Access
```
**Responsabilidades:**
- **Controller:** Recibe HTTP, valida (`@Valid`), delega, retorna HTTP
- **Service:** Lógica de negocio, transacciones, orquestación
- **Repository:** Solo data access (queries)
---
## ✅ INYECCIÓN DE DEPENDENCIAS
### CORRECTO: Constructor Injection
```java
@Service
@RequiredArgsConstructor // Lombok genera constructor
public class UserService {
private final UserRepository userRepository;
private final EmailService emailService;
// Lombok genera automáticamente:
// public UserService(UserRepository userRepo, EmailService email) {
// this.userRepository = userRepo;
// this.emailService = email;
// }
}
```
### ❌ INCORRECTO: Field Injection
```java
@Service
public class UserService {
@Autowired // ❌ NUNCA USAR
private UserRepository userRepository;
}

```
**Por qué constructor injection:**
- Inmutable (final fields)
- Testeable (mock dependencies fácilmente)
- Spring Boot best practice oficial
- Falla rápido si faltan dependencias
---
## �� CONTROLLER PATTERN
```java
@RestController
@RequestMapping(&quot;/api/users&quot;)
@RequiredArgsConstructor
@Slf4j
public class UserController {
private final UserService userService;
@PostMapping
public ResponseEntity&lt;UserResponse&gt; create(
@Valid @RequestBody UserRequest request
) {
log.info(&quot;Creating user: {}&quot;, request.email());
UserResponse response = userService.create(request);
return ResponseEntity.status(201).body(response);
}
@GetMapping(&quot;/{id}&quot;)
public ResponseEntity&lt;UserResponse&gt; getById(@PathVariable Long id) {
return userService.findById(id)
.map(ResponseEntity::ok)
.orElse(ResponseEntity.notFound().build());
}
@PutMapping(&quot;/{id}&quot;)
public ResponseEntity&lt;UserResponse&gt; update(
@PathVariable Long id,
@Valid @RequestBody UserRequest request
) {

UserResponse response = userService.update(id, request);
return ResponseEntity.ok(response);
}
@DeleteMapping(&quot;/{id}&quot;)
public ResponseEntity&lt;Void&gt; delete(@PathVariable Long id) {
userService.delete(id);
return ResponseEntity.noContent().build();
}
}
```
**Reglas Controller:**
- ✅ `@RestController` (no `@Controller`)
- ✅ `ResponseEntity&lt;T&gt;` para control HTTP explícito
- ✅ `@Valid` para activar validación automática
- ✅ Logging en operaciones importantes
- ✅ HTTP status apropiados (201, 204, 404)
- ❌ NO lógica de negocio aquí
- ❌ NO acceso directo a Repository
---
## ��️ SERVICE PATTERN
```java
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true) // Por defecto lectura
public class OrderService {
private final OrderRepository orderRepository;
private final ProductRepository productRepository;
private final NotificationService notificationService;
@Transactional // Escritura requiere anotación explícita
public OrderResponse create(OrderRequest request) {
// 1. Validar stock
validateProductStock(request.productId(), request.quantity());

// 2. Crear orden
Order order = Order.builder()
.customerId(request.customerId())
.productId(request.productId())
.quantity(request.quantity())
.status(OrderStatus.PENDING)
.build();
Order saved = orderRepository.save(order);
// 3. Notificar
notificationService.sendOrderConfirmation(saved);
// 4. Retornar DTO
return toResponse(saved);
}
public Optional&lt;OrderResponse&gt; findById(Long id) {
return orderRepository.findById(id)
.map(this::toResponse);
}
@Transactional
public void updateStatus(Long id, OrderStatus newStatus) {
Order order = orderRepository.findById(id)
.orElseThrow(() -&gt; new OrderNotFoundException(id));
order.setStatus(newStatus);
// Auto-save por @Transactional (dirty checking)
log.info(&quot;Order {} status updated to {}&quot;, id, newStatus);
}
// Métodos privados para lógica interna
private void validateProductStock(Long productId, int quantity) {
// Lógica de validación
}
private OrderResponse toResponse(Order order) {
return new OrderResponse(
order.getId(),
order.getCustomerId(),

order.getTotal(),
order.getStatus().name()
);
}
}
```
**Reglas Service:**
- ✅ `@Transactional(readOnly = true)` en clase
- ✅ `@Transactional` en métodos de escritura
- ✅ Métodos públicos &lt;20 líneas
- ✅ Métodos privados para sub-lógica
- ✅ Retornar DTOs, NUNCA entities
- ✅ Logging de operaciones importantes
- ❌ NO acceso directo a HTTP request/response
---
## ��️ REPOSITORY PATTERN
```java
@Repository
public interface UserRepository extends JpaRepository&lt;User, Long&gt; {
// Query derivada (Spring genera SQL automáticamente)
Optional&lt;User&gt; findByEmail(String email);
List&lt;User&gt; findByStatus(UserStatus status);
List&lt;User&gt; findByCreatedAtAfter(LocalDateTime date);
boolean existsByEmail(String email);
long countByStatus(UserStatus status);
// Query custom con @Query (solo cuando sea necesario)
@Query(&quot;&quot;&quot;
SELECT u FROM User u
WHERE u.status = :status
AND u.createdAt BETWEEN :startDate AND :endDate
ORDER BY u.createdAt DESC

&quot;&quot;&quot;)
List&lt;User&gt; findActiveUsersBetweenDates(
@Param(&quot;status&quot;) UserStatus status,
@Param(&quot;startDate&quot;) LocalDateTime startDate,
@Param(&quot;endDate&quot;) LocalDateTime endDate
);
// Query nativa (último recurso)
@Query(value = &quot;&quot;&quot;
SELECT * FROM users u
WHERE u.status = ?1
LIMIT ?2
&quot;&quot;&quot;, nativeQuery = true)
List&lt;User&gt; findTopNByStatus(String status, int limit);
}
```
**Reglas Repository:**
- ✅ Interface que extiende `JpaRepository&lt;Entity, ID&gt;`
- ✅ Preferir query derivadas (Spring las genera)
- ✅ Naming convention: `findBy`, `countBy`, `existsBy`, `deleteBy`
- ✅ `@Query` solo para queries complejas
- ✅ `@Param` para parámetros nombrados
- ✅ Text blocks `&quot;&quot;&quot;` para queries multilinea (Java 15+)
- ❌ NO lógica de negocio aquí
- ❌ NO queries nativas sin justificación fuerte
---
## �� CONFIGURATION PATTERN
```java
@Configuration
public class AppConfig {
@Bean
public RestTemplate restTemplate() {
return new RestTemplate();
}
@Bean

public ObjectMapper objectMapper() {
ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new JavaTimeModule());
mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
return mapper;
}
@Value(&quot;${app.api.url}&quot;)
private String apiUrl;
@Value(&quot;${app.api.timeout:5000}&quot;) // Default value
private int timeout;
}
```
**Reglas Configuration:**
- ✅ `@Configuration` para definir beans
- ✅ Métodos `@Bean` para dependencias externas
- ✅ `@Value` para inyectar propiedades
- ✅ Proporcionar defaults cuando sea posible
- ✅ Beans simples y concisos
---
## ⚠️ EXCEPTION HANDLING
```java
@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity&lt;ErrorResponse&gt; handleValidation(
MethodArgumentNotValidException ex
) {
String errors = ex.getBindingResult()
.getFieldErrors()
.stream()
.map(e -&gt; e.getField() + &quot;: &quot; + e.getDefaultMessage())
.collect(Collectors.joining(&quot;, &quot;));

log.error(&quot;Validation error: {}&quot;, errors);
return ResponseEntity
.badRequest()
.body(new ErrorResponse(errors, 400, LocalDateTime.now()));
}
@ExceptionHandler(EntityNotFoundException.class)
public ResponseEntity&lt;ErrorResponse&gt; handleNotFound(
EntityNotFoundException ex
) {
log.error(&quot;Entity not found: {}&quot;, ex.getMessage());
return ResponseEntity
.status(404)
.body(new ErrorResponse(ex.getMessage(), 404, LocalDateTime.now()));
}
@ExceptionHandler(Exception.class)
public ResponseEntity&lt;ErrorResponse&gt; handleGeneral(Exception ex) {
log.error(&quot;Unexpected error&quot;, ex);
return ResponseEntity
.status(500)
.body(new ErrorResponse(&quot;Internal server error&quot;, 500,
LocalDateTime.now()));
}
}
```
**Reglas Exception Handling:**
- ✅ `@ControllerAdvice` para manejo global
- ✅ `@ExceptionHandler` para cada tipo
- ✅ Logging de errores
- ✅ `ErrorResponse` consistente
- ✅ HTTP status apropiados (400, 404, 500)
- ❌ NO exponer stacktraces al cliente
---
## �� PACKAGE STRUCTURE
```

com.example.myapp/
├── controller/ # @RestController
├── service/ # @Service
├── repository/ # @Repository (interfaces JPA)
├── model/
│ ├── entity/ # @Entity (JPA entities)
│ └── dto/ # Records (Request/Response)
├── config/ # @Configuration
├── scheduler/ # @Scheduled (tasks programadas)
├── exception/ # Custom exceptions + @ControllerAdvice
└── util/ # Utilidades (si es necesario)
```
**Reglas Package:**
- ✅ Organización por tipo (controller, service, repository)
- ✅ Separación entities vs DTOs
- ✅ Nombres en singular (service, no services)
- ❌ NO mezclar capas en un mismo package
---
## �� CHECKLIST SPRING BOOT
Antes de crear clase, verifica:
- [ ] ¿Capa correcta? (Controller/Service/Repository)
- [ ] ¿Constructor injection con `@RequiredArgsConstructor`?
- [ ] ¿Anotación correcta? (`@Service`/`@RestController`/`@Repository`)
- [ ] ¿Usa Lombok? (`@Slf4j` si necesita logging)
- [ ] ¿Métodos públicos &lt;20 líneas?
- [ ] ¿Logging en operaciones críticas?
- [ ] ¿Service retorna DTOs, NO entities?
- [ ] ¿Controller usa `@Valid` para validación?
---
**Versión:** 2.0
**Framework:** Spring Boot 3.x
**Enfoque:** Buenas prácticas genéricas