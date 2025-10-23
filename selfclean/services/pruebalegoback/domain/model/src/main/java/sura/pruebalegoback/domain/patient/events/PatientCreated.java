package sura.pruebalegoback.domain.patient.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import sura.pruebalegoback.domain.common.Event;
import sura.pruebalegoback.domain.patient.Patient;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public class PatientCreated implements Event {

    private final Patient patient;
    private final LocalDateTime createdAt;

    @Override
    public String name() {
        return "PATIENT_CREATED";
    }
}