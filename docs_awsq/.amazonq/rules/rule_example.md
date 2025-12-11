# Rule #1: Simplicidad Verificable con el "Test de los 3 Minutos"

**Proyecto:** Sistema Ticketero  
**Versión:** 1.0  
**Categoría:** Arquitectura - Diseño de Diagramas

---

## 📋 Definición

> **"Si un diagrama o componente no se puede explicar en 3 minutos a un desarrollador nuevo, está sobre-diseñado"**

Esta regla garantiza que la arquitectura sea comprensible, mantenible y que no agregue complejidad innecesaria al proyecto.

---

## 🎯 Objetivo

- Mantener documentación visual concisa y útil
- Evitar sobre-ingeniería en diagramas
- Facilitar onboarding de nuevos desarrolladores
- Reducir deuda técnica de documentación

---

## ✅ Diagramas Permitidos (Core - Máximo 3)

### 1. Diagrama de Contexto (C4 Level 1)

**Propósito:** Mostrar el sistema en su entorno  
**Elementos máximos:** 5  
**Contenido:**
- Sistema Ticketero (centro)
- Actores externos (Usuario, Ejecutivo)
- Sistemas externos (Telegram API)
- Flujos principales de datos

**Prohibido:**
- Detalles de implementación interna
- Tecnologías específicas
- Componentes internos del sistema

**Ejemplo estructura:**
```
[Usuario] ──(crea ticket)──> [Sistema Ticketero] ──(notifica)──> [Telegram API]
                                     |
                            [Ejecutivo] ──(atiende)──>
```

---

### 2. Diagrama de Secuencia End-to-End

**Propósito:** Flujo completo del happy path  
**Interacciones máximas:** 8-10  
**Contenido:**
- Actores principales
- Componentes del sistema (Controller → Service → Repository → DB)
- Flujo de creación y notificación de ticket
- Solo happy path

**Prohibido:**
- Sub-flujos opcionales en diagrama principal
- Manejo de excepciones detallado
- Loops complejos
- Más de 2 niveles de profundidad

**Estructura requerida:**
```
[Controller] → [Service] → [Repository] → [DB]
                   ↓
            [TelegramService] → [Telegram API]
```

---

### 3. Diagrama Entidad-Relación (ER)

**Propósito:** Modelo de datos core  
**Tablas máximas:** 5 (para MVP)  
**Contenido:**
- Entidades del dominio (ticket, mensaje)
- Relaciones principales (1:N)
- Campos clave (PK, FK, campos de negocio)
- Índices principales

**Prohibido:**
- Tablas técnicas (audit, config, logs)
- Relaciones N:M en MVP
- Todos los atributos (solo los esenciales)
- Constraints complejos en diagrama

**Ejemplo:**
```
ticket (1) ──< (N) mensaje
  - id
  - codigo_referencia
  - status
  - national_id
```

---

## ❌ Diagramas Prohibidos (Over-Engineering)

| Diagrama | Razón para NO usarlo |
|----------|---------------------|
| **Diagrama de Clases Completo** | El código ES la documentación. Genera falsa sensación de diseño terminado |
| **Diagrama de Componentes Detallado** | Innecesario cuando tienes estructura de paquetes clara |
| **Diagrama de Deployment Multi-Servicio** | Para >5 servicios. MVP usa Docker Compose simple |
| **State Machine para Flujos Simples** | Solo si hay >5 estados con transiciones complejas |
| **Activity Diagram** | Duplica información del Sequence Diagram |
| **Use Case Diagram** | Backlog/User Stories son más efectivos |
| **Package Diagram Exhaustivo** | Estructura de carpetas ya lo documenta |

---

## 🧪 Test de los 3 Minutos - Checklist

Antes de crear un diagrama, responde:

### Pregunta 1: Valor
**¿Este diagrama comunica el 80% del valor de la información?**
- ✅ Sí → Continuar
- ❌ No → No lo hagas

### Pregunta 2: Claridad
**¿Puedo explicarlo sin leer documentación adicional?**
- ✅ Sí → Continuar
- ❌ No → Simplificar

### Pregunta 3: Necesidad
**¿El código puede explicarse mejor sin este diagrama?**
- ✅ Sí → NO crear el diagrama
- ❌ No → Crear el diagrama

### Pregunta 4: Elementos
**¿Tiene menos de 10 elementos principales?**
- ✅ Sí → Continuar
- ❌ No → Dividir o simplificar

---

## 📏 Límites Cuantitativos

| Aspecto | Límite | Razón |
|---------|--------|-------|
| **Diagramas totales** | 3 | Principio 80/20 |
| **Elementos por diagrama** | 5-10 | Comprensión en 3 minutos |
| **Niveles de profundidad** | 2 | Evitar complejidad cognitiva |
| **Líneas de conexión** | 8-12 | Claridad visual |
| **Swim lanes (secuencia)** | 4-5 | Foco en flujo principal |

---

## 🎓 Para Entrenamientos

### Ejercicio Práctico

**Dado este diagrama sobrecargado:**
```
[Sistema con 15 componentes, 25 relaciones, 3 niveles de profundidad]
```

**Aplica Rule #1:**
1. Identifica el 20% de elementos que dan el 80% del valor
2. Elimina detalles de implementación
3. Agrupa componentes relacionados
4. Valida que sea explicable en 3 minutos

**Resultado esperado:**
```
[Sistema simplificado con 5 componentes principales, flujo claro]
```

---

## ✅ Validación Continua

### En Code Reviews

```bash
# Checklist para aprobar cambios en diagramas
□ ¿Cumple límite de elementos?
□ ¿Aporta valor que el código no da?
□ ¿Está actualizado con implementación?
□ ¿Se explica en <3 minutos?
```

### En Retrospectivas

```
Pregunta: "¿Los diagramas actuales nos ayudan o confunden?"
Acción: Si confunden → Simplificar o eliminar
```

---

## 🚨 Señales de Violación

**Indicadores de que estás violando Rule #1:**

- ⚠️ Necesitas >5 minutos para explicar un diagrama
- ⚠️ Desarrolladores preguntan "¿dónde está esto en el código?"
- ⚠️ Diagramas desactualizados vs código real
- ⚠️ Múltiples diagramas mostrando la misma información
- ⚠️ Nadie usa los diagramas en daily meetings
- ⚠️ Onboarding requiere "leer todos los diagramas primero"

**Acción correctiva:** Eliminar o simplificar hasta cumplir el test de 3 minutos.

---

## 💡 Ejemplos Prácticos

### ✅ CORRECTO: Diagrama Simple de Secuencia

```
Usuario → Controller : POST /api/ticket
Controller → Service : crearTicket(request)
Service → Repository : save(ticket)
Repository → DB : INSERT
Service → TelegramService : programarMensajes()
TelegramService → MQ : publish(mensaje)
Service → Controller : TicketResponse
Controller → Usuario : 201 Created
```

**Tiempo de explicación:** ~2 minutos  
**Elementos:** 7  
**Valor comunicado:** 90%

---

### ❌ INCORRECTO: Diagrama Sobrecargado

```
Usuario → Filter → Controller → ValidationAspect → Service 
→ TransactionManager → ServiceImpl → RepositoryProxy 
→ EntityManager → JDBC → ConnectionPool → DB
+ Exception handling paths
+ Retry logic
+ Logging interceptors
+ Cache layers
+ Event publishers
```

**Tiempo de explicación:** ~10 minutos  
**Elementos:** 15+  
**Valor comunicado adicional vs simple:** ~5%  
**Complejidad agregada:** 300%

---

## 🎯 Regla de Oro

> **"Si Amazon Q necesita más de 3 minutos para entender tu diagrama y generar código acorde, el diagrama está mal diseñado"**

---

## 📚 Referencias

- **C4 Model:** https://c4model.com/ (usar solo Level 1 y 2)
- **UML Distilled - Martin Fowler:** Énfasis en simplicidad
- **The Pragmatic Programmer:** "Good enough software"

---

## 🔄 Actualización de Esta Regla

**Cuándo revisar:**
- Al finalizar cada sprint
- Cuando hay confusión en equipo
- Al agregar nuevo tipo de diagrama

**Quién puede modificar:**
- Arquitecto de proyecto
- Con consenso de equipo

---

**Versión:** 1.0  
**Última actualización:** Diciembre 2024  
**Estado:** Activa