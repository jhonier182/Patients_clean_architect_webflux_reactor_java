package sura.pruebalegoback.domain.patient.ex;

import sura.pruebalegoback.domain.common.ex.ApplicationException;

public class PatientBusinessException extends ApplicationException {

    public PatientBusinessException(String message) {
        super(message);
    }

    public PatientBusinessException(String message, String code) {
        super(message, code);
    }

    // Opcional: Enum para tipos predefinidos
    public enum Type {
        PATIENT_NOT_FOUND("Paciente no encontrado"),
        INVALID_PATIENT_DATA("Datos del paciente inválidos"),
        DUPLICATE_DOCUMENT("Número de documento ya existe"),
        PATIENT_ALREADY_INACTIVE("El paciente ya está inactivo");

        private final String message;

        Type(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public PatientBusinessException build() {
            return new PatientBusinessException(this.message);
        }
    }
}