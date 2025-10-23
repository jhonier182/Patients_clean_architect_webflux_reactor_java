package sura.pruebalegoback.usecase.patient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.ex.PatientBusinessException;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;


@RequiredArgsConstructor
public class GetPatientByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetPatientByIdUseCase.class);
    private final PatientRepository patientRepository;

    public Mono<Patient> execute(String id){
        log.info("Buscando paciente por id");

        return patientRepository.findById(id)
                .doOnNext(patient -> log.debug("Paciente encontrado: {} {}",
                        patient.getFirstName(), patient.getLastName()))
                .switchIfEmpty(Mono.defer(()->{
                    log.warn("Paciente no encontrado");
                    return  Mono.error(PatientBusinessException.Type.PATIENT_NOT_FOUND.build());
                }))
                .onErrorResume(PatientBusinessException.class ,error -> {
                    log.error("Error de negocio  al buscar paciente {}", error.getMessage());
                    return  Mono.error(error);
                })
                .onErrorResume(error -> {
                    log.error("Error tecnico al buscar paciente {} : {} ", id, error.getMessage());
                    return Mono.error(new PatientBusinessException(
                            "Error al buscar el paciente: " + error.getMessage()
                    ));
                });
    }

    //Demuestra: switchIfEmpty, Mono.defer, onErrorResume

}
