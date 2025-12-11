# Descriptor del Proyecto: Sistema de Gestión de Tickets con Notificaciones en Tiempo Real

## 1. Descripción General del Proyecto

Este documento describe el "Sistema de Gestión de Tickets para Atención en Sucursales", una solución innovadora diseñada para modernizar la experiencia de atención al cliente en instituciones financieras. Aborda los desafíos actuales como la falta de visibilidad del cliente sobre los tiempos de espera y la inmovilidad forzada en la sucursal, transformándolos en una experiencia digital, eficiente y transparente. La solución propuesta integra la digitalización del proceso de tickets con notificaciones en tiempo real vía Telegram, asignación automática de tickets a ejecutivos y un panel de monitoreo operacional.

**Problemática Resuelta:** Clientes sin visibilidad de tiempos de espera, inmovilidad en sucursal, incertidumbre en el turno.
**Solución Central:** Digitalización, notificaciones automáticas, movilidad del cliente, asignación inteligente, monitoreo en tiempo real.

## 2. Arquitectura del Sistema: Monolito Modular Orientado a la Evolución

El sistema se ha diseñado bajo una **arquitectura de Monolito Modular**, seleccionada pragmáticamente para equilibrar la eficiencia inicial con la escalabilidad futura.

### Enfoque y Justificación:
*   **Simplicidad Operacional:** Un único artefacto desplegable simplifica el despliegue, monitoreo y gestión en fases iniciales.
*   **Baja Complejidad Inicial:** Los requisitos, aunque robustos, no justifican la complejidad inherente de microservicios desde el inicio.
*   **Cohesión del Dominio:** El dominio de negocio (tickets, colas, ejecutivos) está altamente cohesionado, minimizando acoplamiento entre módulos lógicos.
*   **Evolución Natural:** El diseño modular facilita una eventual migración o extracción de módulos a microservicios si la complejidad o el volumen de negocio lo demandan, sin un "rewriting" completo.

### Estructura Modular (Paquetes Java):
La organización del código se adhiere a un patrón por funcionalidad, agrupando controladores, servicios y repositorios dentro de módulos de negocio lógicos:
*   `common/`: Componentes transversales (configuración, excepciones).
*   `module_tickets/`: Gestión integral de tickets.
*   `module_queues/`: Administración de colas de atención.
*   `module_executives/`: Manejo del estado y asignación de ejecutivos.
*   `module_notifications/`: Encapsulamiento de la lógica de comunicación externa (Telegram).

## 3. Stack Tecnológico Moderno y Robusto

La selección de tecnologías se enfoca en la robustez, el rendimiento y la facilidad de mantenimiento, utilizando estándares de la industria:

*   **Lenguaje y Plataforma:** Java 21 (LTS), asegurando soporte a largo plazo y acceso a las últimas características del lenguaje.
*   **Framework Principal:** Spring Boot 3.2, para un desarrollo rápido, configuración sencilla y un ecosistema maduro.
*   **Acceso a Datos:**
    *   **Spring Data JPA:** Abstracción eficiente para interacción con la base de datos.
    *   **PostgreSQL:** Base de datos relacional open-source, reconocida por su fiabilidad, rendimiento y capacidad de manejo de la consistencia transaccional.
    *   **Flyway:** Gestión de migraciones de esquema de base de datos de manera versionada y automatizada, asegurando la consistencia del entorno.
*   **Notificaciones Externas:** SDK de Telegram para Java (`java-telegram-bot-api`), facilitando la integración con la plataforma de mensajería.
*   **Contenerización:** Docker, para empaquetar la aplicación y sus dependencias, garantizando entornos de desarrollo y producción consistentes y facilitando la integración CI/CD.
*   **Pruebas:** JUnit 5, Mockito para tests unitarios y **Testcontainers** para pruebas de integración con una instancia real de PostgreSQL en un contenedor Docker, garantizando la fiabilidad de las interacciones con la base de datos.

## 4. Requerimientos Funcionales Clave e Implementación

El sistema satisface una serie de requisitos funcionales críticos, con un enfoque en la experiencia del usuario y la eficiencia operativa:

*   **RF-001: Crear Ticket Digital:** Implementado a través de una API RESTful (`POST /tickets`) que recibe DTOs de `CreateTicketRequest`, genera un número único, calcula posición y tiempo, y envía la confirmación.
*   **RF-002: Enviar Notificaciones Automáticas:** El `module_notifications` gestiona el envío de 3 tipos de mensajes (Confirmación, Pre-aviso, Turno Activo) vía Telegram de forma **asíncrona (`@Async`)** para no bloquear el flujo principal.
*   **RF-003: Calcular Posición y Tiempo Estimado:** Lógica de dominio central que estima en tiempo real la posición y el tiempo de espera, considerando ejecutivos disponibles y tiempo promedio por tipo de cola.
*   **RF-004: Asignar Ticket a Ejecutivo Automáticamente:** Un algoritmo de asignación inteligente considera prioridades de cola, balanceo de carga entre ejecutivos y orden FIFO para optimizar la atención.
*   **RF-005: Gestionar Múltiples Colas:** Soporte para 4 tipos de colas (Caja, Personal Banker, Empresas, Gerencia) con configuraciones específicas de tiempo promedio y prioridad.
*   **RF-006: Consultar Estado del Ticket:** API RESTful (`GET /tickets/{ticketNumber}`) permite a los clientes consultar el estado detallado de su ticket en cualquier momento.
*   **RF-007: Panel de Monitoreo para Supervisor:** Un dashboard en tiempo real (`GET /dashboard/metrics`) proporciona métricas operacionales clave, con actualizaciones automáticas cada 5 segundos para supervisión proactiva.
*   **RF-008: Registrar Auditoría de Eventos:** Logging estructurado y persistencia de eventos relevantes (creación, asignación, cambios de estado, mensajes) para trazabilidad y análisis.

## 5. Requerimientos No Funcionales Clave y Estrategia

El diseño del sistema aborda rigurosamente los requisitos no funcionales para garantizar una operación estable y de alto rendimiento:

*   **RNF-001: Disponibilidad:** Estrategias de recuperación automática y diseño resiliente, con un objetivo de 99.5% de uptime y recuperación en menos de 5 minutos.
*   **RNF-002: Performance:** Optimización de consultas y procesos críticos para asegurar tiempos de respuesta mínimos (e.g., creación de ticket < 3s, cálculo de posición < 1s).
*   **RNF-003: Escalabilidad:** La arquitectura modular permite escalar horizontalmente, con una capacidad diseñada para soportar desde 500 tickets/día en fase piloto hasta 25,000+ tickets/día en fase nacional.
*   **RNF-004: Confiabilidad:** Implementación de mecanismos de reintento para notificaciones (3 reintentos automáticos) y diseño para asegurar la integridad y no pérdida de datos.
*   **RNF-005: Seguridad:** Cumplimiento con la ley de protección de datos, encriptación de información sensible (RUT, Telegram ID) y acceso basado en roles al panel administrativo.
*   **RNF-006: Usabilidad:** Interfaces de usuario intuitivas y mensajes claros, diseñados para una curva de aprendizaje mínima y una experiencia fluida.
*   **RNF-007: Mantenibilidad:** Código limpio, documentado, arquitectura modular y logs detallados que facilitan el diagnóstico y futuras evoluciones.

## 6. Buenas Prácticas y Metodologías Aplicadas

El proyecto se adhiere a un conjunto de buenas prácticas de desarrollo de software:

*   **Inmutabilidad:** Uso extensivo de Java Records para DTOs y objetos de valor, promoviendo un código más seguro y predecible.
*   **Diseño Limpio:** Separación clara de responsabilidades entre capas (API, Dominio, Aplicación, Infraestructura).
*   **Desarrollo Orientado a Tests (TDD/Unit & Integration Tests):** Cobertura exhaustiva de pruebas, incluyendo:
    *   **Unitarias:** Con JUnit y Mockito para la lógica de negocio.
    *   **Integración:** Con `@SpringBootTest` y **Testcontainers** para validar la interacción con componentes externos como PostgreSQL, garantizando la robustez del sistema.
*   **Manejo de Configuración:** Utilización de perfiles de Spring (`application-dev.yml`, `application-prod.yml`) para una gestión flexible de configuraciones por entorno.
*   **Logging Estructurado:** Implementación de logs en formato JSON para facilitar la monitorización, análisis y depuración centralizada en plataformas como ELK Stack.

## 7. Relevancia para Amazon Q y Desarrollo de Carrera

Este proyecto demuestra un conjunto de habilidades y experiencias altamente valoradas en entornos profesionales y para plataformas como Amazon Q, incluyendo:

*   **Diseño de Sistemas Escalables y Mantenibles:** Experiencia práctica en la conceptualización de arquitecturas modulares que permiten el crecimiento futuro y la gestión de la complejidad.
*   **Dominio de Java y Spring Boot:** Sólida base en el desarrollo de aplicaciones empresariales con el ecosistema Java líder.
*   **Desarrollo Orientado a Datos:** Competencia en el uso de Spring Data JPA, diseño de esquemas de bases de datos (PostgreSQL) y gestión de migraciones (Flyway).
*   **Integración con Servicios Externos:** Habilidad para implementar y gestionar integraciones con APIs externas (Telegram), incluyendo consideraciones de asincronía y reintentos.
*   **Cultura DevOps y Contenerización:** Experiencia con Docker para la creación de imágenes y la gestión de entornos de desarrollo/producción consistentes, fundamental para pipelines CI/CD.
*   **Testing de Alta Calidad:** Compromiso con la calidad del software a través de pruebas unitarias y de integración robustas, utilizando herramientas como Testcontainers para un testing realista.
*   **Atención a Requisitos No Funcionales:** Capacidad para traducir requisitos de disponibilidad, performance, seguridad y escalabilidad en decisiones de diseño e implementación concretas.
*   **Resolución de Problemas de Negocio Complejos:** Aplicación de tecnología para resolver un problema real de ineficiencia en la atención al cliente, con métricas claras de éxito.

## 8. Beneficios y KPIs Alcanzados

El sistema está diseñado para generar un impacto significativo, medible a través de los siguientes KPIs:

*   **Mejora de NPS:** Incremento proyectado de 45 a 65 puntos.
*   **Reducción de Abandonos de Cola:** Disminución de 15% a 5%.
*   **Incremento en Tickets Atendidos por Ejecutivo:** Aumento del 20%.
*   **Trazabilidad Completa:** Provisión de datos para análisis continuo y mejora de procesos.

Este proyecto representa una implementación completa de un sistema transaccional moderno, destacando la capacidad de entregar valor de negocio a través de una sólida base técnica y metodológica.
