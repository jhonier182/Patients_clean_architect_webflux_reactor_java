package sura.pruebalegoback.usecase.patient;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.PatientFactory;
import sura.pruebalegoback.domain.patient.ex.PatientBusinessException;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

@RequiredArgsConstructor
public class UpdatePatientUseCase {
    private static final  Logger log = LoggerFactory.getLogger(UpdatePatientUseCase.class);
    private final PatientRepository patientRepository;

    public Mono<Patient>updatePatient(String id, PatientUpdateData updateData ){
        log.info("Actualizando paciente:");

        // Validar teléfono primero si se está actualizando
        Mono<Void> phoneValidation = updateData.phone() != null 
            ? PatientFactory.validatePhone(updateData.phone()).then()
            : Mono.empty();
        
        return phoneValidation
                .then(patientRepository.findById(id))
                .switchIfEmpty(Mono.error(PatientBusinessException.Type.PATIENT_NOT_FOUND.build()))
                .map(existingPatient -> mergePatientData(existingPatient, updateData))
                .doOnNext(patient -> log.info("Paciente actualizado exitosamente: {}", patient.getId()))
                .flatMap(patientRepository::update)
                .doOnNext(patient -> log.info("Paciente actulizado exitosamente", patient.getId()))
                .onErrorResume(PatientBusinessException.class, error -> {
                    log.error("Error de negocio al actulizar paciente: {} : {}", id, error.getMessage());
                    return Mono.error(error);
                })
                            .onErrorResume(error -> {
                                log.error("Error tecnico al actualizar paciente {}: {}",id, error.getMessage());
                                return Mono.error(new PatientBusinessException(
                                        "Error al actulizar paciente; " + error.getMessage()
                                ));

                            });
    }

    private Patient mergePatientData(Patient existing, PatientUpdateData updateData) {
        return existing.toBuilder()
                .firstName(updateData.firstName() != null ? updateData.firstName() : existing.getFirstName())
                .lastName(updateData.lastName() != null ? updateData.lastName() : existing.getLastName())
                .address(updateData.address() != null ? updateData.address() : existing.getAddress())
                .phone(updateData.phone() != null ? updateData.phone() : existing.getPhone())
                .email(updateData.email() != null ? updateData.email() : existing.getEmail())
                .city(updateData.city() != null ? updateData.city() : existing.getCity())
                .state(updateData.state() != null ? updateData.state() : existing.getState())
                .build();
    }

    public record PatientUpdateData(
            String firstName,
            String lastName,
            String address,
            String phone,
            String email,
            String city,
            String state
    ){}
}
