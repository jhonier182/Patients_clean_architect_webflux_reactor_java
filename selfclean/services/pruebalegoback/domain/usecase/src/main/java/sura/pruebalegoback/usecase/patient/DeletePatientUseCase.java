package sura.pruebalegoback.usecase.patient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.ex.PatientBusinessException;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

@RequiredArgsConstructor
public class DeletePatientUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeletePatientUseCase.class);
    private final PatientRepository patientRepository;


    public Mono<Void>deletePatient(String id){
        log.info("Eliminando paciente con Id: {}", id);

        return patientRepository.findById(id)
                .switchIfEmpty(Mono.error(PatientBusinessException.Type.PATIENT_NOT_FOUND.build()))
                .doOnNext(patient -> log.debug("Paciente encontrado para eliminar: {}", patient.getId()))
                .flatMap(patient -> patientRepository.deleteById(id))
                .doOnSuccess(v -> log.info("Paciente eliminado exitosamente"))
                .onErrorResume(PatientBusinessException.class, Mono::error)
                .onErrorResume(error ->{
                    log.error("Error al eliminar paciente: ", error.getMessage());
                    return Mono.error(new PatientBusinessException(
                            "Error al eliminar pacientes: " + error.getMessage()
                    ));
                });
    }


    public Mono<Patient>deactivatePatient(String id){
        log.info("Desactivando un paciente");

        return patientRepository.findById(id)
                .switchIfEmpty(Mono.error(PatientBusinessException.Type.PATIENT_NOT_FOUND.build()))
                .filter(Patient::isActive)
                .switchIfEmpty(Mono.error(
                        PatientBusinessException.Type.PATIENT_ALREADY_INACTIVE.build()
                ))
                .map(patient -> patient.toBuilder().active(false).build())
                .flatMap(patientRepository::update)
                .doOnNext(patient -> log.info("Paciente desactivado"))
                .onErrorResume(PatientBusinessException.class, Mono::error)
                .onErrorResume(error -> {
                    log.error("Error al desactivar paciente", error.getMessage());
                    return Mono.error(new PatientBusinessException(
                            "Error al desactivar paciente" + error.getMessage()
                    ));
                });

    }

    public Mono<Patient> reactivatePatient(String id) {
        log.info("Reactivando paciente con ID: {}", id);

        return patientRepository.findById(id)
                .switchIfEmpty(Mono.error(PatientBusinessException.Type.PATIENT_NOT_FOUND.build()))
                .filter(patient -> !patient.isActive())
                .switchIfEmpty(Mono.error(new PatientBusinessException("El paciente ya está activo")))
                .map(patient -> patient.toBuilder().active(true).build())
                .flatMap(patientRepository::update)
                .doOnNext(patient -> log.info("Paciente reactivado exitosamente: {}", patient.getId()));
    }

}
