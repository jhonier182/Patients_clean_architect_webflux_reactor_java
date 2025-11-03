package sura.pruebalegoback.domain.patient;

import reactor.core.publisher.Mono;
import sura.pruebalegoback.domain.common.ex.BusinessException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class PatientFactory {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    public static Mono<Patient> createPatient(String id, String firstName, String lastName,
                                              String documentNumber, String documentType,
                                              String birthDate, String address, String phone,
                                              String email, String city, String state) {

        return validatePatientData(firstName, lastName, documentNumber, documentType,
                birthDate, phone, email, city,state)
                .map(validated -> Patient.builder()
                        .id(id)
                        .firstName(validated.firstName)
                        .lastName(validated.lastName)
                        .documentNumber(validated.documentNumber)
                        .documentType(validated.documentType)
                        .birthDate(validated.birthDate)
                        .address(address)
                        .phone(validated.phone)
                        .email(validated.email)
                        .city(city)
                        .state(state)
                        .admissionDate(LocalDateTime.now())
                        .active(true)
                        .build());
    }

    private static Mono<ValidatedData> validatePatientData(String firstName, String lastName,
                                                           String documentNumber, String documentType,
                                                           String birthDate, String phone, String email,
                                                           String city ,String state) {

        return Mono.defer(()->{
                    ValidatedData data = new ValidatedData();
                    return validateRequired(firstName, "Nombre requerido")
                            .doOnNext(data::setFirstName)
                            .then(validateRequired(lastName,"Apellido requerido"))
                            .doOnNext(data::setLastName)
                            .then(validateRequired(documentNumber, "Numero de documento requerido"))
                            .doOnNext(data::setDocumentNumber)
                            .then(validateRequired(documentType, "Tipo de documento requerido"))
                            .doOnNext(data::setDocumentType)
                            .then(validatePhone(phone))
                            .doOnNext(data::setPhone)
                            .then(validateEmail(email))
                            .doOnNext(data::setEmail)
                            .then(validateBirthDate(birthDate))
                            .doOnNext(data::setBirthDate)
                            .then(validateRequired(city, "Se requiere ciudad"))
                            .doOnNext(data::setCity)
                            .then(validateRequired(state,"Se requiere el estado"))
                            .doOnNext(data::setState)
                            .thenReturn(data);
        });
    }

    private static Mono<String> validateRequired(String value, String message) {
        return Mono.justOrEmpty(value)
                .filter(v -> v != null && !v.trim().isEmpty())
                .switchIfEmpty(Mono.error(new BusinessException(message)));
    }

    private static Mono<String> validateEmail(String email) {
        return Mono.justOrEmpty(email)
                .filter(e -> e == null || EMAIL_PATTERN.matcher(e).matches())
                .switchIfEmpty(Mono.error(new BusinessException("Email inválido")));
    }

    public static Mono<String> validatePhone(String phone) {
        // Si es null o vacío, se acepta (campo opcional)
        if (phone == null || phone.trim().isEmpty()) {
            return Mono.just(null);
        }
        
        // Normalizar: eliminar espacios, guiones, paréntesis para validación básica
        String normalized = phone.replaceAll("[\\s\\-\\(\\)]", "");
        
        // Validar que después de normalizar tenga solo dígitos y opcionalmente + al inicio
        // Acepta entre 7 y 15 dígitos (rango razonable para números telefónicos)
        if (normalized.matches("^[+]?[0-9]{7,15}$")) {
            return Mono.just(phone); // Devolver el original con su formato
        }
        
        // Si no cumple con el formato, lanzar error con mensaje descriptivo
        return Mono.error(new BusinessException(
            "Teléfono inválido. El formato debe contener entre 7 y 15 dígitos. " +
            "Se aceptan formatos con espacios, guiones y paréntesis. " +
            "Ejemplos válidos: +57 300 1234567, 6012345678, (57) 1234567, 3001234567"));
    }

    private static Mono<java.time.LocalDate> validateBirthDate(String birthDate) {
        return Mono.justOrEmpty(birthDate)
                .flatMap(date -> {
                    try {
                        LocalDate parsed = LocalDate.parse(date);
                        if (parsed.isAfter(LocalDate.now())) {
                            return Mono.error(new BusinessException("Fecha de nacimiento no puede ser futura"));
                        }
                        return Mono.just(parsed);
                    } catch (Exception e) {
                        return Mono.error(new BusinessException("Fecha de nacimiento inválida"));
                    }
                });
    }

    @lombok.Data
    private static class ValidatedData {
        private String firstName;
        private String lastName;
        private String documentNumber;
        private String documentType;
        private String email;
        private String phone;
        private LocalDate birthDate;
        private String city;
        private String state;
    }
}
