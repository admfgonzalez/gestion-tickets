
# Sistema de Gestión de Tickets con Notificaciones en Tiempo Real

Este proyecto es un sistema de gestión de tickets para atención en sucursales, diseñado para modernizar la experiencia del cliente a través de la digitalización, notificaciones en tiempo real y optimización de la asignación de ejecutivos.

## ✨ Features

- **Creación de Tickets Digitales:** Permite a los clientes obtener un ticket digital, eliminando la necesidad de tickets físicos.
- **Notificaciones en Tiempo Real:** Envía notificaciones automáticas a los clientes a través de Telegram en tres momentos clave:
    1.  **Confirmación:** Al momento de crear el ticket.
    2.  **Pre-aviso:** Cuando quedan 3 personas por delante.
    3.  **Turno Activo:** Cuando un ejecutivo está listo para atenderlos.
- **Asignación Automática:** Asigna de manera inteligente el siguiente ticket en cola al ejecutivo que se encuentre disponible.
- **Múltiples Colas de Atención:** Soporta diferentes tipos de colas (Caja, Personal Banker, Empresas, Gerencia) con distintas prioridades y tiempos de atención.
- **Consulta de Estado:** Los clientes pueden consultar el estado de su ticket en cualquier momento.
- **Dashboard de Monitoreo:** Un panel en tiempo real para que los supervisores monitoreen el estado de las colas, los ejecutivos y las métricas de atención.

## 🚀 Tech Stack

- **Lenguaje:** Java 21
- **Framework:** Spring Boot 3.2.5
- **Base de Datos:** PostgreSQL
- **Acceso a Datos:** Spring Data JPA
- **Migraciones de Base de Datos:** Flyway
- **Notificaciones:** Telegram Bot API
- **Contenerización:** Docker

## 🏛️ Arquitectura

El sistema sigue una arquitectura de **Monolito Modular**. Esta decisión se basa en la simplicidad operativa y la alta cohesión del dominio del negocio. La estructura modular permite una futura evolución hacia microservicios si el sistema crece en complejidad.

El proyecto está organizado en los siguientes módulos principales:

- `module_tickets`: Gestión de la creación y estado de los tickets.
- `module_queues`: Administración de las colas de atención.
- `module_executives`: Manejo del estado y disponibilidad de los ejecutivos.
- `module_notifications`: Envío de notificaciones a través de servicios externos (Telegram).

## 🏁 Getting Started

### Prerequisites

- Docker y Docker Compose
- Java 21
- Gradle 8.5 o superior

### Running the application with Docker

La forma más sencilla de levantar el entorno completo (aplicación + base de datos) es usando `docker-compose`.

1.  **Clonar el repositorio:**
    ```bash
    git clone <repository-url>
    cd gestion-tickets
    ```

2.  **Crear el archivo de entorno:**
    Crea un archivo `.env` en la raíz del proyecto con las siguientes variables:
    ```env
    # PostgreSQL
    DB_HOST=postgres
    DB_PORT=5432
    DB_DATABASE=ticketero_db
    DB_USERNAME=admin
    DB_PASSWORD=secret

    # Telegram
    TELEGRAM_BOT_TOKEN=TU_TOKEN_DE_TELEGRAM
    ```
    > Reemplaza `TU_TOKEN_DE_TELEGRAM` con el token de tu bot de Telegram.

3.  **Ejecutar el script de inicio:**
    Abre una terminal de PowerShell, navega a la raíz del proyecto y ejecuta el siguiente comando:
    ```powershell
    ./start-app.ps1
    ```
    La aplicación estará disponible en `http://localhost:8080`.

### Running the application locally (without Docker)

1.  **Levantar la base de datos:**
    Puedes usar el `docker-compose.yml` para levantar solo la base de datos:
    ```bash
    docker-compose up -d postgres
    ```

2.  **Configurar las variables de entorno:**
    Modifica el archivo `src/main/resources/application.yml` o configura las variables de entorno en tu IDE para que apunten a la base de datos local.
    ```yml
    spring:
      datasource:
        url: jdbc:postgresql://localhost:5432/ticketero_db
        username: admin
        password: secret
    # ...
    telegram:
      bot-token: "TU_TOKEN_DE_TELEGRAM"
    ```

3.  **Ejecutar la aplicación:**
    ```bash
    ./gradlew bootRun
    ```

## 📋 API Endpoints

Una descripción detallada de los endpoints se encuentra en el documento de arquitectura `GEMINI.md`. A continuación, un resumen de los principales:

- `POST /api/tickets`: Crea un nuevo ticket.
- `GET /api/tickets/{ticketNumber}`: Consulta el estado de un ticket.
- `GET /api/queues`: Obtiene el estado de todas las colas.
- `GET /api/dashboard/metrics`: Obtiene métricas para el panel de monitoreo.

## 🗃️ Database Migrations

Las migraciones de la base de datos se gestionan con **Flyway**. Los scripts de migración se encuentran en `src/main/resources/db/migration`. Flyway se ejecuta automáticamente al iniciar la aplicación, asegurando que el esquema de la base de datos esté siempre actualizado.

## ✅ Running Tests

Para ejecutar los tests de la aplicación, utiliza el siguiente comando:

```bash
./gradlew test
```
Los tests de integración utilizan **Testcontainers** para levantar una instancia de PostgreSQL en un contenedor de Docker, asegurando un entorno de prueba limpio y aislado.
