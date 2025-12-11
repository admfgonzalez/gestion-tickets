# **PROMPT 1: ANÁLISIS \- Requerimientos Funcionales del Sistema Ticketero**

## **Contexto**

**Eres un Analista de Negocio Senior trabajando para una institución financiera. Tu tarea es transformar el documento de negocio existente en un documento de Requerimientos Funcionales de nivel empresarial con criterios de aceptación verificables.**

**IMPORTANTE: Después de completar CADA paso, debes DETENERTE y solicitar una revisión exhaustiva antes de continuar con el siguiente paso.**

---

## **Documento de Entrada**

**Lee el siguiente archivo que YA está en tu proyecto:**

**docs/project-requirements.md**

**Este documento contiene:**

* **Contexto del negocio y problema a resolver**  
* **8 Requerimientos Funcionales (RF-001 a RF-008) en formato narrativo**  
* **Flujo detallado del proceso**  
* **Requerimientos No Funcionales**

---

## **Metodología de Trabajo**

### **Principio Fundamental:**

**"Documentar → Validar → Confirmar → Continuar"**

**Después de CADA paso:**

1. **✅ Documenta el requerimiento funcional completo**  
2. **✅ Valida criterios cuantitativos**  
3. **✅ Revisa formato y claridad**  
4. **⏸️ DETENTE y solicita revisión exhaustiva**  
5. **✅ Espera confirmación antes de continuar**

### **Formato de Solicitud de Revisión:**

**✅ PASO X COMPLETADO**

**Requerimiento documentado:**

**\- RF-XXX: \[Nombre\]**

**Validaciones realizadas:**

**\- \[checklist de criterios\]**

**🔍 SOLICITO REVISIÓN EXHAUSTIVA:**

**Por favor, revisa:**

**1\. ¿Los escenarios Gherkin son correctos?**

**2\. ¿El modelo de datos es completo?**

**3\. ¿Las reglas de negocio están aplicadas?**

**4\. ¿Los ejemplos JSON son válidos?**

**5\. ¿Puedo continuar con el siguiente RF?**

**⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...**

---

## **Tu Tarea**

**Transforma el documento de negocio en un documento de Requerimientos Funcionales profesional siguiendo la estructura y nivel de detalle del ejemplo a continuación.**

**Implementarás en 10 pasos con revisión en cada uno:**

* **PASO 1: Introducción y Reglas de Negocio**  
* **PASO 2: RF-001 (Crear Ticket Digital)**  
* **PASO 3: RF-002 (Enviar Notificaciones Telegram)**  
* **PASO 4: RF-003 (Calcular Posición y Tiempo)**  
* **PASO 5: RF-004 (Asignar Ticket a Ejecutivo)**  
* **PASO 6: RF-005 (Gestionar Múltiples Colas)**  
* **PASO 7: RF-006 (Consultar Estado del Ticket)**  
* **PASO 8: RF-007 (Panel de Monitoreo)**  
* **PASO 9: RF-008 (Registrar Auditoría)**  
* **PASO 10: Matrices de Trazabilidad y Validación Final**

---

## **PASO 1: Introducción y Reglas de Negocio**

**Objetivo: Crear la sección introductoria del documento y documentar las 13 reglas de negocio numeradas.**

**Tareas:**

* **Escribir introducción (propósito, alcance, definiciones)**  
* **Documentar RN-001 a RN-013 con descripción clara**  
* **Definir 4 enumeraciones (QueueType, TicketStatus, AdvisorStatus, MessageTemplate)**

**Implementación:**

### **1\. Introducción**

**\# Requerimientos Funcionales \- Sistema Ticketero Digital**

**\*\*Proyecto:\*\* Sistema de Gestión de Tickets con Notificaciones en Tiempo Real**  

**\*\*Cliente:\*\* Institución Financiera**  

**\*\*Versión:\*\* 1.0**  

**\*\*Fecha:\*\* Diciembre 2025**  

**\*\*Analista:\*\* \[Tu Nombre\]**

**\---**

**\#\# 1\. Introducción**

**\#\#\# 1.1 Propósito**

**Este documento especifica los requerimientos funcionales del Sistema Ticketero Digital, diseñado para modernizar la experiencia de atención en sucursales mediante:**

**\- Digitalización completa del proceso de tickets**

**\- Notificaciones automáticas en tiempo real vía Telegram**

**\- Movilidad del cliente durante la espera**

**\- Asignación inteligente de clientes a ejecutivos**

**\- Panel de monitoreo para supervisión operacional**

**\#\#\# 1.2 Alcance**

**Este documento cubre:**

**\- ✅ 8 Requerimientos Funcionales (RF-001 a RF-008)**

**\- ✅ 13 Reglas de Negocio (RN-001 a RN-013)**

**\- ✅ Criterios de aceptación en formato Gherkin**

**\- ✅ Modelo de datos funcional**

**\- ✅ Matriz de trazabilidad**

**Este documento NO cubre:**

**\- ❌ Arquitectura técnica (ver documento ARQUITECTURA.md)**

**\- ❌ Tecnologías de implementación**

**\- ❌ Diseño de interfaces de usuario**

**\#\#\# 1.3 Definiciones**

**| Término | Definición |**

**|---------|------------|**

**| Ticket | Turno digital asignado a un cliente para ser atendido |**

**| Cola | Fila virtual de tickets esperando atención |**

**| Asesor | Ejecutivo bancario que atiende clientes |**

**| Módulo | Estación de trabajo de un asesor (numerados 1-5) |**

**| Chat ID | Identificador único de usuario en Telegram |**

**| UUID | Identificador único universal para tickets |**

### **2\. Reglas de Negocio**

**Documenta las 13 reglas de negocio:**

**\#\# 2\. Reglas de Negocio**

**Las siguientes reglas de negocio aplican transversalmente a todos los requerimientos funcionales:**

**\*\*RN-001: Unicidad de Ticket Activo\*\***  

**Un cliente solo puede tener 1 ticket activo a la vez. Los estados activos son: EN\_ESPERA, PROXIMO, ATENDIENDO. Si un cliente intenta crear un nuevo ticket teniendo uno activo, el sistema debe rechazar la solicitud con error HTTP 409 Conflict.**

**\*\*RN-002: Prioridad de Colas\*\***  

**Las colas tienen prioridades numéricas para asignación automática:**

**\- GERENCIA: prioridad 4 (máxima)**

**\- EMPRESAS: prioridad 3**

**\- PERSONAL\_BANKER: prioridad 2**

**\- CAJA: prioridad 1 (mínima)**

**Cuando un asesor se libera, el sistema asigna primero tickets de colas con mayor prioridad.**

**\*\*RN-003: Orden FIFO Dentro de Cola\*\***  

**Dentro de una misma cola, los tickets se procesan en orden FIFO (First In, First Out). El ticket más antiguo (createdAt menor) se asigna primero.**

**\*\*RN-004: Balanceo de Carga Entre Asesores\*\***  

**Al asignar un ticket, el sistema selecciona el asesor AVAILABLE con menor valor de assignedTicketsCount, distribuyendo equitativamente la carga de trabajo.**

**\*\*RN-005: Formato de Número de Ticket\*\***  

**El número de ticket sigue el formato: \[Prefijo\]\[Número secuencial 01-99\]**

**\- Prefijo: 1 letra según el tipo de cola**

**\- Número: 2 dígitos, del 01 al 99, reseteado diariamente**

**Ejemplos: C01, P15, E03, G02**

**\*\*RN-006: Prefijos por Tipo de Cola\*\***  

**\- CAJA → C**

**\- PERSONAL\_BANKER → P**

**\- EMPRESAS → E**

**\- GERENCIA → G**

**\*\*RN-007: Reintentos Automáticos de Mensajes\*\***  

**Si el envío de un mensaje a Telegram falla, el sistema reintenta automáticamente hasta 3 veces antes de marcarlo como FALLIDO.**

**\*\*RN-008: Backoff Exponencial en Reintentos\*\***  

**Los reintentos de mensajes usan backoff exponencial:**

**\- Intento 1: inmediato**

**\- Intento 2: después de 30 segundos**

**\- Intento 3: después de 60 segundos**

**\- Intento 4: después de 120 segundos**

**\*\*RN-009: Estados de Ticket\*\***  

**Un ticket puede estar en uno de estos estados:**

**\- EN\_ESPERA: esperando asignación a asesor**

**\- PROXIMO: próximo a ser atendido (posición ≤ 3\)**

**\- ATENDIENDO: siendo atendido por un asesor**

**\- COMPLETADO: atención finalizada exitosamente**

**\- CANCELADO: cancelado por cliente o sistema**

**\- NO\_ATENDIDO: cliente no se presentó cuando fue llamado**

**\*\*RN-010: Cálculo de Tiempo Estimado\*\***  

**El tiempo estimado de espera se calcula como:**

**tiempoEstimado \= posiciónEnCola × tiempoPromedioCola**

**Donde tiempoPromedioCola varía por tipo:**

**\- CAJA: 5 minutos**

**\- PERSONAL\_BANKER: 15 minutos**

**\- EMPRESAS: 20 minutos**

**\- GERENCIA: 30 minutos**

**\*\*RN-011: Auditoría Obligatoria\*\***  

**Todos los eventos críticos del sistema deben registrarse en auditoría con: timestamp, tipo de evento, actor involucrado, entityId afectado, y cambios de estado.**

**\*\*RN-012: Umbral de Pre-aviso\*\***  

**El sistema envía el Mensaje 2 (pre-aviso) cuando la posición del ticket es ≤ 3, indicando que el cliente debe acercarse a la sucursal.**

**\*\*RN-013: Estados de Asesor\*\***  

**Un asesor puede estar en uno de estos estados:**

**\- AVAILABLE: disponible para recibir asignaciones**

**\- BUSY: atendiendo un cliente (no recibe nuevas asignaciones)**

**\- OFFLINE: no disponible (almuerzo, capacitación, etc.)**

### **3\. Enumeraciones**

**\#\# 3\. Enumeraciones**

**\#\#\# 3.1 QueueType**

**Tipos de cola disponibles en el sistema:**

**| Valor | Display Name | Tiempo Promedio | Prioridad | Prefijo |**

**|-------|--------------|-----------------|-----------|---------|**

**| CAJA | Caja | 5 min | 1 | C |**

**| PERSONAL\_BANKER | Personal Banker | 15 min | 2 | P |**

**| EMPRESAS | Empresas | 20 min | 3 | E |**

**| GERENCIA | Gerencia | 30 min | 4 | G |**

**\#\#\# 3.2 TicketStatus**

**Estados posibles de un ticket:**

**| Valor | Descripción | Es Activo? |**

**|-------|-------------|------------|**

**| EN\_ESPERA | Esperando asignación | Sí |**

**| PROXIMO | Próximo a ser atendido | Sí |**

**| ATENDIENDO | Siendo atendido | Sí |**

**| COMPLETADO | Atención finalizada | No |**

**| CANCELADO | Cancelado | No |**

**| NO\_ATENDIDO | Cliente no se presentó | No |**

**\#\#\# 3.3 AdvisorStatus**

**Estados posibles de un asesor:**

**| Valor | Descripción | Recibe Asignaciones? |**

**|-------|-------------|----------------------|**

**| AVAILABLE | Disponible | Sí |**

**| BUSY | Atendiendo cliente | No |**

**| OFFLINE | No disponible | No |**

**\#\#\# 3.4 MessageTemplate**

**Plantillas de mensajes para Telegram:**

**| Valor | Descripción | Momento de Envío |**

**|-------|-------------|------------------|**

**| totem\_ticket\_creado | Confirmación de creación | Inmediato al crear ticket |**

**| totem\_proximo\_turno | Pre-aviso | Cuando posición ≤ 3 |**

**| totem\_es\_tu\_turno | Turno activo | Al asignar a asesor |**

**Validaciones:**

* **\[ \] Introducción tiene propósito, alcance y definiciones claras**  
* **\[ \] 13 Reglas de Negocio documentadas (RN-001 a RN-013)**  
* **\[ \] Cada RN tiene descripción completa y clara**  
* **\[ \] 4 Enumeraciones definidas con todos sus valores**  
* **\[ \] Formato profesional y consistente**

**🔍 PUNTO DE REVISIÓN 1:**

**✅ PASO 1 COMPLETADO**

**Secciones documentadas:**

**\- 1\. Introducción (propósito, alcance, definiciones)**

**\- 2\. Reglas de Negocio (RN-001 a RN-013)**

**\- 3\. Enumeraciones (4 enums definidas)**

**Validaciones realizadas:**

**\- ✅ 13 Reglas de Negocio documentadas**

**\- ✅ Cada RN tiene descripción clara**

**\- ✅ RN-010 incluye fórmula de cálculo**

**\- ✅ 4 Enumeraciones con valores completos**

**\- ✅ Tablas bien formateadas**

**🔍 SOLICITO REVISIÓN EXHAUSTIVA:**

**Por favor, revisa:**

**1\. ¿La introducción es clara y profesional?**

**2\. ¿Las 13 reglas de negocio son comprensibles?**

**3\. ¿Las enumeraciones tienen todos los valores necesarios?**

**4\. ¿El formato es consistente?**

**5\. ¿Puedo continuar con PASO 2 (RF-001)?**

**⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...**

---

## **PASO 2: RF-001 (Crear Ticket Digital)**

**Objetivo: Documentar el RF-001 completo siguiendo el ejemplo proporcionado.**

**Tareas:**

* **Copiar y adaptar el ejemplo completo de RF-001**  
* **Asegurar 7 escenarios Gherkin mínimo**  
* **Incluir modelo de datos con 12 campos**  
* **Incluir reglas de negocio aplicables**  
* **Incluir ejemplos JSON de respuestas HTTP**

**Implementación:**

### **RF-001: Crear Ticket Digital**

**Descripción: El sistema debe permitir al cliente crear un ticket digital para ser atendido en sucursal, ingresando su identificación nacional (RUT/ID), número de teléfono y seleccionando el tipo de atención requerida. El sistema generará un número único de ticket, calculará la posición actual en cola y el tiempo estimado de espera basado en datos reales de la operación.**

**Prioridad: Alta**

**Actor Principal: Cliente**

**Precondiciones:**

* **Terminal de autoservicio disponible y funcional**  
* **Sistema de gestión de colas operativo**  
* **Conexión a base de datos activa**

**Modelo de Datos (Campos del Ticket):**

**\- codigoReferencia: UUID único (ej: "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6")**

**\- numero: String formato específico por cola (ej: "C01", "P15", "E03", "G02")**

**\- nationalId: String, identificación nacional del cliente**

**\- telefono: String, número de teléfono para Telegram**

**\- branchOffice: String, nombre de la sucursal**

**\- queueType: Enum (CAJA, PERSONAL\_BANKER, EMPRESAS, GERENCIA)**

**\- status: Enum (EN\_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO\_ATENDIDO)**

**\- positionInQueue: Integer, posición actual en cola (calculada en tiempo real)**

**\- estimatedWaitMinutes: Integer, minutos estimados de espera**

**\- createdAt: Timestamp, fecha/hora de creación**

**\- assignedAdvisor: Relación a entidad Advisor (null inicialmente)**

**\- assignedModuleNumber: Integer 1-5 (null inicialmente)**

**Reglas de Negocio Aplicables:**

* **RN-001: Un cliente solo puede tener 1 ticket activo a la vez**  
* **RN-005: Número de ticket formato: \[Prefijo\]\[Número secuencial 01-99\]**  
* **RN-006: Prefijos por cola: C=Caja, P=Personal Banker, E=Empresas, G=Gerencia**  
* **RN-010: Cálculo de tiempo estimado: posiciónEnCola × tiempoPromedioCola**

**Criterios de Aceptación (Gherkin):**

**Escenario 1: Creación exitosa de ticket para cola de Caja**

**Given el cliente con nationalId "12345678-9" no tiene tickets activos**

**And el terminal está en pantalla de selección de servicio**

**When el cliente ingresa:**

  **| Campo        | Valor           |**

  **| nationalId   | 12345678-9      |**

  **| telefono     | \+56912345678    |**

  **| branchOffice | Sucursal Centro |**

  **| queueType    | CAJA            |**

**Then el sistema genera un ticket con:**

  **| Campo                 | Valor Esperado                    |**

  **| codigoReferencia      | UUID válido                       |**

  **| numero                | "C\[01-99\]"                        |**

  **| status                | EN\_ESPERA                         |**

  **| positionInQueue       | Número \> 0                        |**

  **| estimatedWaitMinutes  | positionInQueue × 5               |**

  **| assignedAdvisor       | null                              |**

  **| assignedModuleNumber  | null                              |**

**And el sistema almacena el ticket en base de datos**

**And el sistema programa 3 mensajes de Telegram**

**And el sistema retorna HTTP 201 con JSON:**

  **{**

    **"identificador": "a1b2c3d4-e5f6-7g8h-9i0j-k1l2m3n4o5p6",**

    **"numero": "C01",**

    **"positionInQueue": 5,**

    **"estimatedWaitMinutes": 25,**

    **"queueType": "CAJA"**

  **}**

**Escenario 2: Error \- Cliente ya tiene ticket activo**

**Given el cliente con nationalId "12345678-9" tiene un ticket activo:**

  **| numero | status     | queueType      |**

  **| P05    | EN\_ESPERA  | PERSONAL\_BANKER|**

**When el cliente intenta crear un nuevo ticket con queueType CAJA**

**Then el sistema rechaza la creación**

**And el sistema retorna HTTP 409 Conflict con JSON:**

  **{**

    **"error": "TICKET\_ACTIVO\_EXISTENTE",**

    **"mensaje": "Ya tienes un ticket activo: P05",**

    **"ticketActivo": {**

      **"numero": "P05",**

      **"positionInQueue": 3,**

      **"estimatedWaitMinutes": 45**

    **}**

  **}**

**And el sistema NO crea un nuevo ticket**

**Escenario 3: Validación \- RUT/ID inválido**

**Given el terminal está en pantalla de ingreso de datos**

**When el cliente ingresa nationalId vacío**

**Then el sistema retorna HTTP 400 Bad Request con JSON:**

  **{**

    **"error": "VALIDACION\_FALLIDA",**

    **"campos": {**

      **"nationalId": "El RUT/ID es obligatorio"**

    **}**

  **}**

**And el sistema NO crea el ticket**

**Escenario 4: Validación \- Teléfono en formato inválido**

**Given el terminal está en pantalla de ingreso de datos**

**When el cliente ingresa telefono "123"**

**Then el sistema retorna HTTP 400 Bad Request**

**And el mensaje de error especifica formato requerido "+56XXXXXXXXX"**

**Escenario 5: Cálculo de posición \- Primera persona en cola**

**Given la cola de tipo PERSONAL\_BANKER está vacía**

**When el cliente crea un ticket para PERSONAL\_BANKER**

**Then el sistema calcula positionInQueue \= 1**

**And estimatedWaitMinutes \= 15**

**And el número de ticket es "P01"**

**Escenario 6: Cálculo de posición \- Cola con tickets existentes**

**Given la cola de tipo EMPRESAS tiene 4 tickets EN\_ESPERA**

**When el cliente crea un nuevo ticket para EMPRESAS**

**Then el sistema calcula positionInQueue \= 5**

**And estimatedWaitMinutes \= 100**

**And el cálculo es: 5 × 20min \= 100min**

**Escenario 7: Creación sin teléfono (cliente no quiere notificaciones)**

**Given el cliente no proporciona número de teléfono**

**When el cliente crea un ticket**

**Then el sistema crea el ticket exitosamente**

**And el sistema NO programa mensajes de Telegram**

**Postcondiciones:**

* **Ticket almacenado en base de datos con estado EN\_ESPERA**  
* **3 mensajes programados (si hay teléfono)**  
* **Evento de auditoría registrado: "TICKET\_CREADO"**

**Endpoints HTTP:**

* **`POST /api/tickets` \- Crear nuevo ticket**

---

**Validaciones:**

* **\[ \] RF-001 documentado completamente**  
* **\[ \] 7 escenarios Gherkin incluidos**  
* **\[ \] Modelo de datos con 12 campos especificados**  
* **\[ \] Reglas de negocio aplicables referenciadas (RN-001, RN-005, RN-006, RN-010)**  
* **\[ \] Ejemplos JSON válidos para respuestas HTTP**  
* **\[ \] Precondiciones y postcondiciones claras**

**🔍 PUNTO DE REVISIÓN 2:**

**✅ PASO 2 COMPLETADO**

**Requerimiento documentado:**

**\- RF-001: Crear Ticket Digital**

**Validaciones realizadas:**

**\- ✅ 7 escenarios Gherkin (happy path \+ errores \+ edge cases)**

**\- ✅ Modelo de datos con 12 campos definidos**

**\- ✅ RN-001, RN-005, RN-006, RN-010 aplicadas**

**\- ✅ Ejemplos JSON válidos (201, 409, 400\)**

**\- ✅ Formato Gherkin correcto (Given/When/Then/And)**

**🔍 SOLICITO REVISIÓN EXHAUSTIVA:**

**Por favor, revisa:**

**1\. ¿Los 7 escenarios cubren todos los casos importantes?**

**2\. ¿El modelo de datos tiene todos los campos necesarios?**

**3\. ¿Las reglas de negocio están correctamente aplicadas?**

**4\. ¿Los ejemplos JSON son válidos y completos?**

**5\. ¿Puedo continuar con PASO 3 (RF-002)?**

**⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...**

---

## **PASO 3: RF-002 (Enviar Notificaciones Automáticas vía Telegram)**

**Objetivo: Documentar RF-002 con el mismo nivel de detalle que RF-001.**

**Tareas:**

* **Documentar descripción, prioridad, actores**  
* **Incluir modelo de datos Mensaje (8 campos)**  
* **Documentar 3 plantillas de mensajes con texto completo**  
* **Crear mínimo 6 escenarios Gherkin**  
* **Aplicar RN-007 (reintentos) y RN-008 (backoff)**

**Guía de Implementación:**

* **3 plantillas de mensajes: totem\_ticket\_creado, totem\_proximo\_turno, totem\_es\_tu\_turno Guía de Implementación:**

**\#\#\# \*\*RF-002: Enviar Notificaciones Automáticas vía Telegram\*\***

**\*\*Descripción:\*\***

**\[Descripción completa del requerimiento basada en REQUERIMIENTOS-NEGOCIO.md\]**

**\*\*Prioridad:\*\* Alta**

**\*\*Actor Principal:\*\* Sistema (automatizado)**

**\*\*Precondiciones:\*\***

**\- Ticket creado con teléfono válido**

**\- Telegram Bot configurado y activo**

**\- Cliente tiene cuenta de Telegram**

**\*\*Modelo de Datos (Entidad Mensaje):\*\***

* **id: BIGSERIAL (primary key)**  
* **ticket\_id: BIGINT (foreign key a ticket)**  
* **plantilla: String (totem\_ticket\_creado, totem\_proximo\_turno, totem\_es\_tu\_turno)**  
* **estadoEnvio: Enum (PENDIENTE, ENVIADO, FALLIDO)**  
* **fechaProgramada: Timestamp**  
* **fechaEnvio: Timestamp (nullable)**  
* **telegramMessageId: String (nullable, retornado por Telegram API)**  
* **intentos: Integer (contador de reintentos, default 0\)**

**\*\*Plantillas de Mensajes:\*\***

**\*\*1. totem\_ticket\_creado:\*\***

**✅ \<b\>Ticket Creado\</b\>**

**Tu número de turno: \<b\>{numero}\</b\> Posición en cola: \<b\>\#{posicion}\</b\> Tiempo estimado: \<b\>{tiempo} minutos\</b\>**

**Te notificaremos cuando estés próximo.**

**\*\*2. totem\_proximo\_turno:\*\***

**⏰ \<b\>¡Pronto será tu turno\!\</b\>**

**Turno: \<b\>{numero}\</b\> Faltan aproximadamente 3 turnos.**

**Por favor, acércate a la sucursal.**

**\*\*3. totem\_es\_tu\_turno:\*\***

**🔔 \<b\>¡ES TU TURNO {numero}\!\</b\>**

**Dirígete al módulo: \<b\>{modulo}\</b\> Asesor: \<b\>{nombreAsesor}\</b\>**

**\*\*Reglas de Negocio Aplicables:\*\***

**\- RN-007: 3 reintentos automáticos**

**\- RN-008: Backoff exponencial (30s, 60s, 120s)**

**\- RN-011: Auditoría de envíos**

**\*\*Criterios de Aceptación (Gherkin):\*\***

**\[Incluir MÍNIMO 6 escenarios cubriendo:\]**

**1\. Envío exitoso del Mensaje 1**

**2\. Envío exitoso del Mensaje 2**

**3\. Envío exitoso del Mensaje 3**

**4\. Fallo de red en primer intento, éxito en segundo**

**5\. 3 reintentos fallidos → estado FALLIDO**

**6\. Backoff exponencial entre reintentos**

**7\. (Opcional) Cliente sin teléfono, no se programan mensajes**

**\*\*Postcondiciones:\*\***

**\- Mensaje insertado en BD con estado según resultado**

**\- telegram\_message\_id almacenado si éxito**

**\- Intentos incrementado en cada reintento**

**\- Auditoría registrada**

**\*\*Endpoints HTTP:\*\***

**\- Ninguno (proceso interno automatizado por scheduler)**

**Validaciones:**

* **\[ \] RF-002 documentado completamente**  
* **\[ \] Modelo de datos Mensaje con 8 campos**  
* **\[ \] 3 plantillas de mensajes con texto completo**  
* **\[ \] Mínimo 6 escenarios Gherkin**  
* **\[ \] RN-007 y RN-008 aplicadas correctamente**

**🔍 PUNTO DE REVISIÓN 3:**

**✅ PASO 3 COMPLETADO**

**Requerimiento documentado:**

**\- RF-002: Enviar Notificaciones Automáticas vía Telegram**

**Validaciones realizadas:**

**\- ✅ Modelo Mensaje con 8 campos definidos**

**\- ✅ 3 plantillas documentadas con formato HTML**

**\- ✅ Mínimo 6 escenarios Gherkin**

**\- ✅ RN-007 (reintentos) y RN-008 (backoff) aplicadas**

**\- ✅ Emojis incluidos en plantillas (✅, ⏰, 🔔)**

**🔍 SOLICITO REVISIÓN EXHAUSTIVA:**

**Por favor, revisa:**

**1\. ¿Las 3 plantillas tienen el formato HTML correcto?**

**2\. ¿Los 6+ escenarios cubren éxito, fallos y reintentos?**

**3\. ¿El modelo Mensaje es completo?**

**4\. ¿Las reglas de backoff están claras?**

**5\. ¿Puedo continuar con PASO 4 (RF-003)?**

**⏸️ ESPERANDO CONFIRMACIÓN PARA CONTINUAR...**

---

## **PASOS 4-9: Estructura Similar**

**\[Los pasos 4-9 seguirán el mismo patrón con revisiones exhaustivas\]**

**PASO 4: RF-003 (Calcular Posición y Tiempo) → Revisión**  
**PASO 5: RF-004 (Asignar Ticket a Ejecutivo) → Revisión**  
**PASO 6: RF-005 (Gestionar Múltiples Colas) → Revisión**  
**PASO 7: RF-006 (Consultar Estado) → Revisión**  
**PASO 8: RF-007 (Panel de Monitoreo) → Revisión**  
**PASO 9: RF-008 (Auditoría) → Revisión**

---

## **PASO 10: Matrices y Validación Final**

**Objetivo: Crear matrices de trazabilidad y realizar validación completa del documento.**

**Tareas:**

* **Crear matriz de trazabilidad RF → Beneficio → Endpoints**  
* **Crear matriz de dependencias entre RFs**  
* **Documentar casos de uso principales**  
* **Crear matriz de endpoints HTTP**  
* **Checklist de validación final**

**\[Implementación del PASO 10 con revisión final\]**

---

## **Instrucciones para Continuar**

**Después de recibir confirmación en cada paso, genera el siguiente RF con el mismo nivel de detalle que RF-001.**

**Estructura para cada RF (PASOS 4-9):**

* **Modelo de datos (si aplica)**  
* **Reglas de negocio aplicables**  
* **Plantillas: 3 plantillas de mensajes con texto completo**  
* **Algoritmo: posición \= COUNT(tickets EN\_ESPERA antes de este) \+ 1**  
* **Algoritmo: tiempoEstimado \= posición × tiempoPromedioCola**  
* **Tiempos promedio: Caja(5min), Personal Banker(15min), Empresas(20min), Gerencia(30min)**  
* **Endpoint: `GET /api/tickets/{numero}/position`**  
* **Mínimo 5 escenarios Gherkin**

### **RF-004: Asignar Ticket a Ejecutivo Automáticamente**

**Incluir:**

* **Modelo Advisor: name, email, status, moduleNumber, assignedTicketsCount**  
* **Estados: AVAILABLE, BUSY, OFFLINE**  
* **Algoritmo: seleccionar AVAILABLE con menor assignedTicketsCount**  
* **Prioridad colas: GERENCIA(4) \> EMPRESAS(3) \> PERSONAL\_BANKER(2) \> CAJA(1)**  
* **Reglas: RN-002 (prioridad), RN-003 (FIFO), RN-004 (balanceo)**  
* **Mínimo 7 escenarios Gherkin**

### **RF-005: Gestionar Múltiples Colas**

**Incluir:**

* **4 colas: CAJA(5min,p1), PERSONAL\_BANKER(15min,p2), EMPRESAS(20min,p3), GERENCIA(30min,p4)**  
* **Endpoints: `GET /api/admin/queues/{type}`, `GET /api/admin/queues/{type}/stats`**  
* **Mínimo 5 escenarios Gherkin**

### **RF-006: Consultar Estado del Ticket**

**Incluir:**

* **Consulta por UUID: `GET /api/tickets/{codigoReferencia}`**  
* **Consulta por número: `GET /api/tickets/{numero}/position`**  
* **Escenarios: EN\_ESPERA, ATENDIENDO, COMPLETADO, no existe**  
* **Mínimo 5 escenarios Gherkin**

### **RF-007: Panel de Monitoreo para Supervisor**

**Incluir:**

* **Dashboard: tickets por estado, clientes en espera por cola, estado asesores, tiempos promedio, alertas**  
* **Actualización cada 5 segundos**  
* **Endpoints: `GET /api/admin/dashboard`, `GET /api/admin/summary`, `GET /api/admin/advisors`**  
* **Mínimo 6 escenarios Gherkin**

### **RF-008: Registrar Auditoría de Eventos**

**Incluir:**

* **Eventos: TICKET\_CREADO, TICKET\_ASIGNADO, TICKET\_COMPLETADO, MENSAJE\_ENVIADO**  
* **Información: timestamp, tipoEvento, actor, entityId, cambiosEstado**  
* **Regla: RN-011 (auditoría obligatoria)**  
* **Mínimo 5 escenarios Gherkin**

---

## **Estructura del Documento Final**

**\# Requerimientos Funcionales \- Sistema Ticketero Digital**

**\*\*Proyecto:\*\* Sistema de Gestión de Tickets con Notificaciones en Tiempo Real**  

**\*\*Cliente:\*\* Institución Financiera**  

**\*\*Versión:\*\* 1.0**  

**\*\*Fecha:\*\* Diciembre 2025**

**\---**

**\#\# 1\. Introducción**

**\[Propósito, Alcance, Definiciones\]**

**\#\# 2\. Reglas de Negocio**

**\[RN-001 a RN-013 numeradas\]**

**\#\# 3\. Requerimientos Funcionales**

**\[RF-001 a RF-008 con formato del ejemplo\]**

**\#\# 4\. Matriz de Trazabilidad**

**\[RF → Beneficio → Endpoints\]**

**\#\# 5\. Modelo de Datos**

**\[Entidades: Ticket, Mensaje, Advisor \+ Enumeraciones\]**

**\#\# 6\. Casos de Uso Principales**

**\[CU-001, CU-002, CU-003\]**

**\#\# 7\. Matriz de Endpoints HTTP**

**\[11 endpoints mapeados\]**

**\#\# 8\. Validaciones y Reglas de Formato**

**\[Formatos de RUT, teléfono, etc.\]**

**\#\# 9\. Checklist de Validación**

**\[Completitud, Claridad, Trazabilidad\]**

**\#\# 10\. Glosario**

**\[Términos clave\]**

---

## **Reglas de Negocio (Completas)**

**Documenta estas 13 reglas de negocio:**

**RN-001: Un cliente solo puede tener 1 ticket activo a la vez**  
**RN-002: Prioridad: GERENCIA(4) \> EMPRESAS(3) \> PERSONAL\_BANKER(2) \> CAJA(1)**  
**RN-003: Orden FIFO dentro de cada cola**  
**RN-004: Balanceo de carga: seleccionar asesor con menor assignedTicketsCount**  
**RN-005: Formato número: \[Prefijo\]\[01-99\]**  
**RN-006: Prefijos: C=Caja, P=Personal Banker, E=Empresas, G=Gerencia**  
**RN-007: 3 reintentos automáticos para mensajes fallidos**  
**RN-008: Backoff exponencial: 30s, 60s, 120s**  
**RN-009: Estados ticket: EN\_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO\_ATENDIDO**  
**RN-010: tiempoEstimado \= posiciónEnCola × tiempoPromedioCola**  
**RN-011: Auditoría obligatoria para todos los eventos**  
**RN-012: Mensaje 2 (pre-aviso) cuando posición \<= 3**  
**RN-013: Estados asesor: AVAILABLE, BUSY, OFFLINE**

---

## **Enumeraciones**

**QueueType: CAJA, PERSONAL\_BANKER, EMPRESAS, GERENCIA**  
**TicketStatus: EN\_ESPERA, PROXIMO, ATENDIENDO, COMPLETADO, CANCELADO, NO\_ATENDIDO**  
**AdvisorStatus: AVAILABLE, BUSY, OFFLINE**  
**MessageTemplate: totem\_ticket\_creado, totem\_proximo\_turno, totem\_es\_tu\_turno**

---

## **Endpoints HTTP (11 total)**

| Método | Endpoint | RF |
| ----- | ----- | ----- |
| **POST** | **/api/tickets** | **RF-001** |
| **GET** | **/api/tickets/{uuid}** | **RF-006** |
| **GET** | **/api/tickets/{numero}/position** | **RF-003** |
| **GET** | **/api/admin/dashboard** | **RF-007** |
| **GET** | **/api/admin/queues/{type}** | **RF-005** |
| **GET** | **/api/admin/queues/{type}/stats** | **RF-005** |
| **GET** | **/api/admin/advisors** | **RF-007** |
| **GET** | **/api/admin/advisors/stats** | **RF-007** |
| **PUT** | **/api/admin/advisors/{id}/status** | **RF-007** |
| **GET** | **/api/admin/summary** | **RF-007** |
| **GET** | **/api/health** | **\-** |

---

## **Criterios de Calidad**

**Tu documento DEBE cumplir:**

**✅ Criterios Cuantitativos:**

* **\[ \] 8 RF documentados con el nivel de detalle del ejemplo**  
* **\[ \] Mínimo 44 escenarios Gherkin totales (RF-001:7, RF-002:6, RF-003:5, RF-004:7, RF-005:5, RF-006:5, RF-007:6, RF-008:5)**  
* **\[ \] 13 Reglas de Negocio numeradas**  
* **\[ \] 11 Endpoints HTTP mapeados**  
* **\[ \] 3 Entidades definidas (Ticket, Mensaje, Advisor)**  
* **\[ \] 4 Enumeraciones especificadas**

**✅ Criterios Cualitativos:**

* **\[ \] Formato Gherkin correcto (Given/When/Then/And)**  
* **\[ \] Ejemplos JSON en respuestas HTTP**  
* **\[ \] Sin ambigüedades**  
* **\[ \] Sin mencionar Java, Spring Boot, PostgreSQL, Docker**

**✅ Formato Profesional:**

* **\[ \] Numeración consistente (RF-XXX, RN-XXX)**  
* **\[ \] Tablas bien formateadas**  
* **\[ \] Jerarquía clara con \#\# y \#\#\#**

---

## **Restricciones**

**❌ NO incluir:**

* **Tecnologías de implementación (Java, Spring Boot, PostgreSQL, Docker)**  
* **Arquitectura de software (capas, patrones)**  
* **Código fuente**

**✅ SÍ incluir:**

* **QUÉ debe hacer el sistema**  
* **CUÁNDO debe hacerlo**  
* **CON QUÉ datos trabaja**  
* **CÓMO se validan los criterios**

---

## **Entregable**

**Archivo: `REQUERIMIENTOS-FUNCIONALES.md`**  
**Ubicación: Raíz del proyecto o carpeta `docs/`**  
**Longitud esperada: 50-70 páginas (12,000-15,000 palabras)**

**Este documento será la entrada para:**

* **PROMPT 2: Diseño de Arquitectura**  
* **Validación por stakeholders**  
* **Base contractual para desarrollo**

---

**IMPORTANTE: Usa el ejemplo RF-001 como plantilla exacta. Mantén el mismo nivel de detalle, estructura y profesionalismo para todos los RF.**

