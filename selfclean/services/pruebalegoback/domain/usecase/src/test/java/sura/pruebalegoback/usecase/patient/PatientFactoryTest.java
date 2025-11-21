package sura.pruebalegoback.usecase.patient;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.PatientFactory;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class PatientFactoryTest {

    @Test
    void shouldCreatePatientWithValidData() {
        // Given
        String id = "test-id";
        String firstName = "John";
        String lastName = "Doe";
        String documentNumber = "12345678";
        String documentType = "CC";
        String birthDate = "1990-01-01";
        String address = "123 Main St";
        String phone = "+1234567890";
        String email = "john.doe@example.com";
        String city = "Bogotá";
        String state = "Cundinamarca";

        // When
        var result = PatientFactory.createPatient(id, firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals(id, patient.getId());
                    assertEquals(firstName, patient.getFirstName());
                    assertEquals(lastName, patient.getLastName());
                    assertEquals(documentNumber, patient.getDocumentNumber());
                    assertEquals(documentType, patient.getDocumentType());
                    assertEquals(LocalDate.parse(birthDate), patient.getBirthDate());
                    assertEquals(address, patient.getAddress());
                    assertEquals(phone, patient.getPhone());
                    assertEquals(email, patient.getEmail());
                    assertEquals(city, patient.getCity());
                    assertEquals(state, patient.getState());
                    assertTrue(patient.isActive());
                    assertNotNull(patient.getAdmissionDate());
                })
                .verifyComplete();
    }

    @Test
    void shouldFailWithInvalidEmail() {
        // Given
        String id = "test-id";
        String firstName = "John";
        String lastName = "Doe";
        String documentNumber = "12345678";
        String documentType = "CC";
        String birthDate = "1990-01-01";
        String address = "123 Main St";
        String phone = "+1234567890";
        String email = "invalid-email";
        String city = "Bogotá";
        String state = "Cundinamarca";

        // When
        var result = PatientFactory.createPatient(id, firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    void shouldFailWithInvalidPhone() {
        // Given
        String id = "test-id";
        String firstName = "John";
        String lastName = "Doe";
        String documentNumber = "12345678";
        String documentType = "CC";
        String birthDate = "1990-01-01";
        String address = "123 Main St";
        String phone = "invalid-phone";
        String email = "john.doe@example.com";
        String city = "Bogotá";
        String state = "Cundinamarca";

        // When
        var result = PatientFactory.createPatient(id, firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    void shouldFailWithFutureBirthDate() {
        // Given
        String id = "test-id";
        String firstName = "John";
        String lastName = "Doe";
        String documentNumber = "12345678";
        String documentType = "CC";
        String birthDate = "2030-01-01"; // Future date
        String address = "123 Main St";
        String phone = "+1234567890";
        String email = "john.doe@example.com";
        String city = "Bogotá";
        String state = "Cundinamarca";

        // When
        var result = PatientFactory.createPatient(id, firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    void shouldFailWithMissingRequiredFields() {
        // Given
        String id = "test-id";
        String firstName = ""; // Empty first name
        String lastName = "Doe";
        String documentNumber = "12345678";
        String documentType = "CC";
        String birthDate = "1990-01-01";
        String address = "123 Main St";
        String phone = "+1234567890";
        String email = "john.doe@example.com";
        String city = "Bogotá";
        String state = "Cundinamarca";

        // When
        var result = PatientFactory.createPatient(id, firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }
}
