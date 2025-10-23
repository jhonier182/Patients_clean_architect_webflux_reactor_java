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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetPatientByIdUseCaseTest {

    @Mock
    private PatientRepository patientRepository;

    private GetPatientByIdUseCase getPatientByIdUseCase;

    @BeforeEach
    void setUp() {
        getPatientByIdUseCase = new GetPatientByIdUseCase(patientRepository);
    }

    @Test
    void shouldReturnPatientWhenFound() {
        // Given
        String patientId = "test-id";
        Patient expectedPatient = Patient.builder()
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

        when(patientRepository.findById(patientId)).thenReturn(Mono.just(expectedPatient));

        // When
        var result = getPatientByIdUseCase.execute(patientId);

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals(expectedPatient.getId(), patient.getId());
                    assertEquals(expectedPatient.getFirstName(), patient.getFirstName());
                    assertEquals(expectedPatient.getLastName(), patient.getLastName());
                    assertEquals(expectedPatient.getDocumentNumber(), patient.getDocumentNumber());
                    assertEquals(expectedPatient.getDocumentType(), patient.getDocumentType());
                    assertEquals(expectedPatient.getBirthDate(), patient.getBirthDate());
                    assertEquals(expectedPatient.getAddress(), patient.getAddress());
                    assertEquals(expectedPatient.getPhone(), patient.getPhone());
                    assertEquals(expectedPatient.getEmail(), patient.getEmail());
                    assertEquals(expectedPatient.getCity(), patient.getCity());
                    assertEquals(expectedPatient.getState(), patient.getState());
                    assertTrue(patient.isActive());
                    assertNotNull(patient.getAdmissionDate());
                })
                .verifyComplete();
    }

    @Test
    void shouldThrowExceptionWhenPatientNotFound() {
        // Given
        String patientId = "non-existent-id";
        when(patientRepository.findById(patientId)).thenReturn(Mono.empty());

        // When
        var result = getPatientByIdUseCase.execute(patientId);

        // Then
        StepVerifier.create(result)
                .expectError(PatientBusinessException.class)
                .verify();
    }

    @Test
    void shouldHandleRepositoryError() {
        // Given
        String patientId = "test-id";
        when(patientRepository.findById(patientId)).thenReturn(Mono.error(new RuntimeException("Database error")));

        // When
        var result = getPatientByIdUseCase.execute(patientId);

        // Then
        StepVerifier.create(result)
                .expectError(PatientBusinessException.class)
                .verify();
    }
}
