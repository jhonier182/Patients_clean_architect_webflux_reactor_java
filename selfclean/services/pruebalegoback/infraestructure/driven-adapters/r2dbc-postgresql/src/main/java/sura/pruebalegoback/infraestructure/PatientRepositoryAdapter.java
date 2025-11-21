package sura.pruebalegoback.infraestructure;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class PatientRepositoryAdapter implements PatientRepository {
    
    private final PatientReactiveRepository reactiveRepository;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;

    @Override
    public Mono<Patient> save(Patient patient) {
        log.debug("Guardando paciente: {}", patient.getId());
        PatientEntity entity = toEntity(patient);
        
        // Verificar si el paciente existe en la base de datos
        return reactiveRepository.findById(patient.getId())
            .hasElement()
            .flatMap(exists -> {
                if (exists) {
                    // Si existe, actualizar usando el repositorio
                    log.debug("Paciente existente, actualizando: {}", patient.getId());
                    return reactiveRepository.save(entity)
                        .map(this::toDomain);
                } else {
                    // Si no existe, insertar usando R2dbcEntityTemplate
                    log.debug("Paciente nuevo, insertando: {}", patient.getId());
                    return r2dbcEntityTemplate.insert(PatientEntity.class)
                        .using(entity)
                        .map(this::toDomain);
                }
            })
            .doOnNext(p -> log.debug("Paciente guardado exitosamente: {}", p.getId()));
    }

    @Override
    public Mono<Patient> findById(String id) {
        log.debug("Buscando paciente por ID: {}", id);
        return reactiveRepository.findById(id)
            .map(this::toDomain)
            .doOnNext(p -> log.debug("Paciente encontrado: {}", p.getId()));
    }

    @Override
    public Flux<Patient> findAll() {
        log.debug("Buscando todos los pacientes");
        return reactiveRepository.findAll()
            .map(this::toDomain)
            .doOnNext(p -> log.debug("Paciente recuperado: {}", p.getId()));
    }

    @Override
    public Flux<Patient> findByActive(boolean active) {
        log.debug("Buscando pacientes activos: {}", active);
        return reactiveRepository.findByActive(active)
            .map(this::toDomain)
            .doOnNext(p -> log.debug("Paciente activo encontrado: {}", p.getId()));
    }

    @Override
    public Flux<Patient> findByDocumentNumber(String documentNumber) {
        log.debug("Buscando paciente por documento: {}", documentNumber);
        return reactiveRepository.findByDocumentNumber(documentNumber)
            .map(this::toDomain)
            .doOnNext(p -> log.debug("Paciente con documento encontrado: {}", p.getId()));
    }

    @Override
    public Flux<Patient> findByCity(String city) {
        log.debug("Buscando pacientes por ciudad: {}", city);
        return reactiveRepository.findByCity(city)
            .map(this::toDomain)
            .doOnNext(p -> log.debug("Paciente de ciudad encontrado: {}", p.getId()));
    }

    @Override
    public Mono<Void> deleteById(String id) {
        log.debug("Eliminando paciente: {}", id);
        return reactiveRepository.deleteById(id)
            .doOnSuccess(v -> log.debug("Paciente eliminado: {}", id));
    }

    @Override
    public Mono<Patient> update(Patient patient) {
        log.debug("Actualizando paciente: {}", patient.getId());
        return reactiveRepository.save(toEntity(patient))
            .map(this::toDomain)
            .doOnNext(p -> log.debug("Paciente actualizado: {}", p.getId()));
    }

    private PatientEntity toEntity(Patient patient) {
        return PatientEntity.builder()
            .id(patient.getId())
            .firstName(patient.getFirstName())
            .lastName(patient.getLastName())
            .documentNumber(patient.getDocumentNumber())
            .documentType(patient.getDocumentType())
            .birthDate(patient.getBirthDate())
            .address(patient.getAddress())
            .phone(patient.getPhone())
            .email(patient.getEmail())
            .city(patient.getCity())
            .state(patient.getState())
            .admissionDate(patient.getAdmissionDate())
            .active(patient.isActive())
            .build();
    }

    private Patient toDomain(PatientEntity entity) {
        return Patient.builder()
            .id(entity.getId())
            .firstName(entity.getFirstName())
            .lastName(entity.getLastName())
            .documentNumber(entity.getDocumentNumber())
            .documentType(entity.getDocumentType())
            .birthDate(entity.getBirthDate())
            .address(entity.getAddress())
            .phone(entity.getPhone())
            .email(entity.getEmail())
            .city(entity.getCity())
            .state(entity.getState())
            .admissionDate(entity.getAdmissionDate())
            .active(entity.getActive())
            .build();
    }
}
