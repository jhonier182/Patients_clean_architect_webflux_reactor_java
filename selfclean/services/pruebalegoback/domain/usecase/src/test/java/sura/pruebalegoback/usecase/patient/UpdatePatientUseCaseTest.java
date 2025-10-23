package sura.pruebalegoback.usecase.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.ex.PatientBusinessException;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePatientUseCaseTest {

    @Mock
    private PatientRepository patientRepository;

    private UpdatePatientUseCase updatePatientUseCase;

    @BeforeEach
    void setUp() {
        updatePatientUseCase = new UpdatePatientUseCase(patientRepository);
    }

    @Test
    void shouldUpdatePatientSuccessfully() {
        // Given
        String patientId = "test-id";
        Patient existingPatient = Patient.builder()
                .id(patientId)
                .firstName("John")
                .lastName("Doe")
                .documentNumber("12345678")
                .documentType("CC")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .phone("+1234567890")
                .email("john.doe@example.com")
                .city("Bogotá")
                .state("Cundinamarca")
                .admissionDate(LocalDateTime.now())
                .active(true)
                .build();

        UpdatePatientUseCase.PatientUpdateData updateData = new UpdatePatientUseCase.PatientUpdateData(
                "John Updated",
                "Doe Updated",
                "456 New St",
                "+0987654321",
                "john.updated@example.com",
                "Medellín",
                "Antioquia"
        );

        Patient updatedPatient = existingPatient.toBuilder()
                .firstName(updateData.firstName())
                .lastName(updateData.lastName())
                .address(updateData.address())
                .phone(updateData.phone())
                .email(updateData.email())
                .city(updateData.city())
                .state(updateData.state())
                .build();

        when(patientRepository.findById(patientId)).thenReturn(Mono.just(existingPatient));
        when(patientRepository.update(any(Patient.class))).thenReturn(Mono.just(updatedPatient));

        // When
        var result = updatePatientUseCase.updatePatient(patientId, updateData);

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals(patientId, patient.getId());
                    assertEquals(updateData.firstName(), patient.getFirstName());
                    assertEquals(updateData.lastName(), patient.getLastName());
                    assertEquals(updateData.address(), patient.getAddress());
                    assertEquals(updateData.phone(), patient.getPhone());
                    assertEquals(updateData.email(), patient.getEmail());
                    assertEquals(updateData.city(), patient.getCity());
                    assertEquals(updateData.state(), patient.getState());
                    // Original fields should remain unchanged
                    assertEquals(existingPatient.getDocumentNumber(), patient.getDocumentNumber());
                    assertEquals(existingPatient.getDocumentType(), patient.getDocumentType());
                    assertEquals(existingPatient.getBirthDate(), patient.getBirthDate());
                    assertEquals(existingPatient.getAdmissionDate(), patient.getAdmissionDate());
                    assertEquals(existingPatient.isActive(), patient.isActive());
                })
                .verifyComplete();
    }

    @Test
    void shouldUpdateOnlyProvidedFields() {
        // Given
        String patientId = "test-id";
        Patient existingPatient = Patient.builder()
                .id(patientId)
                .firstName("John")
                .lastName("Doe")
                .documentNumber("12345678")
                .documentType("CC")
                .birthDate(LocalDate.of(1990, 1, 1))
                .address("123 Main St")
                .phone("+1234567890")
                .email("john.doe@example.com")
                .city("Bogotá")
                .state("Cundinamarca")
                .admissionDate(LocalDateTime.now())
                .active(true)
                .build();

        UpdatePatientUseCase.PatientUpdateData updateData = new UpdatePatientUseCase.PatientUpdateData(
                "John Updated",
                null, // lastName not provided
                null, // address not provided
                null, // phone not provided
                null, // email not provided
                null, // city not provided
                null  // state not provided
        );

        Patient updatedPatient = existingPatient.toBuilder()
                .firstName(updateData.firstName())
                .build();

        when(patientRepository.findById(patientId)).thenReturn(Mono.just(existingPatient));
        when(patientRepository.update(any(Patient.class))).thenReturn(Mono.just(updatedPatient));

        // When
        var result = updatePatientUseCase.updatePatient(patientId, updateData);

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals(patientId, patient.getId());
                    assertEquals(updateData.firstName(), patient.getFirstName());
                    // Other fields should remain unchanged
                    assertEquals(existingPatient.getLastName(), patient.getLastName());
                    assertEquals(existingPatient.getAddress(), patient.getAddress());
                    assertEquals(existingPatient.getPhone(), patient.getPhone());
                    assertEquals(existingPatient.getEmail(), patient.getEmail());
                    assertEquals(existingPatient.getCity(), patient.getCity());
                    assertEquals(existingPatient.getState(), patient.getState());
                })
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        // Given
        String patientId = "non-existent-id";
        UpdatePatientUseCase.PatientUpdateData updateData = new UpdatePatientUseCase.PatientUpdateData(
                "John Updated",
                "Doe Updated",
                null,
                null,
                null,
                null,
                null
        );

        when(patientRepository.findById(patientId)).thenReturn(Mono.empty());

        // When
        var result = updatePatientUseCase.updatePatient(patientId, updateData);

        // Then
        StepVerifier.create(result)
                .expectError(PatientBusinessException.class)
                .verify();
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        String patientId = "test-id";
        UpdatePatientUseCase.PatientUpdateData updateData = new UpdatePatientUseCase.PatientUpdateData(
                "John Updated",
                "Doe Updated",
                null,
                null,
                null,
                null,
                null
        );

        when(patientRepository.findById(patientId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When
        var result = updatePatientUseCase.updatePatient(patientId, updateData);

        // Then
        StepVerifier.create(result)
                .expectError(PatientBusinessException.class)
                .verify();
    }
}
