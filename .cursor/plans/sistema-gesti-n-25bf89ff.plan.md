<!-- 25bf89ff-70a4-489c-a63d-805e70863f16 289e5863-460d-4292-9d3a-0d05e63b5173 -->
# Plan: Sistema de Gestión de Pacientes con WebFlux

## Estructura del Proyecto (Clean Architecture - LEGO SURA)

### 1. Capa de Dominio (domain/)

**domain/model** - Entidades y lógica de negocio:

- `Patient.java`: Entidad principal con datos del paciente (id, nombre, apellido, documento, edad, dirección, teléfono, email, fechaIngreso, activo)
- `PatientFactory.java`: Factory para crear pacientes validando reglas de negocio
- `PatientOperations.java`: Operaciones de dominio sobre pacientes
- `events/PatientCreated.java`: Evento de creación de paciente
- `gateway/PatientRepository.java`: Interface del repositorio
- `gateway/WeatherGateway.java`: Interface para consumo de API externa
- `ex/PatientBusinessException.java`: Excepciones de negocio específicas

**domain/usecase** - Casos de uso:

- `CreatePatientUseCase.java`: Crear paciente (con publicación en RabbitMQ)
- `UpdatePatientUseCase.java`: Actualizar paciente
- `DeletePatientUseCase.java`: Eliminación lógica
- `QueryPatientsUseCase.java`: Consultar pacientes (con filtros usando filter, map)
- `GetPatientByIdUseCase.java`: Obtener por ID (usando switchIfEmpty para manejar no encontrado)
- `ExportPatientsToExcelUseCase.java`: Exportar a Excel (subscribeOn para threads bloqueantes)
- `GetPatientWeatherUseCase.java`: Combinar datos de paciente con clima (usando zip)

### 2. Capa de Infraestructura (infraestructure/)

**driven-adapters/r2dbc-postgresql**:

- `build.gradle`: Dependencias de R2DBC PostgreSQL
- `PatientRepositoryAdapter.java`: Implementación con R2DBC
- `PatientEntity.java`: Entidad JPA/R2DBC
- `PatientReactiveRepository.java`: Interface Spring Data R2DBC
- Configuración de pool de conexiones y mapeos

**driven-adapters/rest-consumer**:

- `build.gradle`: WebClient y Jackson
- `WeatherApiClient.java`: Cliente para API de clima de EE.UU.
- `WeatherResponse.java`: DTOs para mapeo con Jackson
- Configuración de WebClient (timeout, retry, logs)

**driven-adapters/rabbit-publisher**:

- `build.gradle`: reactor-rabbitmq
- `RabbitMQPublisher.java`: Publicador reactivo de eventos
- `RabbitMQConfig.java`: Configuración de conexión

**entry-points/rabbit-listener**:

- `PatientEventListener.java`: Suscriptor con ACK manual y reintentos
- Manejo de errores con onErrorResume

**entry-points/reactive-web**:

- `PatientController.java`: API REST con todos los endpoints
- DTOs de request/response
- `GlobalErrorHandler.java`: ControllerAdvice para manejo de errores
- Validaciones con Bean Validation

**helpers/excel-exporter**:

- `build.gradle`: Apache POI (5.2.x)
- `ExcelExportService.java`: Servicio con subscribeOn(Schedulers.boundedElastic())

### 3. Capa de Aplicación (applications/app-service)

**application.yml**:

```yaml
spring:
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/clinica_db
    username: postgres
    password: postgres
  rabbitmq:
    host: localhost
    port: 5672
weather:
  api:
    base-url: https://api.weather.gov
logging:
  level:
    sura.pruebalegoback: DEBUG
    org.springframework.r2dbc: DEBUG
```

## Aspectos Técnicos Clave

### Operadores Reactivos a Usar

- **map**: Transformar Patient a DTO
- **flatMap**: Operaciones asíncronas (guardar en BD, publicar evento)
- **filter**: Filtrar pacientes por criterios
- **switchIfEmpty**: Retornar error 404 cuando paciente no existe
- **Mono.just**: Crear Mono de valores constantes
- **Mono.create/Mono.defer**: Exportación Excel, manejo lazy
- **Mono.zip/Flux.zip**: Combinar datos de paciente + clima
- **doOnError**: Logging de errores
- **subscribeOn**: Apache POI en thread pool bloqueante

### Manejo de Errores

1. **onErrorResume**: Recuperarse con valores por defecto o flujo alternativo
2. **onErrorReturn**: Retornar valor por defecto en caso de error
3. **ControllerAdvice**: Capturar excepciones globalmente
4. **Errores de negocio**: PatientBusinessException con códigos específicos
5. **Errores técnicos**: ApplicationException para fallos de infraestructura

### RabbitMQ Reactivo

- Usar `reactor-rabbitmq` (ya está en el proyecto)
- ACK manual: `channel.basicAck()` en Mono
- Reintentos: `retry()` con backoff exponencial
- Dead Letter Queue para mensajes fallidos

### WebClient + Jackson

- Configurar ObjectMapper personalizado
- `@JsonProperty` para mapeo de campos
- Try-catch en mapeo: `.onErrorResume(JsonProcessingException.class, ...)`
- Logs de request/response en DEBUG

### Logs (Logback)

- **ERROR**: Excepciones críticas
- **WARN**: Validaciones fallidas, reintentos
- **INFO**: Operaciones de negocio exitosas
- **DEBUG**: Request/Response de APIs, queries SQL
- Configurar `logback-spring.xml`

### Pruebas

**Unitarias**:

- Mocks de repositorios y gateways
- StepVerifier para Mono/Flux
- Cobertura > 80%

**Integración**:

- @SpringBootTest con TestContainers (PostgreSQL, RabbitMQ)
- WebTestClient para endpoints

**Carga**:

- JMeter o Gatling (configuración básica)

## Endpoints API

```
POST   /api/patients                    - Crear paciente
GET    /api/patients                    - Listar todos (con filtros query params)
GET    /api/patients/{id}               - Obtener por ID
PUT    /api/patients/{id}               - Actualizar
DELETE /api/patients/{id}               - Eliminar (lógico)
GET    /api/patients/export/excel       - Exportar a Excel (retorna archivo)
GET    /api/patients/{id}/weather       - Paciente + clima por ubicación
```

## Dependencias Gradle Nuevas

```gradle
// R2DBC PostgreSQL
implementation 'org.springframework.boot:spring-boot-starter-data-r2dbc'
implementation 'org.postgresql:r2dbc-postgresql'

// Apache POI
implementation 'org.apache.poi:poi-ooxml:5.2.5'

// Jackson
implementation 'com.fasterxml.jackson.core:jackson-databind'

// Validation
implementation 'org.springframework.boot:spring-boot-starter-validation'
```

## Orden de Implementación

1. ✅ Modelo de dominio (Patient, factory, gateways)
2. ✅ Dependencias gradle actualizadas
3. ✅ Configuración application.yml
4. Crear WeatherGateway en domain/model
5. Casos de uso (Create, Query, GetById, Update, Delete, Export, Weather)
6. Adaptador R2DBC + configuración PostgreSQL
7. API REST + ControllerAdvice
8. RabbitMQ publisher + listener
9. WebClient para clima + caso de uso zip
10. Exportación Excel con subscribeOn
11. Logs y manejo de errores completo
12. Pruebas unitarias y de integración
13. SonarLint local + coverage

## Restricciones

- **NUNCA usar .block()** - Todo debe ser reactivo
- Threads bloqueantes solo con subscribeOn(Schedulers.boundedElastic())
- ACK manual en RabbitMQ
- Clean Architecture estricta (3 capas)
- Usar MIIA para revisión de código por pares

## Progreso Actual

### ✅ Completado

- [x] Entidades de dominio: Patient, PatientFactory, PatientCreated, PatientBusinessException
- [x] Gateway PatientRepository
- [x] Dependencias gradle (R2DBC, POI, Jackson, Validation)
- [x] Configuración application.yml (R2DBC, RabbitMQ, Weather API, Logs)

### 🔄 En Progreso

- [ ] Crear WeatherGateway en domain/model/patient/gateway

### ⏳ Pendiente

- [ ] Implementar casos de uso (Create, Update, Delete, Query, GetById, Export, Weather)
- [ ] Crear adaptador R2DBC PostgreSQL con entidades y repositorio reactivo
- [ ] Implementar WebClient para API de clima con Jackson mappers
- [ ] Crear publicador reactivo RabbitMQ para eventos de pacientes
- [ ] Implementar listener con ACK manual y reintentos
- [ ] Crear servicio de exportación Excel con Apache POI y subscribeOn
- [ ] Implementar controladores REST y DTOs
- [ ] Configurar ControllerAdvice y manejo de errores (onErrorResume, onErrorReturn)
- [ ] Configurar logback-spring.xml con niveles apropiados
- [ ] Escribir pruebas unitarias con StepVerifier y Mockito
- [ ] Crear pruebas de integración con TestContainers

### To-dos

- [x] Crear entidades de dominio (Patient, PatientFactory, events, gateways, excepciones)
- [x] Implementar casos de uso (Create, Update, Delete, Query, GetById, Export, Weather)
- [ ] Crear adaptador R2DBC PostgreSQL con entidades y repositorio reactivo
- [ ] Implementar WebClient para API de clima con Jackson mappers
- [ ] Crear publicador reactivo RabbitMQ para eventos de pacientes
- [ ] Implementar listener con ACK manual y reintentos
- [ ] Crear servicio de exportación Excel con Apache POI y subscribeOn
- [ ] Implementar controladores REST y DTOs
- [ ] Configurar ControllerAdvice y manejo de errores (onErrorResume, onErrorReturn)
- [ ] Configurar logback-spring.xml con niveles apropiados
- [ ] Actualizar application.yml con configuraciones de R2DBC, RabbitMQ, Weather API
- [ ] Escribir pruebas unitarias con StepVerifier y Mockito
- [ ] Crear pruebas de integración con TestContainers
- [ ] Agregar dependencias de R2DBC, Apache POI, Jackson, Validation