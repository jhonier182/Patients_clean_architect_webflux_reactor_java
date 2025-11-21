# Service Base

# Sistema de Gestión de Pacientes - LEGO Backend

## 📋 Descripción del Proyecto

Este proyecto es un sistema de gestión de pacientes desarrollado con **Spring Boot WebFlux** siguiendo los principios de **Arquitectura Hexagonal (o Clean Architecture)** (patrón LEGO). El sistema proporciona una API REST reactiva completa para la gestión de pacientes de una clínica, incluyendo funcionalidades avanzadas como integración con servicios externos, exportación de datos y mensajería asíncrona.

### Características Principales

- ✅ **Arquitectura Reactiva**: Implementación completa con Spring WebFlux y Project Reactor
- ✅ **Arquitectura Hexagonal / Clean Architecture**: Separación clara de capas (Dominio, Aplicación, Infraestructura)
- ✅ **Base de Datos Reactiva**: PostgreSQL con R2DBC para operaciones no bloqueantes
- ✅ **Mensajería Asíncrona**: Integración con RabbitMQ para eventos de dominio
- ✅ **Integración con APIs Externas**: Consumo de API de clima de EE.UU. mediante WebClient
- ✅ **Exportación de Datos**: Generación de reportes en Excel con Apache POI
- ✅ **Gestión de Tareas**: Sistema adicional de gestión de tareas (bonus)

---

## 🏗️ Arquitectura del Proyecto

El proyecto sigue el patrón **Arquitectura Hexagonal (Clean Architecture, LEGO)** con las siguientes capas:

### 1. Capa de Dominio (`domain/`)

**Responsabilidad**: Contiene la lógica de negocio pura, independiente de frameworks.

#### Modelos de Dominio (`domain/model/`)

- **Patient**: Entidad principal del paciente con validaciones de negocio
- **PatientFactory**: Factory para crear pacientes validando reglas de negocio
- **PatientOperations**: Operaciones de dominio sobre pacientes
- **TaskToDo**: Entidad para gestión de tareas
- **Eventos de Dominio**: `PatientCreated` para eventos de creación

#### Gateways (`domain/model/.../gateway/`)

- `PatientRepository`: Interface para persistencia de pacientes
- `WeatherGateway`: Interface para consumo de API de clima
- `TaskToDoRepository`: Interface para gestión de tareas
- `UserGateway` y `UserScoreGateway`: Interfaces para gestión de usuarios

#### Casos de Uso (`domain/usecase/`)

**Gestión de Pacientes:**
- `CreatePatientUseCase`: Crear paciente con publicación de eventos
- `UpdatePatientUseCase`: Actualizar información de paciente
- `DeletePatientUseCase`: Eliminación lógica de pacientes
- `GetPatientByIdUseCase`: Obtener paciente por ID
- `QueryPatientsUseCase`: Consultar pacientes con múltiples filtros
- `GetPatientWeatherUseCase`: Combinar datos de paciente con información del clima
- `ExportPatientsToExcelUseCase`: Exportar pacientes a formato Excel

**Gestión de Tareas:**
- `CreateTasksUseCase`: Crear nuevas tareas
- `AssignTasksUseCase`: Asignar tareas a usuarios
- `CompleteTasksUseCase`: Completar tareas y actualizar puntuación
- `ReAssignUserTasksUseCase`: Reasignar tareas de usuario
- `QueryTasksUseCase`: Consultar tareas con detalles

### 2. Capa de Aplicación (`applications/app-service/`)

**Responsabilidad**: Configuración y orquestación de la aplicación.

- `MainApplication`: Punto de entrada de la aplicación Spring Boot
- `UseCaseConfig`: Configuración de beans para casos de uso
- `application.yml`: Configuración de la aplicación (BD, RabbitMQ, APIs externas)

### 3. Capa de Infraestructura (`infraestructure/`)

**Responsabilidad**: Implementaciones concretas de adaptadores y puntos de entrada.

#### Adaptadores Conducidos (`driven-adapters/`)

**R2DBC PostgreSQL** (`r2dbc-postgresql/`):
- `PatientRepositoryAdapter`: Implementación reactiva del repositorio
- `PatientEntity`: Entidad de persistencia
- `PatientReactiveRepository`: Interface Spring Data R2DBC
- Configuración de pool de conexiones reactivo

**REST Consumer** (`rest-consumer/`):
- `WeatherApiClient`: Cliente WebClient para API de clima
- `WeatherResponse`: DTOs para mapeo de respuestas
- Configuración de timeouts, retry y manejo de errores

**RabbitMQ Publisher** (`rabbit-publisher/`):
- Configuración de publicación de eventos
- Integración con reactor-rabbitmq

#### Puntos de Entrada (`entry-points/`)

**Reactive Web** (`reactive-web/`):
- `PatientController`: API REST completa para gestión de pacientes
- `CreateTasksService`, `AssignTasksService`, `CompleteTasksService`, `QueryTaskServices`: Endpoints para gestión de tareas
- `GlobalExceptionHandler`: Manejo centralizado de excepciones con `@ControllerAdvice`
- DTOs: `PatientRequest`, `PatientResponse`, `PatientUpdateRequest`

**Event Subscribers** (`subs-events/`):
- `PatientEventListener`: Suscriptor de eventos de RabbitMQ
- `RabbitMQReceiverConfig`: Configuración del receiver
- Manejo de ACK manual y reintentos

#### Helpers (`helpers/`)

**Excel Exporter** (`excel-exporter/`):
- `ExcelExportService`: Servicio para exportación a Excel con Apache POI
- Uso de `subscribeOn(Schedulers.boundedElastic())` para operaciones bloqueantes

---

## 🚀 Tecnologías Utilizadas

### Framework y Librerías Core

- **Spring Boot 3.3.1**: Framework principal
- **Spring WebFlux**: Stack reactivo de Spring
- **Project Reactor**: Programación reactiva (Mono/Flux)
- **Java 21**: Versión de Java utilizada

### Base de Datos

- **PostgreSQL 15**: Base de datos relacional
- **R2DBC**: Driver reactivo para PostgreSQL
- **Spring Data R2DBC**: Abstracción para acceso a datos reactivo

### Mensajería

- **RabbitMQ 3**: Broker de mensajería
- **reactor-rabbitmq**: Cliente reactivo para RabbitMQ

### Integraciones Externas

- **WebClient**: Cliente HTTP reactivo para consumo de APIs
- **Jackson**: Serialización/deserialización JSON
- **Apache POI 5.2.5**: Generación de archivos Excel

### Herramientas de Desarrollo

- **Gradle**: Sistema de construcción
- **Lombok**: Reducción de código boilerplate
- **JUnit 5**: Framework de pruebas
- **JaCoCo**: Cobertura de código

---

## 📡 API REST - Endpoints Disponibles

### Gestión de Pacientes

#### Operaciones CRUD Básicas

```
POST   /api/patients                    - Crear nuevo paciente
GET    /api/patients                    - Listar todos los pacientes
GET    /api/patients/{id}               - Obtener paciente por ID
PUT    /api/patients/{id}               - Actualizar paciente
DELETE /api/patients/{id}               - Eliminar paciente (lógico)
```

#### Consultas y Filtros Avanzados

```
GET    /api/patients/active             - Listar pacientes activos
GET    /api/patients/city/{city}        - Filtrar por ciudad
GET    /api/patients/document/{doc}     - Buscar por número de documento
GET    /api/patients/age-range          - Filtrar por rango de edad (query params: minAge, maxAge)
GET    /api/patients/cities             - Filtrar por múltiples ciudades (query param: cities)
```

#### Gestión de Estado

```
PUT    /api/patients/{id}/deactivate    - Desactivar paciente
PUT    /api/patients/{id}/reactivate   - Reactivar paciente
```

#### Funcionalidades Especiales

```
GET    /api/patients/{id}/weather       - Obtener paciente con información del clima
GET    /api/patients/export/excel       - Exportar todos los pacientes a Excel
GET    /api/patients/export/excel/active - Exportar pacientes activos a Excel
```

### Gestión de Tareas

```
POST   /api/task                        - Crear nueva tarea
POST   /api/task/assign                 - Asignar tarea a usuario
POST   /api/task/{id}/complete          - Completar tarea
GET    /api/task/{id}                   - Obtener tarea con detalles
GET    /api/task                        - Listar todas las tareas
```

---

## 🔧 Configuración e Instalación

### Requisitos Previos

- Java 21 o superior
- Gradle 7.x o superior
- Docker y Docker Compose (para servicios de infraestructura)
- PostgreSQL 15
- RabbitMQ 3

### Configuración de Servicios con Docker Compose

El proyecto incluye un archivo `docker-compose.yml` para levantar los servicios necesarios:

```bash
cd selfclean/services/pruebalegoback
docker-compose up -d
```

Esto levantará:
- **PostgreSQL** en el puerto `5433`
- **RabbitMQ** en los puertos `5672` (AMQP) y `15672` (Management UI)

### Configuración de la Aplicación

El archivo `application.yml` contiene la configuración necesaria:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5433/clinica_db
    username: postgres
    password: 1234
  rabbitmq:
    host: localhost
    port: 5672

weather:
  api:
    base-url: https://api.weather.gov
```

### Ejecución de la Aplicación

```bash
# Compilar el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun

# O ejecutar directamente
java -jar applications/app-service/build/libs/app-service.jar
```

La aplicación estará disponible en: `http://localhost:8080`

---

## 🎯 Operadores Reactivos Implementados

El proyecto demuestra el uso correcto de múltiples operadores de Project Reactor:

- **`map`**: Transformación de datos (Patient → DTO)
- **`flatMap`**: Operaciones asíncronas encadenadas (guardar en BD, publicar eventos)
- **`filter`**: Filtrado de pacientes por criterios
- **`switchIfEmpty`**: Manejo de casos cuando no se encuentra un paciente
- **`Mono.just`**: Creación de Mono con valores constantes
- **`Mono.defer`**: Evaluación lazy de operaciones
- **`Mono.create`**: Creación de Mono para operaciones complejas (exportación Excel)
- **`Mono.zip` / `Flux.zip`**: Combinación de múltiples fuentes reactivas (paciente + clima)
- **`doOnError`**: Logging y side effects en caso de errores
- **`onErrorResume`**: Recuperación de errores con flujos alternativos
- **`onErrorReturn`**: Retorno de valores por defecto en errores
- **`subscribeOn`**: Ejecución en thread pools específicos (Apache POI en boundedElastic)

---

## 🛡️ Manejo de Errores

El sistema implementa un manejo robusto de errores:

### Excepciones de Negocio

- `PatientBusinessException`: Errores específicos del dominio de pacientes
- `BusinessException`: Errores de negocio genéricos
- Códigos de error específicos para cada tipo de excepción

### Excepciones Técnicas

- `ApplicationException`: Errores de infraestructura
- Manejo de errores de conexión a APIs externas
- Fallbacks para servicios no disponibles

### Global Exception Handler

- `@ControllerAdvice` para captura centralizada de excepciones
- Mapeo de excepciones a códigos HTTP apropiados
- Mensajes de error estructurados y consistentes

### Validaciones

- Bean Validation (`@Valid`, `@NotNull`, `@Email`, etc.)
- Validaciones de negocio en la capa de dominio
- Manejo de `WebExchangeBindException` para errores de validación

---

## 📊 Logging

El proyecto utiliza **Logback** con configuración estructurada:

- **ERROR**: Excepciones críticas y errores del sistema
- **WARN**: Validaciones fallidas, reintentos de operaciones
- **INFO**: Operaciones de negocio exitosas
- **DEBUG**: Request/Response de APIs, queries SQL, consumo de servicios externos

Configuración en `logback-spring.xml` con niveles específicos por paquete.

---

## 🧪 Pruebas

### Pruebas Unitarias

- Cobertura de casos de uso principales
- Uso de `StepVerifier` para testing reactivo
- Mocks de repositorios y gateways
- Pruebas de operaciones de dominio

### Pruebas de Integración

- Configuración con TestContainers (pendiente de completar)
- Pruebas de endpoints REST con WebTestClient
- Verificación de flujos completos

---

## 📦 Estructura del Proyecto

```
pruebalegoback/
├── applications/
│   └── app-service/          # Configuración y punto de entrada
├── domain/
│   ├── model/                 # Entidades y lógica de negocio
│   └── usecase/               # Casos de uso
├── infraestructure/
│   ├── driven-adapters/       # Adaptadores (BD, APIs, RabbitMQ)
│   ├── entry-points/          # Controladores REST y suscriptores
│   └── helpers/               # Utilidades (Excel, etc.)
├── test/                      # Pruebas
├── build.gradle
├── docker-compose.yml
└── README.md
```

---

## 🔄 Flujos Principales

### Creación de Paciente

1. Cliente envía `POST /api/patients` con datos del paciente
2. `PatientController` recibe la petición y valida el DTO
3. `CreatePatientUseCase` ejecuta la lógica de negocio
4. `PatientFactory` valida y crea la entidad `Patient`
5. `PatientRepositoryAdapter` persiste en PostgreSQL (R2DBC)
6. Se publica evento `PatientCreated` en RabbitMQ
7. Se retorna `PatientResponse` al cliente

### Consulta con Clima

1. Cliente solicita `GET /api/patients/{id}/weather`
2. `GetPatientWeatherUseCase` obtiene el paciente de la BD
3. Se combina con `WeatherGateway` para obtener datos del clima
4. `Mono.zip` combina ambas fuentes reactivas
5. Se retorna respuesta combinada o fallback si el servicio de clima falla

### Exportación a Excel

1. Cliente solicita `GET /api/patients/export/excel`
2. `ExportPatientsToExcelUseCase` obtiene todos los pacientes
3. `ExcelExportService` genera el archivo usando Apache POI
4. Operación bloqueante ejecutada en `Schedulers.boundedElastic()`
5. Se retorna el archivo Excel como stream

---

## 🎓 Principios y Buenas Prácticas Aplicadas

### Arquitectura Hexagonal / Clean Architecture

- ✅ Separación estricta de capas
- ✅ Independencia del dominio de frameworks
- ✅ Inversión de dependencias (interfaces en dominio)
- ✅ Testabilidad mejorada

### Programación Reactiva

- ✅ Sin uso de `.block()` en código de producción
- ✅ Operaciones no bloqueantes en toda la aplicación
- ✅ Uso correcto de schedulers para operaciones bloqueantes
- ✅ Manejo de backpressure implícito

### SOLID

- ✅ Single Responsibility: Cada clase tiene una responsabilidad clara
- ✅ Open/Closed: Extensible mediante interfaces
- ✅ Liskov Substitution: Implementaciones intercambiables
- ✅ Interface Segregation: Interfaces específicas por dominio
- ✅ Dependency Inversion: Dependencias hacia abstracciones

---

## 📝 Notas Técnicas Importantes

### Restricciones Cumplidas

- ✅ **NUNCA usar `.block()`**: Todo el código es reactivo
- ✅ **Threads bloqueantes solo con `subscribeOn`**: Apache POI ejecutado en `boundedElastic()`
- ✅ **ACK manual en RabbitMQ**: Implementado en el listener
- ✅ **Arquitectura Hexagonal / Clean Architecture estricta**: Separación clara de 3 capas
- ✅ **Manejo de errores robusto**: Múltiples estrategias implementadas

### Integraciones Externas

- **Weather API**: Consumo de `https://api.weather.gov` con retry y fallbacks
- **RabbitMQ**: Publicación y consumo de eventos de dominio
- **PostgreSQL**: Persistencia reactiva con R2DBC

---

## 🚧 Mejoras Futuras

- [ ] Completar pruebas de integración con TestContainers
- [ ] Implementar pruebas de carga con JMeter/Gatling
- [ ] Mejorar cobertura de código a >80%
- [ ] Configurar SonarLint para análisis de calidad
- [ ] Implementar métricas con Micrometer
- [ ] Agregar documentación OpenAPI/Swagger

---

## 👥 Autor

Desarrollado siguiendo los estándares y patrones de **LEGO** para arquitectura de software.

---

## 📄 Licencia

Este proyecto es parte de una prueba técnica y está destinado a fines educativos y de evaluación.

---

## 🔗 Recursos Adicionales

- [Spring WebFlux Documentation](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- [Project Reactor Reference](https://projectreactor.io/docs/core/release/reference/)
- [R2DBC Documentation](https://r2dbc.io/)
- [RabbitMQ Reactor Documentation](https://github.com/reactor/reactor-rabbitmq)

---

**Versión**: 1.0.0  
**Última actualización**: 11/2025

