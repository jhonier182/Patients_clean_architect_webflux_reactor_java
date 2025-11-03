package sura.pruebalegoback.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.dto.PatientRequest;
import sura.pruebalegoback.dto.PatientResponse;
import sura.pruebalegoback.dto.PatientUpdateRequest;
import sura.pruebalegoback.usecase.patient.*;


import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    
    private static final Logger log = LoggerFactory.getLogger(PatientController.class);
    
    private final CreatePatientUseCase createPatientUseCase;
    private final GetPatientByIdUseCase getPatientByIdUseCase;
    private final QueryPatientsUseCase queryPatientsUseCase;
    private final UpdatePatientUseCase updatePatientUseCase;
    private final DeletePatientUseCase deletePatientUseCase;
    private final GetPatientWeatherUseCase getPatientWeatherUseCase;
    private final ExportPatientsToExcelUseCase exportPatientsToExcelUseCase;

    @PostMapping
    public Mono<ResponseEntity<PatientResponse>> createPatient(@Valid @RequestBody PatientRequest request) {
        log.info("Creando paciente: {} {}", request.getFirstName(), request.getLastName());
        
        return createPatientUseCase.createPatient(
                request.getFirstName(),
                request.getLastName(),
                request.getDocumentNumber(),
                request.getDocumentType(),
                request.getBirthDate().toString(),
                request.getAddress(),
                request.getPhone(),
                request.getEmail(),
                request.getCity(),
                request.getState()
        )
        .map(PatientResponse::fromDomain)
        .map(ResponseEntity::ok)
        .doOnSuccess(response -> log.info("Paciente creado exitosamente: {}", response.getBody().getId()))
        .doOnError(error -> log.error("Error al crear paciente: {}", error.getMessage(), error));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<PatientResponse>> getPatientById(@PathVariable("id") String id) {
        log.info("Buscando paciente con ID: {}", id);
        
        return getPatientByIdUseCase.execute(id)
                .map(PatientResponse::fromDomain)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Paciente encontrado: {}", response.getBody().getId()))
                .doOnError(error -> log.error("Error al buscar paciente {}: {}", id, error.getMessage(), error));
    }

    @GetMapping
    public Flux<PatientResponse> getAllPatients() {
        log.info("Consultando todos los pacientes");
        
        return queryPatientsUseCase.getAllPatients()
                .map(PatientResponse::fromDomain)
                .doOnNext(patient -> log.debug("Paciente encontrado: {}", patient.getId()))
                .doOnComplete(() -> log.info("Consulta de pacientes completada"));
    }

    @GetMapping("/active")
    public Flux<PatientResponse> getActivePatients() {
        log.info("Consultando pacientes activos");
        
        return queryPatientsUseCase.getActivePatients()
                .map(PatientResponse::fromDomain)
                .doOnNext(patient -> log.debug("Paciente activo encontrado: {}", patient.getId()));
    }

    @GetMapping("/city/{city}")
    public Flux<PatientResponse> getPatientsByCity(@PathVariable("city") String city) {
        log.info("Buscando pacientes de ciudad: {}", city);
        
        return queryPatientsUseCase.getPatientByCity(city)
                .map(PatientResponse::fromDomain)
                .doOnNext(patient -> log.debug("Paciente de ciudad encontrado: {}", patient.getId()));
    }

    @GetMapping("/document/{documentNumber}")
    public Flux<PatientResponse> getPatientsByDocumentNumber(@PathVariable("documentNumber") String documentNumber) {
        log.info("Buscando paciente por documento: {}", documentNumber);
        
        return queryPatientsUseCase.getPatientsByDocumentNumber(documentNumber)
                .map(PatientResponse::fromDomain)
                .doOnNext(patient -> log.debug("Paciente con documento encontrado: {}", patient.getId()));
    }

    @GetMapping("/age-range")
    public Flux<QueryPatientsUseCase.PatientSummary> getPatientsByAgeRange(
            @RequestParam Integer minAge, 
            @RequestParam Integer maxAge) {
        log.info("Consultando pacientes con edad entre {} y {}", minAge, maxAge);
        
        return queryPatientsUseCase.getPatientsWithAge(minAge, maxAge)
                .doOnNext(summary -> log.debug("Paciente filtrado por edad: {}", summary));
    }

    @GetMapping("/cities")
    public Mono<List<PatientResponse>> getPatientsByMultipleCities(@RequestParam List<String> cities) {
        log.info("Consultando pacientes en múltiples ciudades: {}", cities);
        
        return queryPatientsUseCase.getPatientsByMultipleCities(cities)
                .map(patients -> patients.stream()
                        .map(PatientResponse::fromDomain)
                        .toList())
                .doOnNext(patients -> log.info("Total de pacientes encontrados: {}", patients.size()));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<PatientResponse>> updatePatient(
            @PathVariable("id") String id, 
            @Valid @RequestBody PatientUpdateRequest request) {
        log.info("Actualizando paciente con ID: {}", id);
        
        UpdatePatientUseCase.PatientUpdateData updateData = new UpdatePatientUseCase.PatientUpdateData(
                request.getFirstName(),
                request.getLastName(),
                request.getAddress(),
                request.getPhone(),
                request.getEmail(),
                request.getCity(),
                request.getState()
        );
        
        return updatePatientUseCase.updatePatient(id, updateData)
                .map(PatientResponse::fromDomain)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Paciente actualizado exitosamente: {}", response.getBody().getId()))
                .doOnError(error -> log.error("Error al actualizar paciente {}: {}", id, error.getMessage(), error));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deletePatient(@PathVariable("id") String id) {
        log.info("Eliminando paciente con ID: {}", id);
        
        return deletePatientUseCase.deletePatient(id)
                .then(Mono.<ResponseEntity<Void>>just(ResponseEntity.noContent().build()))
                .doOnSuccess(response -> log.info("Paciente eliminado exitosamente: {}", id))
                .doOnError(error -> log.error("Error al eliminar paciente {}: {}", id, error.getMessage(), error));
    }

    @PutMapping("/{id}/deactivate")
    public Mono<ResponseEntity<PatientResponse>> deactivatePatient(@PathVariable("id") String id) {
        log.info("Desactivando paciente con ID: {}", id);
        
        return deletePatientUseCase.deactivatePatient(id)
                .map(PatientResponse::fromDomain)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Paciente desactivado exitosamente: {}", response.getBody().getId()))
                .doOnError(error -> log.error("Error al desactivar paciente {}: {}", id, error.getMessage(), error));
    }

    @PutMapping("/{id}/reactivate")
    public Mono<ResponseEntity<PatientResponse>> reactivatePatient(@PathVariable("id") String id) {
        log.info("Reactivando paciente con ID: {}", id);
        
        return deletePatientUseCase.reactivatePatient(id)
                .map(PatientResponse::fromDomain)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Paciente reactivado exitosamente: {}", response.getBody().getId()))
                .doOnError(error -> log.error("Error al reactivar paciente {}: {}", id, error.getMessage(), error));
    }

    @GetMapping("/{id}/weather")
    public Mono<ResponseEntity<GetPatientWeatherUseCase.PatientWithWeather>> getPatientWeather(@PathVariable("id") String id) {
        log.info("Obteniendo datos de clima para paciente con ID: {}", id);
        
        return getPatientWeatherUseCase.execute(id)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.info("Datos de clima obtenidos para paciente: {}", id))
                .doOnError(error -> log.error("Error al obtener datos de clima para paciente {}: {}", id, error.getMessage(), error));
    }

    @GetMapping(value = "/export/excel", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<byte[]>> exportAllPatientsToExcel() {
        log.info("Exportando todos los pacientes a Excel");
        
        return exportPatientsToExcelUseCase.exportAllPatients()
                .map(bytes -> ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=patients.xlsx")
                        .body(bytes))
                .doOnSuccess(response -> log.info("Exportación a Excel completada"))
                .doOnError(error -> log.error("Error al exportar a Excel: {}", error.getMessage(), error));
    }

    @GetMapping(value = "/export/excel/active", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<byte[]>> exportActivePatientsToExcel() {
        log.info("Exportando pacientes activos a Excel");
        
        return exportPatientsToExcelUseCase.exportActivePatients()
                .map(bytes -> ResponseEntity.ok()
                        .header("Content-Disposition", "attachment; filename=active_patients.xlsx")
                        .body(bytes))
                .doOnSuccess(response -> log.info("Exportación de pacientes activos a Excel completada"))
                .doOnError(error -> log.error("Error al exportar pacientes activos a Excel: {}", error.getMessage(), error));
    }
}
