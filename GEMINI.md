# 🚀 Sistema de Gestión de Tickets con Notificaciones en Tiempo Real (Arquitectura con Java y Spring Boot)

Este documento presenta una propuesta de arquitectura y stack tecnológico para el **Sistema de Gestión de Tickets**, basado en los requerimientos del proyecto y orientado a un enfoque pragmático y productivo con tecnologías del ecosistema Java.

## 1. Stack Tecnológico

- **Lenguaje y Plataforma:** Java 21 (LTS)
- **Framework Principal:** Spring Boot 3.2
- **Acceso a Datos:** Spring Data JPA
- **Base de Datos:** PostgreSQL
- **Migraciones de Base de Datos:** Flyway
- **Notificaciones:** SDK de Telegram para Java (ej. `java-telegram-bot-api`)
- **Contenerización:** Docker

## 2. Arquitectura Recomendada: Monolito Modular

Para este proyecto, la arquitectura más adecuada es un **Monolito Modular**.

**Justificación:**

- **Simplicidad Operacional:** Un único artefacto desplegable simplifica enormemente el despliegue, la monitorización y la gestión operativa, especialmente en las fases iniciales del proyecto.
- **Baja Complejidad Inicial:** Los requerimientos, aunque bien definidos, no presentan una complejidad que justifique la sobrecarga de una arquitectura de microservicios (gestión de red, consistencia de datos, despliegues distribuidos).
- **Cohesión del Dominio:** El dominio del negocio (tickets, colas, clientes, ejecutivos) está altamente cohesionado. Separarlo en servicios independientes generaría acoplamiento y una comunicación excesiva entre ellos.
- **Evolución Natural:** Un diseño modular bien implementado permite, si el sistema crece en complejidad, extraer módulos a microservicios de forma controlada y solo cuando sea estrictamente necesario.

La estructura modular se basará en separar la lógica del negocio en componentes cohesivos y débilmente acoplados, utilizando paquetes de Java como fronteras lógicas.

## 3. Estructura del Proyecto (Spring Boot 3.2)

Se propone una estructura de proyecto orientada a la funcionalidad, donde cada módulo del negocio agrupa sus propias clases relacionadas (controladores, servicios, repositorios, etc.).

```
/
├── .gitignore
├── build.gradle.kts      # o pom.xml
├── flyway/               # Scripts de migración de base deatos (V1__init.sql, etc.)
├── docker-compose.yml    # Para levantar PostgreSQL y la aplicación localmente
├── Dockerfile            # Para construir la imagen de la aplicación
└── src/
    └── main/
        ├── java/
        │   └── com/institucion/ticketero/
        │       ├── TicketeroApplication.java   # Punto de entrada
        │       ├── common/                     # Componentes transversales
        │       │   ├── config/                 # Configuración (Beans, Seguridad)
        │       │   ├── exceptions/             # Excepciones personalizadas
        │       │   └── scheduling/             # Tareas programadas (si aplica)
        │       │
        │       ├── module_tickets/             # Módulo de gestión de tickets
        │       │   ├── api/                    # DTOs (Request/Response) y Controladores REST
        │       │   ├── domain/                 # Entidades, Enums y lógica de dominio pura
        │       │   ├── application/            # Casos de uso o servicios de aplicación
        │       │   └── infrastructure/         # Repositorios (JPA), clientes externos
        │       │
        │       ├── module_queues/              # Módulo de gestión de colas
        │       │   ├── api/
        │       │   ├── domain/
        │       │   ├── application/
        │       │   └── infrastructure/
        │       │
        │       ├── module_executives/          # Módulo de gestión de ejecutivos
        │       │   └── ...
        │       │
        │       └── module_notifications/       # Módulo para envío de notificaciones
        │           ├── application/            # Servicio de notificación
        │           └── infrastructure/         # Cliente de Telegram
        │
        └── resources/
            ├── application.yml                 # Configuración principal de Spring
            └── db/migration/                   # Ubicación de scripts de Flyway
```

## 4. Diseño del Dominio y Persistencia

### 4.1. Agregados y Entidades Principales

El núcleo del dominio se centrará en los siguientes agregados:

- **Agregado `Ticket`:** Es la entidad central.
  - `Ticket` (Raíz del agregado): Contendrá el estado del ticket (`PENDING`, `ATTENDING`, `CLOSED`), el tipo de atención, el RUT/ID del cliente, y la referencia a la cola.
  - `CustomerInfo`: Objeto de valor con la información del cliente (RUT, Telegram ID).

- **Agregado `Queue` (Cola de Atención):**
  - `Queue` (Raíz del agregado): Representa una cola específica (ej. "Caja"). Contendrá el tipo de atención, la lista de tickets en espera (o una referencia a ellos), y la configuración de prioridad y tiempo promedio.

- **Agregado `Executive` (Ejecutivo):**
  - `Executive` (Raíz del agregado): Representa a un ejecutivo de atención. Contendrá su estado (`AVAILABLE`, `BUSY`), el módulo donde atiende y el tipo de colas que puede atender.

### 4.2. DTOs vs. Entidades

- **Entidades (`@Entity`):** Se usarán exclusivamente para la capa de persistencia con JPA. Contendrán la lógica de estado y las validaciones más básicas. Se mantendrán "limpias", sin dependencias de frameworks externos más allá de `jakarta.persistence`.
- **DTOs (Data Transfer Objects):** Se utilizarán para toda la comunicación con el exterior (API REST). Se crearán DTOs específicos para cada caso de uso (ej. `CreateTicketRequest`, `TicketStatusResponse`, `DashboardMetricsDTO`). Esto desacopla la API de la estructura de la base de datos, permitiendo que ambas evolucionen de forma independiente. **No se expondrán las entidades JPA directamente en la API.**

### 4.3. Estrategia de Persistencia con PostgreSQL y Flyway

- **Spring Data JPA:** Se utilizará para abstraer el acceso a datos. Los repositorios (`TicketRepository`, `QueueRepository`, etc.) extenderán de `JpaRepository` para operaciones CRUD básicas y permitirán consultas personalizadas con JPQL o `Criteria API`.
- **PostgreSQL:** Es una base de datos relacional robusta, ideal para mantener la consistencia transaccional que este sistema requiere (ej. asignar un ticket y marcar al ejecutivo como ocupado debe ser una operación atómica).
- **Flyway:** Se usará para gestionar el versionado del esquema de la base de datos de manera programática.
  - Los scripts de migración SQL se ubicarán en `src/main/resources/db/migration`.
  - El primer script (`V1__initial_schema.sql`) creará todas las tablas, secuencias e índices necesarios.
  - Flyway se ejecutará automáticamente al iniciar la aplicación, asegurando que la base de datos esté siempre en el estado esperado.

## 5. Conectores e Integraciones Externas

La única integración externa clave es con **Telegram**.

- **Módulo `module_notifications`:** Este módulo encapsulará toda la lógica de comunicación con la API de Telegram.
  - Se utilizará una librería cliente de Telegram para Java.
  - El `NotificationService` ofrecerá métodos de alto nivel como `sendConfirmation(ticket)` o `sendPreAlert(ticket)`.
  - La comunicación será **asíncrona** (`@Async`) para no bloquear el flujo principal de la aplicación (ej. la creación de un ticket no debe esperar a que el mensaje de Telegram se envíe).

## 6. Buenas Prácticas y Sugerencias

- **Records de Java 21:** Utilizar `records` para DTOs y objetos de valor inmutables. Son concisos, seguros y perfectos para este propósito.
- **Inmutabilidad:** Diseñar el dominio y los DTOs para ser tan inmutables como sea posible, reduciendo efectos secundarios y facilitando el razonamiento sobre el código.
- **Tests Unitarios y de Integración:**
  - **Unitarios:** Para la lógica de dominio y servicios, usando Mockito para simular dependencias.
  - **De Integración (`@SpringBootTest`):** Para probar el flujo completo desde el controlador hasta la base de datos, utilizando `Testcontainers` para levantar una instancia real de PostgreSQL en un contenedor Docker durante la ejecución de los tests.
- **Manejo de Configuración:** Utilizar el sistema de perfiles de Spring (`application-dev.yml`, `application-prod.yml`) para gestionar configuraciones específicas de cada entorno (ej. credenciales de la BD, token de Telegram).
- **Logging Estructurado:** Implementar logging estructurado (ej. JSON) para facilitar la ingesta y análisis en herramientas como ELK Stack o Splunk.

## 7. Herramientas de IaC y Orquestación

- **IaC (Infraestructura como Código):** Para un proyecto de este tamaño, **Terraform** es una excelente opción si se despliega en un proveedor cloud (AWS, Azure, GCP). Permite definir la base de datos (ej. RDS en AWS), el balanceador de carga y el servicio de contenedores de forma declarativa. Sin embargo, para un inicio rápido, un `docker-compose.yml` bien definido es suficiente para el entorno local y despliegues simples.
- **Orquestación:** **Kubernetes** es el estándar de facto, pero introduce una alta complejidad.
  - **Sugerencia pragmática:** Iniciar con un servicio de contenedores gestionado simple como **AWS App Runner**, **Azure Container Apps** o incluso una **instancia EC2/VM con Docker Compose**. Estas opciones ofrecen un excelente balance entre coste, simplicidad y escalabilidad inicial.

## 8. API REST Endpoints (Sugerencia)

```
/api
├── /tickets
│   ├── POST /                         # RF-001: Crea un nuevo ticket
│   │   // Request: { "rut": "...", "queueType": "PERSONAL_BANKER" }
│   │   // Response: { "ticketNumber": "PB-101", "position": 5, "estimatedWaitTime": 25 }
│   │
│   ├── GET /{ticketNumber}            # RF-006: Consulta estado detallado
│   │   // Response: { "status": "PENDING", "position": 5, ... }
│
├── /queues
│   ├── GET /                          # Obtiene estado de todas las colas
│   │   // Response: [ { "name": "Caja", "waiting": 10, ... } ]
│
└── /dashboard                         # RF-007: Métricas para el panel
    └── GET /metrics
        // Response: { "totalTickets": 150, "executives": [ ... ] }
```

---
*Este documento fue generado por Gemini como una propuesta de arquitectura técnica basada en los requerimientos del proyecto y las mejores prácticas de la industria.*