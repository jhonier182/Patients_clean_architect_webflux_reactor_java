package sura.pruebalegoback.domain.patient.gateway;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.patient.Patient;

public interface PatientRepository {

    Mono<Patient> save(Patient patient);
    Mono<Patient> findById(String id);
    Flux<Patient> findAll();
    Flux<Patient> findByActive(boolean active);
    Flux<Patient> findByDocumentNumber(String documentNumber);
    Flux<Patient> findByCity(String city);
    Mono<Void> deleteById(String id);
    Mono<Patient> update(Patient patient);
}
