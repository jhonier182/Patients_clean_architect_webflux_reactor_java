package sura.pruebalegoback.usecase.patient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import sura.pruebalegoback.domain.common.EventsGateway;
import sura.pruebalegoback.domain.patient.Patient;
import sura.pruebalegoback.domain.patient.gateway.PatientRepository;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreatePatientUseCaseTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private EventsGateway eventsGateway;

    private CreatePatientUseCase createPatientUseCase;

    @BeforeEach
    void setUp() {
        createPatientUseCase = new CreatePatientUseCase(patientRepository, eventsGateway);
    }

    @Test
    void shouldCreatePatientSuccessfully() {
        // Given
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

        Patient expectedPatient = Patient.builder()
                .id("test-id")
                .firstName(firstName)
                .lastName(lastName)
                .documentNumber(documentNumber)
                .documentType(documentType)
                .birthDate(java.time.LocalDate.parse(birthDate))
                .address(address)
                .phone(phone)
                .email(email)
                .city(city)
                .state(state)
                .admissionDate(LocalDateTime.now())
                .active(true)
                .build();

        when(patientRepository.save(any(Patient.class))).thenReturn(Mono.just(expectedPatient));
        when(eventsGateway.emit(any())).thenReturn(Mono.empty());

        // When
        var result = createPatientUseCase.createPatient(firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

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
    void shouldFailWithInvalidData() {
        // Given
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
        var result = createPatientUseCase.createPatient(firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

        // Then
        StepVerifier.create(result)
                .expectError()
                .verify();
    }

    @Test
    void shouldContinueEvenIfEventPublishingFails() {
        // Given
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

        Patient expectedPatient = Patient.builder()
                .id("test-id")
                .firstName(firstName)
                .lastName(lastName)
                .documentNumber(documentNumber)
                .documentType(documentType)
                .birthDate(java.time.LocalDate.parse(birthDate))
                .address(address)
                .phone(phone)
                .email(email)
                .city(city)
                .state(state)
                .admissionDate(LocalDateTime.now())
                .active(true)
                .build();

        when(patientRepository.save(any(Patient.class))).thenReturn(Mono.just(expectedPatient));
        when(eventsGateway.emit(any())).thenReturn(Mono.error(new RuntimeException("Event publishing failed")));

        // When
        var result = createPatientUseCase.createPatient(firstName, lastName, documentNumber,
                documentType, birthDate, address, phone, email, city, state);

        // Then
        StepVerifier.create(result)
                .assertNext(patient -> {
                    assertEquals(expectedPatient.getId(), patient.getId());
                    assertEquals(expectedPatient.getFirstName(), patient.getFirstName());
                    assertEquals(expectedPatient.getLastName(), patient.getLastName());
                })
                .verifyComplete();
    }
}
