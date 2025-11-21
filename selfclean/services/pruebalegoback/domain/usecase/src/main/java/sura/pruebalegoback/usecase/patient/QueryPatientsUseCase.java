package sura.pruebalegoback.usecase.patient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

import java.util.List;

@RequiredArgsConstructor
public class QueryPatientsUseCase {

    private static final Logger log = LoggerFactory.getLogger(QueryPatientsUseCase.class);
    private final PatientRepository patientRepository;


    public Flux<Patient> getAllPatients() {
        log.info("consultando todos los pacientes");

        return patientRepository.findAll()
                .doOnNext(patient -> log.debug("Pacientes encontrados: {}", patient.getId()))
                .doOnComplete(() -> log.info("Consulta de pacientes completada"))
                .onErrorResume(error -> {
                    log.error("Error al consultar pacientes", error.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<Patient> getActivePatients(){
        log.info("Consultando pacientes activos");
        return patientRepository.findByActive(true)
                .filter(Patient::isActive)
                .doOnNext(patient -> log.debug("Paciente Activo: {} {} ",
                               patient.getFirstName(), patient.getLastName()))
                .onErrorResume(error ->{
                    log.error("Error al consultar pacientes activos: {}", error.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<Patient>getPatientByCity(String city){
        log.info("Buscando pacientes de ciudad: {}" ,city);
        return patientRepository.findByCity(city)
                .filter(patient -> patient.getCity() != null)
                .filter(patient -> patient.getCity().equalsIgnoreCase(city))
                .map(patient -> {
                    log.debug("Paciente encontrado en : {} {} ", city,patient.getFullName());
                    return patient;
                })
                .onErrorResume(error ->{
                    log.error("Eror al consultar pacientes por ciudad. ", error.getMessage());
                    return Flux.empty();
                });
    }

    public Flux<Patient>getPatientsByDocumentNumber(String document){
        log.info("Buscando paciente por documento");

        return patientRepository.findByDocumentNumber(document)
                .doOnNext(patient -> log.debug("Paciente con documento {}  encontrado: {}",
                       document ,patient.getId()))
                .switchIfEmpty(Flux.defer(() -> {
                   log.warn("No se encontraron pacientes con documento: {}" ,document);
                   return Flux.empty();
        }));
    }

    public Flux<PatientSummary>getPatientsWithAge(Integer minAge, Integer maxAge){
        log.info("Consultando pacientes con edad entre  {} y {} ", minAge, maxAge);

        return patientRepository.findAll()
                .filter(patient -> {
                    int age = patient.getAge();
                    return age >= minAge && age <= maxAge;
                })
                .map(patient -> new PatientSummary(
                        patient.getId(),
                        patient.getFullName(),
                        patient.getAge(),
                        patient.getCity(),
                        patient.isActive()
                ))
                .doOnNext(summary -> log.debug("Paciente filtrado por edad: {}", summary))
                .onErrorResume(error ->{
                    log.error("Error al filtrar pacientes por edad" , error.getMessage());
                    return  Flux.empty();
                });

    }

    public Mono<List<Patient>>getPatientsByMultipleCities(List<String>cities){
        log.info("Consultando pacientes en multiples ciudades: {}" ,cities);

        return Flux.fromIterable(cities)
                .flatMap(patientRepository::findByCity)
                .distinct(Patient::getId)
                .collectList()
                .doOnNext(patients -> log.info("Total de pacientes encontrados: {}", patients.size()));
    }

    public record PatientSummary(
            String id,
            String fullName,
            int age,
            String city,
            boolean active

    ){}
}
