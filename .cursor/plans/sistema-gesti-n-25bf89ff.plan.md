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
- ❌ `RabbitMQPublisher.java`: **FALTA IMPLEMENTAR** (crítico)
- ❌ `RabbitMQConfig.java`: **FALTA IMPLEMENTAR** (crítico)
- ⚠️ Actualmente `EventsGateway` usa implementación fake (solo loguea)

**entry-points/subs-events**:

- `PatientEventListener.java`: Suscriptor con ACK manual y reintentos (⚠️ ACK manual necesita corrección)
- Manejo de errores con onErrorResume
- `RabbitMQReceiverConfig.java`: Configuración del receiver

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
    password: 1234
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

**Endpoints Base:**

```
POST   /api/patients                    - Crear paciente
GET    /api/patients                    - Listar todos los pacientes
GET    /api/patients/{id}               - Obtener por ID
PUT    /api/patients/{id}               - Actualizar
DELETE /api/patients/{id}               - Eliminar (lógico)
```

**Endpoints Adicionales Implementados:**

```
GET    /api/patients/active             - Listar pacientes activos
GET    /api/patients/city/{city}        - Filtrar por ciudad
GET    /api/patients/document/{doc}     - Buscar por documento
GET    /api/patients/age-range          - Filtrar por rango de edad (query params: minAge, maxAge)
GET    /api/patients/cities              - Filtrar por múltiples ciudades (query param: cities)
PUT    /api/patients/{id}/deactivate    - Desactivar paciente
PUT    /api/patients/{id}/reactivate   - Reactivar paciente
GET    /api/patients/{id}/weather       - Paciente + clima por ubicación
GET    /api/patients/export/excel        - Exportar a Excel (retorna archivo)
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
4. ✅ Crear WeatherGateway en domain/model
5. ✅ Casos de uso (Create, Query, GetById, Update, Delete, Export, Weather)
6. ✅ Adaptador R2DBC + configuración PostgreSQL
7. ✅ API REST + ControllerAdvice
8. ⚠️ RabbitMQ publisher + listener (Publisher falta, Listener necesita corrección ACK)
9. ✅ WebClient para clima + caso de uso zip
10. ✅ Exportación Excel con subscribeOn
11. ✅ Logs y manejo de errores completo
12. ⚠️ Pruebas unitarias y de integración (Parciales: 60% unitarias, 0% integración)
13. ❓ SonarLint local + coverage (Verificar configuración)

## Restricciones

- **NUNCA usar .block()** - Todo debe ser reactivo
- Threads bloqueantes solo con subscribeOn(Schedulers.boundedElastic())
- ACK manual en RabbitMQ
- Clean Architecture estricta (3 capas)
- Usar MIIA para revisión de código por pares

## Progreso Actual

### ✅ Completado (100%)

**Capa de Dominio:**

- [x] Entidades de dominio: Patient, PatientFactory, PatientOperations, PatientCreated
- [x] Gateway PatientRepository (interface)
- [x] Gateway WeatherGateway (interface)
- [x] Excepciones: PatientBusinessException, ApplicationException, BusinessException

**Casos de Uso:**

- [x] CreatePatientUseCase (con publicación de eventos)
- [x] UpdatePatientUseCase
- [x] DeletePatientUseCase (eliminación lógica)
- [x] QueryPatientsUseCase (con filtros usando filter, map)
- [x] GetPatientByIdUseCase (usando switchIfEmpty, Mono.defer)
- [x] ExportPatientsToExcelUseCase (con subscribeOn para Apache POI)
- [x] GetPatientWeatherUseCase (usando Mono.zip)

**Infraestructura - Adaptadores:**

- [x] Adaptador R2DBC PostgreSQL completo:
  - PatientRepositoryAdapter
  - PatientEntity
  - PatientReactiveRepository
  - Configuración de pool de conexiones
- [x] WebClient para API de clima:
  - WeatherApiClient implementado
  - Jackson mappers con try-catch
  - Retry con backoff exponencial
  - Timeouts configurados
  - onErrorResume/onErrorReturn para fallbacks
- [x] Exportación Excel:
  - ExcelExportService con Apache POI
  - subscribeOn(Schedulers.boundedElastic()) correctamente implementado

**Infraestructura - Puntos de Entrada:**

- [x] API REST completa:
  - PatientController con todos los endpoints
  - DTOs: PatientRequest, PatientResponse, PatientUpdateRequest
  - Más endpoints que el plan original (active, city, document, age-range, etc.)
- [x] GlobalExceptionHandler:
  - @ControllerAdvice implementado
  - Manejo de PatientBusinessException, BusinessException, ApplicationException
  - Validaciones con WebExchangeBindException
- [x] RabbitMQ Listener:
  - PatientEventListener implementado
  - Lee mensajes y los imprime en consola
  - Reintentos con retryWhen y backoff exponencial
  - ⚠️ **PROBLEMA**: ACK manual incompleto (solo obtiene tag, no ejecuta basicAck)

**Configuración:**

- [x] application.yml completo:
  - R2DBC configurado (PostgreSQL)
  - RabbitMQ configurado
  - Weather API configurado
  - Logs configurados por paquete
- [x] logback-spring.xml configurado
- [x] Dependencias Gradle:
  - R2DBC PostgreSQL
  - Apache POI 5.2.x
  - Jackson
  - Bean Validation
  - reactor-rabbitmq

**Operadores Reactivos Implementados:**

- [x] map: Usado en transformaciones DTOs
- [x] flatMap: Usado en guardado BD, publicación eventos, WebClient
- [x] filter: Usado en QueryPatientsUseCase
- [x] switchIfEmpty: Usado en GetPatientByIdUseCase
- [x] Mono.just: Valores constantes
- [x] Mono.create: Exportación Excel
- [x] Mono.defer: Lazy evaluation en GetPatientByIdUseCase
- [x] Mono.zip: Combinar paciente + clima en GetPatientWeatherUseCase
- [x] doOnError: Logging de errores en múltiples lugares
- [x] subscribeOn: Apache POI en thread pool bloqueante

**Manejo de Errores:**

- [x] onErrorResume: Usado en WeatherApiClient, GetPatientByIdUseCase, GetPatientWeatherUseCase
- [x] onErrorReturn: Usado en WeatherApiClient, GetPatientWeatherUseCase
- [x] @ControllerAdvice: GlobalExceptionHandler completo
- [x] Separación errores negocio/técnicos: PatientBusinessException vs ApplicationException
- [x] Try-catch con Jackson: Implementado en WeatherApiClient con JsonProcessingException

**Logs:**

- [x] Logback configurado con niveles apropiados
- [x] ERROR: Excepciones críticas
- [x] WARN: Reintentos, validaciones fallidas
- [x] INFO: Operaciones de negocio exitosas
- [x] DEBUG: Request/Response APIs, queries SQL, consumo de servicios

**Verificación de .block():**

- [x] ✅ No se usa .block() en código de producción
- [x] ⚠️ Solo 2 ocurrencias en tests (aceptable):
  - test/acceptance/.../ConsumeQueryApp.java
  - domain/model/src/test/.../TaskToDoOperationsTest.java

### ⚠️ Pendiente Crítico

**RabbitMQ Publisher (BLOQUEADOR):**

- [ ] ❌ Implementar RabbitMQPublisher con reactor-rabbitmq
- [ ] ❌ Implementar RabbitMQConfig con configuración de conexión
- [ ] ❌ Crear implementación real de EventsGateway que publique en RabbitMQ
- [ ] ❌ Configurar exchange, routing keys, colas
- **Impacto**: Los eventos de creación de pacientes NO se publican realmente en RabbitMQ
- **Estado actual**: EventsGateway usa implementación fake (solo loguea en DefaultBeansConfig)

**Corrección RabbitMQ Listener:**

- [ ] ⚠️ Corregir ACK manual en PatientEventListener:
  - Actual: Solo obtiene `delivery.getEnvelope().getDeliveryTag()`
  - Debe: Ejecutar `channel.basicAck(deliveryTag, false)`
  - Agregar manejo de NACK con `basicNack` en errores
- **Impacto**: Los mensajes no se confirman correctamente, pueden reprocesarse

### ⏳ Pendiente (Prioridad Media)

**Pruebas:**

- [ ] ⚠️ Completar pruebas unitarias (actualmente ~60%):
  - Algunos casos de uso tienen pruebas (CreatePatientUseCase, GetPatientByIdUseCase)
  - Faltan pruebas para otros casos de uso
  - Faltan pruebas de controladores
  - Faltan pruebas de adaptadores
  - **Meta**: Cobertura > 80%
- [ ] ❌ Pruebas de integración con TestContainers:
  - PostgreSQL container
  - RabbitMQ container
  - Verificar flujo completo: crear → publicar → recibir
  - WebTestClient para endpoints
- [ ] ❌ Pruebas de carga:
  - JMeter o Gatling básico
  - Endpoints principales

**Calidad:**

- [ ] ❓ Configurar SonarLint local:
  - Instalar plugin en IDE
  - Verificar reglas activas
  - Revisar deuda técnica
- [ ] ❓ Configurar cobertura local:
  - Plugin de cobertura en IDE
  - Generar reportes
  - Verificar > 80% cobertura

**MIIA:**

- [ ] ❓ Usar revisor de código por pares de MIIA:
  - Configurar herramienta
  - Realizar al menos una revisión documentada
  - **Requisito obligatorio mencionado en prueba técnica**

### 📊 Estado del Proyecto vs Prueba Técnica

**Cumplimiento Estimado: ~75%**

**Componentes Core:**

- ✅ API con R2DBC PostgreSQL: 100%
- ✅ Operadores Reactor: 100%
- ✅ subscribeOn + Apache POI: 100%
- ✅ WebClient + Jackson: 100%
- ✅ Sin .block(): 95% (solo en tests, aceptable)
- ❌ RabbitMQ Publisher: 0% ← **CRÍTICO**
- ⚠️ RabbitMQ Listener: 70% (ACK mal)
- ✅ Manejo de errores: 100%
- ✅ Logs Logback: 100%
- ✅ Clean Architecture: 100%
- ⚠️ Pruebas: 30% (solo unitarias parciales)
- ❌ Pruebas integración: 0%
- ❌ Pruebas carga: 0%
- ❓ SonarLint: ? (verificar)
- ❓ MIIA: ? (verificar)

## 🚨 Problemas Críticos Identificados

### 1. RabbitMQ Publisher No Implementado

**Ubicación**: `infraestructure/driven-adapters/rabbit-publisher/` (vacío)

**Problema**:

- CreatePatientUseCase intenta publicar eventos pero no hay implementación real
- EventsGateway actual solo loguea (DefaultBeansConfig línea 69-73)
- Los eventos nunca llegan a RabbitMQ

**Solución Requerida**:

1. Crear `RabbitMQConfig.java` con configuración reactiva
2. Crear `RabbitMQPublisher.java` o `RabbitMQEventsGateway.java`
3. Implementar `EventsGateway` que use reactor-rabbitmq
4. Configurar exchange, routing keys, colas

### 2. ACK Manual Incorrecto

**Ubicación**: `PatientEventListener.java` líneas 96-106

**Problema**:

```java
// Código actual (INCORRECTO):
delivery.getEnvelope().getDeliveryTag(); // Solo obtiene, no hace ACK
```

**Solución Requerida**:

```java
// Código correcto:
long deliveryTag = delivery.getEnvelope().getDeliveryTag();
Channel channel = // obtener canal del receiver
channel.basicAck(deliveryTag, false); // Confirmar manualmente
```

### 3. Pruebas Incompletas

- Pruebas unitarias: ~60% (faltan varios casos de uso y controladores)
- Pruebas integración: 0%
- Pruebas carga: 0%

## 🎯 Acciones Requeridas (Orden de Prioridad)

### Prioridad ALTA (Bloqueadores)

1. **Implementar RabbitMQ Publisher**

   - Crear `infraestructure/driven-adapters/rabbit-publisher/src/main/java/.../RabbitMQConfig.java`
   - Crear `infraestructure/driven-adapters/rabbit-publisher/src/main/java/.../RabbitMQEventsGateway.java`
   - Implementar `EventsGateway` con reactor-rabbitmq
   - Configurar exchange "patient.events", routing key "patient.created"

2. **Corregir ACK Manual en Listener**

   - Modificar `PatientEventListener.processMessage()`
   - Ejecutar `channel.basicAck()` correctamente
   - Agregar manejo de NACK para errores

3. **Verificar .block() en Tests**

   - Revisar si las 2 ocurrencias en tests son necesarias
   - Considerar usar StepVerifier en su lugar si es posible

### Prioridad MEDIA

4. **Completar Pruebas Unitarias**

   - ExportPatientsToExcelUseCase
   - GetPatientWeatherUseCase
   - QueryPatientsUseCase (otros métodos)
   - PatientController (endpoints principales)
   - PatientRepositoryAdapter
   - WeatherApiClient

5. **Implementar Pruebas de Integración**

   - TestContainers para PostgreSQL y RabbitMQ
   - Flujo completo: crear paciente → publicar evento → recibir evento
   - WebTestClient para endpoints REST

6. **Configurar SonarLint y Cobertura**

   - Instalar plugins en IDE
   - Ejecutar análisis local
   - Revisar y corregir deuda técnica

### Prioridad BAJA

7. **Implementar Pruebas de Carga**

   - Configuración básica JMeter o Gatling
   - Endpoints principales

8. **Configurar MIIA Revisor**

   - Instalar/configurar herramienta
   - Realizar revisión de código por pares
   - Documentar en README

9. **Optimización de Logs**

   - Revisar niveles DEBUG en producción
   - Ajustar según necesidad real vs plan

## 📝 Notas Adicionales

- **Endpoint adicionales**: El proyecto tiene más endpoints que el plan original (active, city, document, age-range, deactivate/reactivate). Esto es positivo y demuestra buena implementación.

- **Operadores Reactivos**: Todos los operadores requeridos están implementados y usados correctamente en lugares apropiados.

- **Arquitectura**: Clean Architecture (LEGO SURA) está bien implementada con separación clara de capas.

- **Manejo de Errores**: Excelente implementación de onErrorResume, onErrorReturn y ControllerAdvice con separación clara entre errores de negocio y técnicos.

- **Logs**: Configuración correcta con niveles apropiados. El requisito de "no poner logs sino cambiar niveles de configuración" está bien entendido (DEBUG configurado para consumo de servicios).