package sura.pruebalegoback.usecase.patient;


import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.PatientFactory;
import sura.pruebalegoback.domain.patient.events.PatientCreated;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

import java.time.LocalDateTime;


import static sura.pruebalegoback.domain.common.UniqueIDGenerator.uuid;


@RequiredArgsConstructor
public class CreatePatientUseCase {

    private static final Logger log = LoggerFactory.getLogger(CreatePatientUseCase.class);

    private final PatientRepository patientRepository;
    private final EventsGateway eventsGateway;

    public Mono<Patient>createPatient(
            String firstName,
            String lastName,
            String documentNumber,
            String documentType,
            String birthDate,
            String address,
            String phone,
            String email,
            String city,
            String state
    ) {
        log.info("Iniciando creacion de pacientes : {} {}", firstName, lastName);
        return uuid()
                .doOnNext(id -> log.info("UUID generado para paciente: {}", id))
                .flatMap(id -> PatientFactory.createPatient(id,firstName,lastName,documentNumber,
                        documentType,birthDate,address,phone,email,city,state))
                .doOnNext(patient -> log.debug("Paciente validado: {}", patient.getId()))
                .flatMap(patientRepository::save)
                .doOnNext(patient -> log.debug("Paciente guardado exitosamente: {}", patient.getId()))
                .flatMap(this::publishPatienteCreatedEvent)
                .doOnError(error -> log.error("Error al crear  paciente: {}", error.getMessage(), error));



    }

    private Mono<Patient> publishPatienteCreatedEvent(Patient patient) {
        return eventsGateway.emit(new PatientCreated(patient, LocalDateTime.now()))
                .doOnSuccess(v -> log.info("Evento PatientCreated publicado para paciente: {}", patient.getId()))
                .doOnError(error -> log.warn("Error al publicar el evento para paciente {}: {}",
                        patient.getId(), error.getMessage()))
                .thenReturn(patient)
                .onErrorResume(error ->{
                    log.warn("continuando el flujo a pesar del error en la publicacion del evento.");
                    return Mono.just(patient);
                });
    }


}
