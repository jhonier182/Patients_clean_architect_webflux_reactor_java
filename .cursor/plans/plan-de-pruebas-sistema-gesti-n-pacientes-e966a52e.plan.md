<!-- e966a52e-3818-4ea4-b31a-172ff00df21c 67e21f13-c005-4fc1-b6e7-d902fcd4a37f -->
# Plan de Pruebas - Sistema de Gestión de Pacientes

## Objetivo

Verificar el funcionamiento completo del sistema incluyendo APIs REST, integraciones con servicios externos, exportación de datos y manejo de eventos.

## Pre-requisitos

1. **Servicios activos**:

   - Spring Boot app corriendo en `http://localhost:8080`
   - PostgreSQL en puerto `5433`
   - RabbitMQ en puerto `5672` (gestión web en `http://localhost:15672`)

2. **Herramientas recomendadas**:

   - Postman o similar para pruebas REST
   - cURL para comandos de línea
   - Navegador para verificar descargas Excel
   - Cliente PostgreSQL para verificar datos

## 1. Verificación de Infraestructura

### 1.1 Verificar estado de servicios

- Verificar logs de aplicación: debe mostrar conexión exitosa a PostgreSQL
- Verificar RabbitMQ: acceder a `http://localhost:15672` (guest/guest)
- Verificar colas creadas en RabbitMQ: `patient.created.queue`, `pruebalegoback.*`

### 1.2 Health Check (si existe endpoint)

```
GET http://localhost:8080/actuator/health
```

Esperado: Estado OK de todos los componentes

## 2. Pruebas CRUD Básicas de Pacientes

### 2.1 Crear Paciente (POST /api/patients)

**Request**:

```json
POST http://localhost:8080/api/patients
Content-Type: application/json

{
  "firstName": "Juan",
  "lastName": "Pérez",
  "documentNumber": "12345678",
  "documentType": "CC",
  "birthDate": "1990-05-15",
  "address": "Calle 123 #45-67",
  "phone": "+57 300 1234567",
  "email": "juan.perez@example.com",
  "city": "Bogotá",
  "state": "Cundinamarca"
}
```

**Validar**:

- Status: 200 OK
- Response contiene `id` generado
- `active`: true
- `admissionDate` presente
- `fullName` y `age` calculados correctamente
- Verificar en logs: mensaje "Paciente creado exitosamente"
- Verificar en RabbitMQ: evento `PatientCreated` publicado

**Ejecutar múltiples veces** con diferentes datos para crear varios pacientes de prueba.

### 2.2 Obtener Paciente por ID (GET /api/patients/{id})

**Request**:

```
GET http://localhost:8080/api/patients/{id_del_paso_2.1}
```

**Validar**:

- Status: 200 OK
- Datos coinciden con los enviados en creación
- Campos calculados (`fullName`, `age`) correctos

**Caso de error**:

```
GET http://localhost:8080/api/patients/00000000-0000-0000-0000-000000000000
```

Esperado: 404 o 400 con mensaje de paciente no encontrado

### 2.3 Listar Todos los Pacientes (GET /api/patients)

**Request**:

```
GET http://localhost:8080/api/patients
```

**Validar**:

- Status: 200 OK
- Array de pacientes retornado
- Todos los campos presentes en cada paciente
- Verificar en logs: "Consulta de pacientes completada"

### 2.4 Actualizar Paciente (PUT /api/patients/{id})

**Request**:

```json
PUT http://localhost:8080/api/patients/{id}
Content-Type: application/json

{
  "firstName": "Juan Carlos",
  "lastName": "Pérez García",
  "address": "Calle Nueva 456",
  "phone": "+57 301 9876543",
  "email": "juan.carlos@example.com",
  "city": "Medellín",
  "state": "Antioquia"
}
```

**Validar**:

- Status: 200 OK
- Campos actualizados correctamente
- `id`, `documentNumber`, `documentType`, `birthDate` no cambian
- `admissionDate` no cambia
- Verificar en base de datos: cambios persistidos

**Caso de error - ID inexistente**:

Esperado: 404 o 400 con mensaje apropiado

### 2.5 Eliminar Paciente (DELETE /api/patients/{id})

**Request**:

```
DELETE http://localhost:8080/api/patients/{id}
```

**Validar**:

- Status: 204 No Content
- Verificar en base de datos: registro eliminado
- Verificar en logs: "Paciente eliminado exitosamente"

**Caso de error - ID inexistente**:

Esperado: 404 o 400

## 3. Pruebas de Consultas y Filtros

### 3.1 Listar Pacientes Activos (GET /api/patients/active)

**Preparación**: Crear pacientes activos y desactivados

**Request**:

```
GET http://localhost:8080/api/patients/active
```

**Validar**:

- Status: 200 OK
- Solo pacientes con `active: true`
- Verificar conteo: debe coincidir con pacientes activos en BD

### 3.2 Filtrar por Ciudad (GET /api/patients/city/{city})

**Request**:

```
GET http://localhost:8080/api/patients/city/Bogotá
```

**Validar**:

- Status: 200 OK
- Solo pacientes de la ciudad especificada
- Case-insensitive: probar con "bogotá", "BOGOTÁ", "Bogotá"

### 3.3 Buscar por Documento (GET /api/patients/document/{documentNumber})

**Request**:

```
GET http://localhost:8080/api/patients/document/12345678
```

**Validar**:

- Status: 200 OK
- Paciente con documento específico retornado
- Múltiples resultados si hay duplicados (si aplica)

**Caso sin resultados**:

Esperado: Array vacío o 404 según implementación

### 3.4 Filtrar por Rango de Edad (GET /api/patients/age-range)

**Request**:

```
GET http://localhost:8080/api/patients/age-range?minAge=25&maxAge=35
```

**Validar**:

- Status: 200 OK
- Solo pacientes con edad entre 25 y 35 años
- Verificar que `age` está dentro del rango
- Formato de respuesta: `PatientSummary` con campos específicos

**Casos límite**:

- `minAge=0&maxAge=150`: debería retornar todos
- `minAge=100&maxAge=110`: probablemente sin resultados

### 3.5 Filtrar por Múltiples Ciudades (GET /api/patients/cities)

**Request**:

```
GET http://localhost:8080/api/patients/cities?cities=Bogotá,Medellín,Cali
```

**Validar**:

- Status: 200 OK
- Pacientes de cualquiera de las ciudades especificadas
- Sin duplicados (verificar `distinct` en implementación)
- Formato: Lista completa de `PatientResponse`

## 4. Pruebas de Validación

### 4.1 Validación de Campos Requeridos

**Request inválido**:

```json
POST http://localhost:8080/api/patients
{
  "firstName": "",
  "lastName": "Pérez"
}
```

**Validar**:

- Status: 400 Bad Request
- Mensajes de validación específicos para cada campo faltante
- Campos requeridos según `PatientRequest`: `firstName`, `lastName`, `documentNumber`, `documentType`, `birthDate`, `city`, `state`

### 4.2 Validación de Email

**Request**:

```json
POST http://localhost:8080/api/patients
{
  "email": "email-invalido"
}
```

**Validar**:

- Status: 400 Bad Request
- Mensaje: "El email debe tener un formato válido"

### 4.3 Validación de Fecha de Nacimiento

**Request**:

```json
POST http://localhost:8080/api/patients
{
  "birthDate": "2025-12-31"
}
```

**Validar**:

- Status: 400 Bad Request
- Mensaje: "La fecha de nacimiento debe ser en el pasado"

## 5. Pruebas de Gestión de Estado

### 5.1 Desactivar Paciente (PUT /api/patients/{id}/deactivate)

**Preparación**: Crear paciente activo

**Request**:

```
PUT http://localhost:8080/api/patients/{id}/deactivate
```

**Validar**:

- Status: 200 OK
- `active: false` en respuesta
- Verificar en BD: campo `active` actualizado
- No aparece en `/api/patients/active`

**Caso de error - Ya desactivado**:

Esperado: Error de negocio apropiado

### 5.2 Reactivar Paciente (PUT /api/patients/{id}/reactivate)

**Request**:

```
PUT http://localhost:8080/api/patients/{id}/reactivate
```

**Validar**:

- Status: 200 OK
- `active: true` en respuesta
- Aparece en `/api/patients/active`

**Caso de error - Ya activo**:

Esperado: Error de negocio apropiado

## 6. Pruebas de Integración con Weather API

### 6.1 Obtener Clima del Paciente (GET /api/patients/{id}/weather)

**Preparación**: Paciente con `city` y `state` válidos (ej: "Denver", "CO")

**Request**:

```
GET http://localhost:8080/api/patients/{id}/weather
```

**Validar**:

- Status: 200 OK
- Estructura `PatientWithWeather`:
  - Datos del paciente completos
  - Objeto `weather` con: `city`, `state`, `temperature`, `condition`, `humidity`, `windSpeed`, `description`
- Si Weather API falla: debe usar datos por defecto (graceful degradation)
- Verificar logs: intento de conexión a Weather API

**Casos**:

- Paciente con ciudad/estado válidos: datos reales o fallback
- Paciente sin ciudad/estado: datos por defecto
- ID inexistente: 404

## 7. Pruebas de Exportación a Excel

### 7.1 Exportar Todos los Pacientes (GET /api/patients/export/excel)

**Request**:

```
GET http://localhost:8080/api/patients/export/excel
```

**Validar**:

- Status: 200 OK
- Content-Type: `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`
- Header `Content-Disposition: attachment; filename=patients.xlsx`
- Descargar archivo y verificar:
  - Archivo Excel válido (extensión .xlsx)
  - Columnas correctas: ID, nombres, documento, edad, ciudad, etc.
  - Todos los pacientes incluidos
  - Datos correctos en cada fila

### 7.2 Exportar Pacientes Activos (GET /api/patients/export/excel/active)

**Request**:

```
GET http://localhost:8080/api/patients/export/excel/active
```

**Validar**:

- Status: 200 OK
- Filename: `active_patients.xlsx`
- Solo pacientes activos en el archivo
- Estructura correcta del Excel

## 8. Pruebas de Eventos RabbitMQ

### 8.1 Verificar Publicación de Eventos

**Preparación**:

1. Crear nuevo paciente
2. Verificar en RabbitMQ Management UI (`http://localhost:15672`)

**Validar**:

- Cola `patient.created.queue` recibe mensaje
- Exchange `patient.events` tiene binding correcto
- Contenido del mensaje: datos del paciente creado
- Logs de aplicación: "Evento PatientCreated publicado"

### 8.2 Verificar Consumo de Eventos

**Validar**:

- Listener `PatientEventListener` registrado en logs
- Consumer activo en RabbitMQ UI
- Al publicar evento: procesamiento visible en logs

**Nota**: Si hay procesamiento automático de eventos, verificar comportamiento esperado

## 9. Pruebas de Rendimiento y Límites

### 9.1 Carga de Datos

- Crear 100+ pacientes y verificar:
  - Tiempo de respuesta de listado
  - Paginación (si existe)
  - Memoria/recursos

### 9.2 Exportación con Muchos Datos

- Exportar con 1000+ pacientes
- Verificar: tiempo de generación, tamaño de archivo, completitud

## 10. Pruebas de Endpoints de Tareas (Bonus)

### 10.1 Listar Tareas (GET /task)

```
GET http://localhost:8080/task
```

### 10.2 Obtener Tarea con Detalles (GET /task/{id})

```
GET http://localhost:8080/task/{id}
```

**Validar**: Estructura `TaskWithUser` con datos de tarea y usuario

## 11. Verificación en Base de Datos

### 11.1 Consultas SQL Directas

Conectar a PostgreSQL y ejecutar:

```sql
-- Verificar estructura
\d patients

-- Contar registros
SELECT COUNT(*) FROM patients;

-- Verificar datos de paciente específico
SELECT * FROM patients WHERE id = '{id}';

-- Verificar pacientes activos
SELECT COUNT(*) FROM patients WHERE active = true;

-- Verificar por ciudad
SELECT city, COUNT(*) FROM patients GROUP BY city;
```

## 12. Checklist Final

- [ ] Todos los endpoints CRUD funcionan
- [ ] Validaciones de entrada funcionan
- [ ] Filtros y búsquedas retornan resultados correctos
- [ ] Gestión de estado (activar/desactivar) funciona
- [ ] Integración con Weather API funciona o tiene fallback
- [ ] Exportación a Excel genera archivos válidos
- [ ] Eventos se publican en RabbitMQ
- [ ] Manejo de errores es apropiado (404, 400, 500)
- [ ] Logs contienen información útil
- [ ] Datos persisten correctamente en PostgreSQL
- [ ] Cálculos derivados (edad, nombre completo) son correctos

## Notas Adicionales

- Documentar tiempos de respuesta para endpoints críticos
- Capturar screenshots de RabbitMQ UI para documentación
- Guardar archivos Excel generados para verificación
- Registrar cualquier comportamiento inesperado o mejoras sugeridas

### To-dos

- [ ] Verificar servicios activos (Spring Boot, PostgreSQL, RabbitMQ) y acceso a interfaces de gestión
- [ ] Probar operaciones CRUD básicas: crear, leer, actualizar y eliminar pacientes
- [ ] Probar todas las consultas y filtros: activos, por ciudad, por documento, por edad, múltiples ciudades
- [ ] Verificar validaciones de entrada: campos requeridos, formato de email, fecha de nacimiento
- [ ] Probar gestión de estado: desactivar y reactivar pacientes
- [ ] Probar integración con Weather API y manejo de fallbacks
- [ ] Probar exportación a Excel: todos los pacientes y solo activos, verificar estructura de archivos
- [ ] Verificar publicación y consumo de eventos en RabbitMQ usando la UI de gestión
- [ ] Probar casos de error: IDs inexistentes, datos inválidos, validaciones fallidas
- [ ] Verificar persistencia correcta de datos ejecutando consultas SQL directas en PostgreSQL